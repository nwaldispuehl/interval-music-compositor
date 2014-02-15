package ch.retorte.intervalmusiccompositor.commons.platform;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
