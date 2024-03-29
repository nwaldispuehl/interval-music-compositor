// On Windows platform, don't show a console when opening the app.
#![windows_subsystem = "windows"]

use std::thread;
use std::env;
use std::io::prelude::*;
use std::fs;
use std::fs::{DirEntry, OpenOptions};
use std::ops::Not;
use std::path::{Path, PathBuf};
use std::process::Command;
use std::str;
use std::sync::mpsc::{channel, Receiver, Sender};
use std::time::Duration;

use chrono::Utc;
use dircpy::*;
use druid::{AppDelegate, AppLauncher, Data, DelegateCtx, Handled, Lens, Selector, Target, UnitPoint, WidgetExt, WindowDesc};
use druid::widget::{Flex, Label};
use druid::widget::prelude::*;
use sysinfo::{Pid, PidExt, ProcessExt, System, SystemExt};

const HALF_A_SECOND: Duration = Duration::from_millis(500);

const MESSAGE_ID: Selector<String> = Selector::new("message");

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

            // Copy the content to the log file.
            let mut file = OpenOptions::new()
                .create(true)
                .append(true)
                .open(Path::new(&software_root_dir).join("upgrade.log")).unwrap();
            if let Err(e) = writeln!(file, "[{}] {}", Utc::now().format("%Y-%m-%d %H:%M:%S"), msg.clone().message) {
                eprintln!("Unable to write into log file: {}", e);
            }

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
    let pid = Pid::from_u32(parse(main_program_pid));

    if sys.process(pid).is_some() {
        loop {
            sys.refresh_processes();
            match sys.process(pid) {
                None => {
                    break
                },
                Some(process) => {
                    print(sender.clone(), &format!("Process '{}' running (status: '{}'), waiting...", process.name(), process.status()));
                    thread::sleep(HALF_A_SECOND);
                }
            }
        }
    }

    print(sender.clone(), "Process terminated or not existing.");
}

fn parse(main_program_id: &String) -> u32 {
    main_program_id.parse::<>().unwrap()
}

fn upgrade(software_root_dir: &String, sender: Sender<Message>) {
    print(sender.clone(), &format!("Upgrade in {}", software_root_dir));

    let upgrade_dir = Path::new(software_root_dir).join("upgrade").join("IntervalMusicCompositor");
    let root_dir = Path::new(software_root_dir).to_path_buf();

    copy_dir(&upgrade_dir, &root_dir, "bin", sender.clone());
    make_dir_executable(&root_dir.join("bin"), sender.clone());
    copy_dir(&upgrade_dir, &root_dir, "conf", sender.clone());
    copy_dir(&upgrade_dir, &root_dir, "include", sender.clone());
    copy_dir(&upgrade_dir, &root_dir, "legal", sender.clone());
    copy_dir(&upgrade_dir, &root_dir, "lib", sender.clone());
    copy_dir(&upgrade_dir, &root_dir, "man", sender.clone());

    copy_file(&upgrade_dir, &root_dir, "CHANGELOG.txt", sender.clone());
    copy_file(&upgrade_dir, &root_dir, "CREDITS.txt", sender.clone());
    copy_file(&upgrade_dir, &root_dir, "interval_music_compositor.svg", sender.clone());
    copy_file(&upgrade_dir, &root_dir, "release", sender.clone());
}

fn copy_dir(upgrade_dir: &PathBuf, root_dir: &PathBuf, item: &str, sender: Sender<Message>) {

    let from = upgrade_dir.join(item);
    let to = root_dir.join(item);

    print(sender.clone(), &format!("Copying directory '{}' to '{}'", from.to_str().unwrap(), to.to_str().unwrap()));

    let result = CopyBuilder::new(from, to)
        .overwrite(true)
        // Never copy the updater binary
        .with_exclude_filter("intervalmusiccompositor-updater")
        .run();

    match result {
        Ok(_) => {
            print(sender.clone(), "  Success.");
        },
        Err(e) => {
            print(sender.clone(), &format!("Unable to copy due to: {}.", e));
        }
    }
}

fn make_dir_executable(directory: &PathBuf, sender: Sender<Message>) {
    let files = fs::read_dir(directory).unwrap();
    for file in files {
        let entry = file.unwrap();
        print(sender.clone(), &format!("Marking executable: '{}'", entry.path().to_str().unwrap()));
        let _ = make_executable(&entry);
    }

}

fn copy_file(upgrade_dir: &PathBuf, root_dir: &PathBuf, item: &str, sender: Sender<Message>) {

    let from = upgrade_dir.join(item);
    let to = root_dir.join(item);

    print(sender.clone(), &format!("Copying file '{}' to '{}'", from.to_str().unwrap(), to.to_str().unwrap()));

    let result = fs::copy(from, to);

    match result {
        Ok(_) => {
            print(sender.clone(), "  Success.");
        },
        Err(e) => {
            print(sender.clone(), &format!("Unable to copy due to: {}.", e));
        }
    }
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

#[cfg(target_os = "linux")]
fn make_executable(entry: &DirEntry) -> std::io::Result<()> {
    use std::fs::File;
    use std::os::unix::fs::PermissionsExt;
    let file = File::open(entry.path())?;
    let mut perms = file.metadata()?.permissions();
    perms.set_mode(0o744);
    file.set_permissions(perms)?;
    Ok(())
}

#[cfg(target_os = "macos")]
fn make_executable(_entry: &DirEntry) -> std::io::Result<()> {
    Ok(())
}

#[cfg(target_os = "windows")]
fn make_executable(_entry: &DirEntry) -> std::io::Result<()> {
    Ok(())
}



#[cfg(target_os = "windows")]
fn main_program_name() -> &'static str {
    "intervalmusiccompositor.app.bat"
}

#[cfg(not(target_os = "windows"))]
fn main_program_name() -> &'static str {
    "intervalmusiccompositor.app"
}
