/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.avian.xvr.router;

import java.util.*;

/**
 * @author Zack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HashtableApplicationRouter implements IApplicationRouter {
	Map<String,String> routeMap;
	
	private static HashtableApplicationRouter router;
	
	private HashtableApplicationRouter() { 
		routeMap = new Hashtable<String,String>();
		
		routeMap.put("5551212","res/xml/playaudio.xml");
		routeMap.put("5551313","res/xml/sayprompt.xml");
	}
	
	public static HashtableApplicationRouter getInstance() { 
		if(router == null) { 
			router = new HashtableApplicationRouter();
		}
		
		return router;
	}
	/* (non-Javadoc)
	 * @see com.avian.xvr.router.IApplicationRouter#getApplicationUrlForDnis(java.lang.String)
	 */
	public String getApplicationUrlForDnis(String dnis) {
		return routeMap.get(dnis);
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.router.IApplicationRouter#getApplicationUrlForSipAddress(java.lang.String)
	 */
	public String getApplicationUrlForSipAddress(String address) {
		return null;
	}

}
