package uk.co.datumedge.autumn;

/**
 * Thrown when a class could not be singletonised.
 */
public class SingletoniseException extends RuntimeException {
	private static final long serialVersionUID = 8150198722641942662L;

	public SingletoniseException(String message) {
		super(message);
	}

	public SingletoniseException(Throwable cause) {
		super(cause);
	}
}
