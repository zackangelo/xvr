package com.avian.xvr.interpreter;

import com.avian.xvr.XvrException;

public class InterpreterException extends XvrException {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	public InterpreterException(String message) { 
		super(message);
	}

	public InterpreterException(String message, Throwable cause) {
		super(message, cause);
	}
}
