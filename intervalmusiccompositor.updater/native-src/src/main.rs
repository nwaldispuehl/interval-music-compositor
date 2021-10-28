// On Windows platform, don't show a console when opening the app.
#![windows_subsystem = "windows"]

use std::{thread, time};
use std::env;
use std::fs;
use std::ops::Not;
use std::path::Path;
use std::process::Command;
use std::str;
use std::time::Duration;
use std::sync::mpsc::{channel, Sender, Receiver};

use sysinfo::{Pid, ProcessExt, System, SystemExt};

const HALF_A_SECOND: Duration = time::Duration::from_millis(500);

const MESSAGE_ID: Selector<String> = Selector::new("message");

use druid::widget::prelude::*;
use druid::widget::{Flex, Label};
use druid::{AppDelegate, AppLauncher, Data, DelegateCtx, Handled, Lens, Selector, Target, UnitPoint, WidgetExt, WindowDesc};

/// Stand-alone updater for the IntervalMusicCompositor.
///
/// Performs these tasks:
/// - waits for the main program to terminate (by pid provided as argument)
/// - copies the main library from the upgrade directory to the program directory, and
/// - restarts the main program.
///
/// We use this separate updater since not all OS allow for overwriting files which are currently
/// in use, as for example the Java module files used in the main program.
///
fn main() {
    let args: Vec<String> = env::args().collect();

    // Check if number of arguments is ok
    if args.len() != 3 {
        println!("Args: {:?}", args);
        quit_with_error("Arguments: SOFTWARE_ROOT_DIR MAIN_PROGRAM_PID");
    }

    let software_root_dir = args[1].clone();
    let main_program_pid = args[2].clone();

    // Check if the root dir is really pointing to the software by checking for the existence of a known file.
    if no_software_detected_in(&software_root_dir) {
        quit_with_error("SOFTWARE_ROOT_DIR does not appear to point to actual software directory.");
    }

    //println!("Arguments: dir: '{}', pid: '{}'", software_root_dir, main_program_pid);

    open_program_window_with(main_program_pid, software_root_dir);
}

fn no_software_detected_in(software_root_dir: &String) -> bool {
    Path::new(software_root_dir).join("lib").join("modules").exists().not()
}


//---- UI code

fn open_program_window_with(main_program_pid: String, software_root_dir: String) {

    // Main window
    let main_window = WindowDesc::new(||build_root_widget())
        .title("Interval Music Compositor Updater")
        .window_size((620.0, 120.0));

    // App state
    let message: Message = Message {
        message: "Starting...".into(),
        terminate: false
    };

    // Communication channels
    let (sender, receiver): (Sender<Message>, Receiver<Message>) = channel();

    perform_upgrade_with(main_program_pid.clone(), software_root_dir.clone(), sender.clone());

    // Start the application
    let app_launcher = AppLauncher::with_window(main_window);

    let sink = app_launcher.get_external_handle();

    thread::spawn( move || {
        while let Ok(msg) = receiver.recv() {
            sink.submit_command(MESSAGE_ID, msg.message, Target::Auto)
                .expect("Unable to send display message.");

            if msg.terminate {
                break
            }
        }
    });

    app_launcher
        .delegate(Delegate {})
        .launch(message)
        .expect("Failed to launch application");
}

fn build_root_widget() -> impl Widget<Message> {
    // a label that will determine its text based on the current app data.
    let label = Label::new(|data: &Message, _env: &Env| {
        format!("{}", data.message)
    }).with_text_size(16.0);

    Flex::column()
        .with_child(label)
        .align_vertical(UnitPoint::CENTER)
}

#[derive(Clone, Data, Lens)]
struct Message {
    message: String,
    terminate: bool
}

impl Message {
    fn new(str: String) -> Message {
        Message {
            message: str,
            terminate: false
        }
    }

}

struct Delegate;

impl AppDelegate<Message> for Delegate {
    fn command(
        &mut self,
        _ctx: &mut DelegateCtx,
        _target: Target,
        cmd: &druid::Command,
        data: &mut Message,
        _env: &Env,
    ) -> Handled {
        if let Some(msg) = cmd.get(MESSAGE_ID) {
            data.message = msg.to_string();
            Handled::Yes
        } else {
            Handled::No
        }
    }
}


//---- Update code

fn perform_upgrade_with(main_program_pid: String, software_root_dir: String, sender: Sender<Message>) {
    // We start the upgrade in another thread to not block the UI.
    thread::spawn(move || {
        sleep_for_half_a_second();
        wait_for_process(&main_program_pid, sender.clone());
        upgrade(&software_root_dir, sender.clone());
        restart_software_in(&software_root_dir, sender.clone());
    });
}

fn sleep_for_half_a_second() {
    thread::sleep(HALF_A_SECOND);
}

fn wait_for_process(main_program_pid: &String, sender: Sender<Message>) {
    print(sender.clone(), &format!("Waiting for process with pid '{}'", main_program_pid));

    let mut sys = System::new_all();
    let pid = Pid::from(parse(main_program_pid));
    let hundred_millis = time::Duration::from_millis(100);

    if sys.process(pid).is_some() {
        loop {
            sys.refresh_processes();
            match sys.process(pid) {
                None => {
                    break
                },
                Some(process) => {
                    print(sender.clone(), &format!("Process '{}' running (status: '{}'), waiting...", process.name(), process.status()));
                    thread::sleep(hundred_millis);
                }
            }
        }
    }

    print(sender.clone(), "Process terminated or not existing.");
}

fn upgrade(software_root_dir: &String, sender: Sender<Message>) {
    print(sender.clone(), &format!("Upgrade in {}", software_root_dir));

    let upgrade_dir = Path::new(software_root_dir).join("upgrade").join("IntervalMusicCompositor");
    let root_dir = Path::new(software_root_dir);

    let result = copy(upgrade_dir.join("lib").join("modules").as_path(),root_dir.join("lib").join("modules").as_path(), sender.clone());

    match result {
        Err(e) => {
            quit_with_error_and_message(sender.clone(), &format!("Unable to copy due to: {}.", e));
        },
        Ok(_) => {
            print(sender.clone(), "Copy process was successful.");
        }
    }
}

fn copy(from: &Path, to: &Path, sender: Sender<Message>) -> std::io::Result<u64> {
    print(sender, &format!("Copying '{}' to '{}'", from.to_str().unwrap(), to.to_str().unwrap()));
    fs::copy(from, to)
}

fn restart_software_in(software_root_dir: &String, sender: Sender<Message>) {
    let executable = Path::new(software_root_dir).join("bin").join(main_program_name());
    let result = Command::new(executable)
        .spawn();
    match result {
        Err(e) => quit_with_error_and_message(sender.clone(), &format!("Unable to start main application due to: {}.", e)),
        Ok(_) => print(sender.clone(), "Successfully started main application again.")
    }
    sender.send(Message{ message: "Quitting...".to_string(), terminate: true }).unwrap();
    thread::sleep(HALF_A_SECOND);
    std::process::exit(0);
}

fn print(sender: Sender<Message>, text: &str) {
    sender.send(Message::new(format!("{}", text))).unwrap();
}

fn quit_with_error_and_message(sender: Sender<Message>, text: &str) {
    sender.send(Message::new(format!("{}", text))).unwrap();
    quit_with_error(text);
}

fn quit_with_error(text: &str) {
    eprintln!("{}", text);
    std::process::exit(1);
}

//---- Logger


//---- OS dependent functions

///
/// The return type is system dependent:
///
/// Linux: i32
/// Mac: i32
/// Windows: usize
///
#[cfg(target_os = "windows")]
fn parse(main_program_id: &String) -> usize {
    main_program_id.parse::<>().unwrap()
}

///
/// The return type is system dependent:
///
/// Linux: i32
/// Mac: i32
/// Windows: usize
///
#[cfg(not(target_os = "windows"))]
fn parse(main_program_id: &String) -> i32 {
    main_program_id.parse::<>().unwrap()
}

#[cfg(target_os = "windows")]
fn main_program_name() -> &'static str {
    "intervalmusiccompositor.app.bat"
}

#[cfg(not(target_os = "windows"))]
fn main_program_name() -> &'static str {
    "intervalmusiccompositor.app"
}
