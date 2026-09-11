package com.nnp.common.config.exception;


import java.io.Serial;

public class ConfigServiceException extends RuntimeException {


	@Serial
    private static final long serialVersionUID = -3316091900555343931L;

	public ConfigServiceException() {
		super();
	}

	public ConfigServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigServiceException(String message) {
		super(message);
	}

	public ConfigServiceException(Throwable cause) {
		super(cause);
	}

}
