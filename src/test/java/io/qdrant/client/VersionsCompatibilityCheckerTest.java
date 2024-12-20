package io.qdrant.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class VersionsCompatibilityCheckerTest {
  private static Stream<Object[]> validVersionProvider() {
    return Stream.of(
        new Object[] {"1.2.3", 1, 2, "3"},
        new Object[] {"1.2.3-alpha", 1, 2, "3-alpha"},
        new Object[] {"1.2", 1, 2, ""},
        new Object[] {"1", 1, 0, ""},
        new Object[] {"1.", 1, 0, ""});
  }

  @ParameterizedTest
  @MethodSource("validVersionProvider")
  public void testParseVersion_validVersion(
      String versionStr, int expectedMajor, int expectedMinor, String expectedRest)
      throws Exception {
    Method method =
        VersionsCompatibilityChecker.class.getDeclaredMethod("parseVersion", String.class);
    method.setAccessible(true);
    Version version = (Version) method.invoke(null, versionStr);
    assertEquals(expectedMajor, version.getMajor());
    assertEquals(expectedMinor, version.getMinor());
    assertEquals(expectedRest, version.getRest());
  }

  private static Stream<String> invalidVersionProvider() {
    return Stream.of("v1.12.0", "", ".1", ".1.", "1.null.1", "null.0.1", null);
  }

  @ParameterizedTest
  @MethodSource("invalidVersionProvider")
  public void testParseVersion_invalidVersion(String versionStr) throws Exception {
    Method method =
        VersionsCompatibilityChecker.class.getDeclaredMethod("parseVersion", String.class);
    method.setAccessible(true);
    assertThrows(InvocationTargetException.class, () -> method.invoke(null, versionStr));
  }

  private static Stream<Object[]> versionCompatibilityProvider() {
    return Stream.of(
        new Object[] {"1.9.3.dev0", "2.8.1.dev12-something", false},
        new Object[] {"1.9", "2.8", false},
        new Object[] {"1", "2", false},
        new Object[] {"1.9.0", "2.9.0", false},
        new Object[] {"1.1.0", "1.2.9", true},
        new Object[] {"1.2.7", "1.1.8.dev0", true},
        new Object[] {"1.2.1", "1.2.29", true},
        new Object[] {"1.2.0", "1.2.0", true},
        new Object[] {"1.2.0", "1.4.0", false},
        new Object[] {"1.4.0", "1.2.0", false},
        new Object[] {"1.9.0", "3.7.0", false},
        new Object[] {"3.0.0", "1.0.0", false},
        new Object[] {"", "1.0.0", false},
        new Object[] {"1.0.0", "", false},
        new Object[] {"", "", false});
  }

  @ParameterizedTest
  @MethodSource("versionCompatibilityProvider")
  public void testIsCompatible(String clientVersion, String serverVersion, boolean expected) {
    assertEquals(expected, VersionsCompatibilityChecker.isCompatible(clientVersion, serverVersion));
  }
}
