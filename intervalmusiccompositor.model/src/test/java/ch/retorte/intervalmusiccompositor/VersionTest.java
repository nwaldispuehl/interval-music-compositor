package ch.retorte.intervalmusiccompositor;

import ch.retorte.intervalmusiccompositor.model.update.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author nw
 */
public class VersionTest {

  @Test
  public void shouldReflectVersion() {
    // when
    Version v0 = new Version(0);
    Version v1 = new Version(1);
    Version v2_3 = new Version(2, 3);
    Version v4_5_6 = new Version(4, 5, 6);
    Version v7_8_9_10 = new Version(7, 8, 9, 10);

    // then
    assertVersion(v0, 0, 0, 0, 0);
    assertVersion(v1, 1, 0, 0, 0);
    assertVersion(v2_3, 2, 3, 0, 0);
    assertVersion(v4_5_6, 4, 5, 6, 0);
    assertVersion(v7_8_9_10, 7, 8, 9, 10);
  }
  
  @Test
  public void shouldParseVersionString() {
    // given
    String versionString = "9.8.7.6";

    // when
    Version version = new Version(versionString);

    // then
    assertVersion(version, 9, 8, 7, 6);
  }

  @Test
  public void shouldCompare() {
    // given
    Version higherVersion = new Version(10, 1, 2, 3);
    Version lessHigherVersion = new Version(10, 1, 2, 2);
    Version inbetweenVersion = new Version(5, 4, 5, 6);
    Version lowerVersion = new Version(1, 7, 8, 9);
    Version otherInbetweenVersion = new Version("5.4.5.6");

    // then
    assertHigherThan(higherVersion, lessHigherVersion);
    assertHigherThan(lessHigherVersion, inbetweenVersion);
    assertHigherThan(inbetweenVersion, lowerVersion);

    assertEquals(0, inbetweenVersion.compareTo(otherInbetweenVersion));
  }

  @Test
  public void shouldPrettyPrint() {
    assertEquals("1.2.3", new Version(1, 2, 3).toString());
  }

  private void assertVersion(Version v, int major, int minor, int revision, int build) {
    assertEquals(major, v.getMajor());
    assertEquals(minor, v.getMinor());
    assertEquals(revision, v.getRevision());
    assertEquals(build, v.getBuild());
  }

  private void assertHigherThan(Version higherVersion, Version lowerVersion) {
    assertEquals(1, higherVersion.compareTo(lowerVersion));
  }
}
