package uk.co.datumedge.autumn;

public class BindException extends RuntimeException {
	public BindException(String message) {
		super(message);
	}

	public BindException(Throwable cause) {
		super(cause);
	}
}
