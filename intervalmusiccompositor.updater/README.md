# IntervalMusicCompositor Updater

The Updater is a standalone program to be invoked by the main application which helps to update the main applications program files.
For Linux and macOS we use the Java version (`intervalmusiccompositor.updater.jar`) since they are able to replace files which are currently used (i.e. the Java modules). 
For Windows -- which does not allow manipulation of files currently in use -- we have this native updater`intervalmusiccompositor-updater.exe` written in Rust.

## Native Windows updater

We want to target these architectures:

- x86_64-unknown-linux-gnu
- x86_64-apple-darwin
- x86_64-pc-windows-gnu

### Preparation

Install the Rust programming environment. 

#### Windows target

Then install a toolchain for Windows:

    $ rustup target add x86_64-pc-windows-gnu
    $ rustup toolchain install stable-x86_64-pc-windows-gnu

as well as apparently:

    $ sudo apt-get install mingw-w64 gcc-mingw-w64-x86-64

#### MacOs target

The following information is from this page: https://wapl.es/rust/2019/02/17/rust-cross-compile-linux-to-macos.html

    $ sudo apt-get install \
        cmake \
        clang \
        gcc \
        g++ \
        zlib1g-dev \
        libmpc-dev \
        libmpfr-dev \
        libgmp-dev \
        libxml2-dev \
        libssl-dev

    $ git clone https://github.com/tpoechtrager/osxcross
    $ cd osxcross
    $ wget -nc https://s3.dockerproject.org/darwin/v2/MacOSX10.10.sdk.tar.xz
    $ mv MacOSX10.10.sdk.tar.xz tarballs/
    $ UNATTENDED=yes OSX_VERSION_MIN=10.7 ./build.sh

### Build

Then, in the `native-src` directory, build the executable:

#### Linux

    $ cargo build --release --target x86_64-unknown-linux-gnu

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-unknown-linux-gnu/release/intervalmusiccompositor-updater

#### MacOs

    $ export PATH="/home/nw/projects/osxcross/target/bin:$PATH"
    $ cargo build --release --target x86_64-apple-darwin

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-apple-darwin/release/intervalmusiccompositor-updater

#### Windows

    $ cargo build --release --target x86_64-pc-windows-gnu

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-pc-windows-gnu/release/intervalmusiccompositor-updater.exe
