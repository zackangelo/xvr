package com.avian.xvr.grammar;

public class GrammarParseException extends Exception {
	public GrammarParseException(String message,Throwable cause) { 
		super(message,cause);
	}
	
	public GrammarParseException(String message) { 
		super(message);
	}
}
