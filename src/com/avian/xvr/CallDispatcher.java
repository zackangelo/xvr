/*
 * Created on Apr 6, 2005
 *
 */
package com.avian.xvr;

import org.apache.log4j.Logger;

import com.avian.xvr.signaling.ISignalingChannel;
import com.avian.xvr.signaling.ISignalingEventHandler;

/**
 * @author Zack
 *
 */
public class CallDispatcher implements ISignalingEventHandler {
	private Logger logger;
	public CallDispatcher() { 
		logger = Logger.getLogger(this.getClass());
	}
	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingEventHandler#onIncomingConnection(com.avian.xvr.signaling.ISignalingChannel)
	 */
	public void onIncomingConnection(ISignalingChannel chan) {
		
	}
}
