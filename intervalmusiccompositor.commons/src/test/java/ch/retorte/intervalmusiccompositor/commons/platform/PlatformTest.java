package ch.retorte.intervalmusiccompositor.commons.platform;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author nw
 */
public class PlatformTest {

  @Test
  public void shouldKnowOwnPlatform() {
    // given
    Platform windows = new WindowsPlatform("");
    Platform linux = new LinuxPlatform("");
    Platform macosx = new MacOsxPlatform("");

    // then
    assertTrue(windows.isWindows());
    assertFalse(windows.isLinux());
    assertFalse(windows.isMac());

    assertFalse(linux.isWindows());
    assertTrue(linux.isLinux());
    assertFalse(linux.isMac());

    assertFalse(macosx.isWindows());
    assertFalse(macosx.isLinux());
    assertTrue(macosx.isMac());
  }

  @Test
  public void shouldCreateLinuxPlatform() {
    // when
    Platform platform = new PlatformFactory("Linux").getPlatform();

    // then
    assertThat(platform, instanceOf(LinuxPlatform.class));
  }

  @Test
  public void shouldCreateWindowsPlatform() {
    // when
    Platform platform = new PlatformFactory("Windows").getPlatform();

    // then
    assertThat(platform, instanceOf(WindowsPlatform.class));
  }

  @Test
  public void shouldCreateMacOsxPlatform() {
    // when
    Platform platform = new PlatformFactory("Mac OSX").getPlatform();

    // then
    assertThat(platform, instanceOf(MacOsxPlatform.class));
  }

  @Test (expected = PlatformNotFoundException.class)
  public void shouldFailOnUnknownPlatform() {
    new PlatformFactory("Unknown platform").getPlatform();
  }

  @Test
  public void shouldProduceCorrectDistributionRootPath() {
    // given
    Platform platform = new PlatformFactory("Linux").getPlatform();
    String binaryPath1 = "/home/user/intervalmusiccompositor/bin";
    String binaryPath2 = "/home/user/./intervalmusiccompositor/bin";
    String binaryPath3 = "/home/user/intervalmusiccompositor/bin/.";

    // when
    String distributionRootPath1 = platform.getDistributionRootPathWith(binaryPath1);
    String distributionRootPath2 = platform.getDistributionRootPathWith(binaryPath2);
    String distributionRootPath3 = platform.getDistributionRootPathWith(binaryPath3);

    // then
    assertThat(distributionRootPath1, is("/home/user/intervalmusiccompositor"));
    assertThat(distributionRootPath2, is("/home/user/intervalmusiccompositor"));
    assertThat(distributionRootPath3, is("/home/user/intervalmusiccompositor"));

  }

}
