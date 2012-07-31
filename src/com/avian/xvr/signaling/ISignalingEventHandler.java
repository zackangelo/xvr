/*
 * Created on Apr 5, 2005
 *
 */
package com.avian.xvr.signaling;

/**
 * @author Zack
 *
 */
public interface ISignalingEventHandler {
	public void onIncomingConnection(ISignalingChannel chan);
}
