package com.avian.xvr.vxml;

import com.avian.xvr.interpreter.ParseException;;

public class VxmlParseException extends ParseException {
	public VxmlParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VxmlParseException(String message) {
		super(message);
	}

}
