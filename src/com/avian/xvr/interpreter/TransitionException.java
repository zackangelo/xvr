package com.avian.xvr.interpreter;

public class TransitionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String target;
	public TransitionException(String target) { 
		this.target = target;
	}
	
	public String getTarget() { 
		return this.target;
	}
}
