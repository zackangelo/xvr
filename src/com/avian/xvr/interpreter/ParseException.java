package com.avian.xvr.interpreter;

public class ParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException(String message,Throwable root) { 
		super(message,root);
	}
	
	public ParseException(String message) { 
		super(message);
	}
}
