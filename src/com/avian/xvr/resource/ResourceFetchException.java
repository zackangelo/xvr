/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.avian.xvr.resource;

import com.avian.xvr.XvrException;

/**
 * @author Zack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResourceFetchException extends XvrException {

	/**
	 * @param message
	 */
	public ResourceFetchException(String message) {
		super(message);
	}
	
	public ResourceFetchException(String message,Throwable root) {
		super(message,root);
	}

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

}
