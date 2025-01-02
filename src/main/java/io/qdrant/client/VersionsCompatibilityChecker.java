package io.qdrant.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Version {
  private final int major;
  private final int minor;

  public Version(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
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

      return new Version(major, minor);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Unable to parse version, expected format: x.y[.z], found: " + version, e);
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
    try {
      Version client = parseVersion(clientVersion);
      Version server = parseVersion(serverVersion);

      if (client.getMajor() != server.getMajor()) return false;
      return Math.abs(client.getMinor() - server.getMinor()) <= 1;

    } catch (IllegalArgumentException e) {
      logger.warn("Version comparison failed: {}", e.getMessage());
      return false;
    }
  }
}
