package org.tarantool.core.exception;

/**
 * A remote server error with error code and message.
 */
public class TarantoolException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	int code;

	public int getCode() {
		return code;
	}

	public TarantoolException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;

	}

	public TarantoolException(int code, String message) {
		super(message);
		this.code = code;

	}

}
