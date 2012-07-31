/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.signaling;

import com.avian.xvr.media.IMediaChannel;


/**
 * @author Zack
 *
 */
public interface ISignalingChannel {
	//status values
	public final static int RINGING = 0;
	public final static int ACTIVE = 1;
	public final static int DISCONNECTED = 2;

	public void init() throws SignalingException;
	public void destroy();
	
	public void hangup() throws SignalingException;
	public void answer() throws SignalingException;
	public IMediaChannel getMediaChannel() throws SignalingException;
	public void transfer(String destination) throws SignalingException;
	
	public int getStatus();
}
