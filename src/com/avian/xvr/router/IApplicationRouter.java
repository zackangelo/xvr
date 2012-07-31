/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.avian.xvr.router;

/**
 * @author Zack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IApplicationRouter {
	public String getApplicationUrlForDnis(String dnis);
	public String getApplicationUrlForSipAddress(String address);
}
