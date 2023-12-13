package io.qdrant.client;

/**
 * An exception when interacting with qdrant
 */
public class QdrantException extends RuntimeException {
	/**
	 * Instantiates a new instance of {@link QdrantException}
	 * @param message The exception message
	 */
	public QdrantException(String message) {
        super(message);
    }
}
