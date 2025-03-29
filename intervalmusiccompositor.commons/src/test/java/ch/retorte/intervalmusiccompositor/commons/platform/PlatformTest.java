package ch.retorte.intervalmusiccompositor.commons.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    assertInstanceOf(LinuxPlatform.class, platform);
  }

  @Test
  public void shouldCreateWindowsPlatform() {
    // when
    Platform platform = new PlatformFactory("Windows").getPlatform();

    // then
    assertInstanceOf(WindowsPlatform.class, platform);
  }

  @Test
  public void shouldCreateMacOsxPlatform() {
    // when
    Platform platform = new PlatformFactory("Mac OSX").getPlatform();

    // then
    assertInstanceOf(MacOsxPlatform.class, platform);
  }

  @Test
  public void shouldFailOnUnknownPlatform() {
    assertThrows(PlatformNotFoundException.class, () -> new PlatformFactory("Unknown platform").getPlatform());
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
    assertEquals("/home/user/intervalmusiccompositor", distributionRootPath1);
    assertEquals("/home/user/intervalmusiccompositor", distributionRootPath2);
    assertEquals("/home/user/intervalmusiccompositor", distributionRootPath3);
  }
}
