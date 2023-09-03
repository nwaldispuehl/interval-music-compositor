# IntervalMusicCompositor Updater

The Updater is a standalone program to be invoked by the main application which helps to update the main applications program files.
Since some operating systems do not allow manipulation of files currently in use, we have this native updater written in Rust.
It is compiled to a binary `intervalmusiccompositor-updater` for the Linux and Mac OSX operating systems, and a `intervalmusiccompositor-updater.exe` for Windows, respectively.

## Native Windows updater

We want to target these architectures:

- x86_64-unknown-linux-gnu
- x86_64-apple-darwin
- x86_64-pc-windows-gnu
- aarch64-apple-darwin

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
    $ wget -nc https://github.com/phracker/MacOSX-SDKs/releases/download/11.3/MacOSX11.3.sdk.tar.xz
    $ mv MacOSX11.3.sdk.tar.xz.xz tarballs/
    $ UNATTENDED=yes OSX_VERSION_MIN=11 ./build.sh

### Build

Then, in the `native-src` directory, build the executable:

#### Linux

    $ cargo build --release --target x86_64-unknown-linux-gnu

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-unknown-linux-gnu/release/intervalmusiccompositor-updater

#### MacOs

    $ export PATH="${HOME}/projects/osxcross/target/bin:$PATH"
    $ cargo build --release --target x86_64-apple-darwin

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-apple-darwin/release/intervalmusiccompositor-updater

#### MacOs Aarch64

    $ export PATH="${HOME}/projects/osxcross/target/bin:$PATH"
    $ cargo build --release --target aarch64-apple-darwin

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/aarch64-apple-darwin/release/intervalmusiccompositor-updater

#### Windows

    $ cargo build --release --target x86_64-pc-windows-gnu

The executable is then present in this directory:

    intervalmusiccompositor.updater/native-src/target/x86_64-pc-windows-gnu/release/intervalmusiccompositor-updater.exe
