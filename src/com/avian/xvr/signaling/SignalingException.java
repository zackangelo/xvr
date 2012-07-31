/*
 * Created on Apr 5, 2005
 *
 */
package com.avian.xvr.signaling;

/**
 * @author Zack
 *
 */
public class SignalingException extends Exception {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public SignalingException(String message) {
		super(message);
	}

	public SignalingException(String message, Throwable cause) {
		super(message, cause);
	}

}
