package io.qdrant.client;

/** An exception indicating that rate limit is reached */
public class ResourceExhaustedResponse extends RuntimeException {
  /** The number of seconds to wait before retrying */
  private final int retryAfterSeconds;

  /**
   * Instantiates a new instance of {@link ResourceExhaustedResponse}
   *
   * @param message The message to display
   * @param retryAfterSeconds The number of seconds to wait before retrying
   */
  public ResourceExhaustedResponse(String message, int retryAfterSeconds) {
    super(message);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  /**
   * Gets the number of seconds to wait before retrying
   *
   * @return the number of seconds to wait before retrying
   */
  public int getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
