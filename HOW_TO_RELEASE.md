# How to release

This is a little guide how to perform a new **Interval Music Compositor** release.

## Source preparations

First, prepare the source code for the upcoming release.

- Check that all changes have been written to the change log file in `intervalmusiccompositor.app/src/main/resources/CHANGELOG.txt`
- Decide about the new version according to the semantic versioning scheme (http://semver.org/), e.g. `1.2.3`.
- Upgrade the version by changing it in the following files:
  - For the build: `intervalmusiccompositor.app/gradle.properties`
  - For inside the application: `intervalmusiccompositor.app/src/main/resources/imc.properties`
  - For the documentation: `intervalmusiccompositor.app/src/main/resources/CHANGELOG.txt`
- Commit the change to the `master` branch and tag the version with `v` prefix:

      $ git add .
      $ git commit -m 'Upgraded to version 1.2.3.'
      $ git tag v1.2.3
      $ git push 
      $ git push --tags

## Release distribution

Second, build the actual release distribution and release it on github. 

- Build the distribution with:

      $ ./gradlew clean distAll

  The distribution files are now to be found at `intervalmusiccompositor.app/build/distributions/`.
- On the github releases page (https://github.com/nwaldispuehl/interval-music-compositor/releases) create a new release following the pattern of previous releases. It should contain:
  - The title: `Interval Music Compositor 1.2.3`
  - A list of significant changes including links to the github issues (if applicable), taken from the change log. 
  - A link to the full diff since the last release.
  - A hint towards the instruction (http://nwaldispuehl.github.io/interval-music-compositor/en/instructions) and download (http://nwaldispuehl.github.io/interval-music-compositor/en/download) pages.
  - The uploaded distribution archives for the three operating systems Linux, Mac, and Windows.
  - The source code is added automatically by github.

## Download pages update

Then, update references pointing to the download.

### Github project page

- Check out the project website (http://nwaldispuehl.github.io/interval-music-compositor/en/) which is stored in the same project in the `gh-pages` branch:

      $ git checkout gh-pages

- Edit the top part on the download pages `download.markdown` (german), and `en/download.markdown` (english) to match the current release:

> **The current version 1.2.3 was issued on 1st January 1970.**<br/>

- Commit and push the changes to the origin again:

      $ git add .
      $ git commit -m 'Upgraded version on download page.'
      $ git push
      
### Heise downloads

We have a product entry in the download page of the german it and software website heise online (https://www.heise.de/): https://www.heise.de/download/product/interval-music-compositor-78824

It just links the github pages download page, but the version (and occasionally the screenshot) has to be updated. 

## Update mechanism update

Finally update the version in the resource consulted by the program when checking for new versions. It is located at:

https://interval-music-compositor.retorte.ch/current_version.txt

The legacy version is located at:

http://interval_music_compositor.retorte.ch/current_version.txt
