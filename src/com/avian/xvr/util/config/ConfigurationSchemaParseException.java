package com.avian.xvr.util.config;

public class ConfigurationSchemaParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationSchemaParseException(String message,Throwable cause) { 
		super(message,cause);
	}
	
	public ConfigurationSchemaParseException(String message) { 
		super(message);
	}
}
