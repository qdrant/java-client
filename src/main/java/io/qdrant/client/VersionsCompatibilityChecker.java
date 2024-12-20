package io.qdrant.client;

import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Version {
  private final int major;
  private final int minor;
  private final String rest;

  public Version(int major, int minor, String rest) {
    this.major = major;
    this.minor = minor;
    this.rest = rest;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public String getRest() {
    return rest;
  }
}

/** Utility class to check compatibility between server's and client's versions. */
public class VersionsCompatibilityChecker {
  private static final Logger logger = LoggerFactory.getLogger(VersionsCompatibilityChecker.class);

  /** Default constructor. */
  public VersionsCompatibilityChecker() {}

  private static Version parseVersion(String version) throws IllegalArgumentException {
    if (version.isEmpty()) {
      throw new IllegalArgumentException("Version is None");
    }

    try {
      String[] parts = version.split("\\.");
      int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
      int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
      String rest =
          parts.length > 2
              ? String.join(".", new ArrayList<>(Arrays.asList(parts).subList(2, parts.length)))
              : "";

      return new Version(major, minor, rest);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Unable to parse version, expected format: x.y.z, found: " + version, e);
    }
  }

  /**
   * Compares server's and client's versions.
   *
   * @param clientVersion The client's version.
   * @param serverVersion The server's version.
   * @return True if the versions are compatible, false otherwise.
   */
  public static boolean isCompatible(String clientVersion, String serverVersion) {
    if (clientVersion.isEmpty()) {
      logger.warn("Unable to compare with client version {}", clientVersion);
      return false;
    }

    if (serverVersion.isEmpty()) {
      logger.warn("Unable to compare with server version {}", serverVersion);
      return false;
    }

    if (clientVersion.equals(serverVersion)) {
      return true;
    }

    try {
      Version parsedServerVersion = parseVersion(serverVersion);
      Version parsedClientVersion = parseVersion(clientVersion);

      int majorDiff = Math.abs(parsedServerVersion.getMajor() - parsedClientVersion.getMajor());
      if (majorDiff >= 1) {
        return false;
      }
      return Math.abs(parsedServerVersion.getMinor() - parsedClientVersion.getMinor()) <= 1;
    } catch (IllegalArgumentException e) {
      logger.warn("Unable to compare versions: {}", e.getMessage());
      return false;
    }
  }
}
