/*
 * Created on Apr 5, 2005
 *
 */
package com.avian.xvr.signaling;

/**
 * @author Zack
 *
 */
public interface ISignalingProvider {
	public void init(ISignalingEventHandler seh) throws SignalingException;
	public void destroy();
	
	public void listenForConnections() throws SignalingException;
	public void stopListening();
	
}
