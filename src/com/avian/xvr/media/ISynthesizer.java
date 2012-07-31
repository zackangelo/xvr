/*
 * Created on Apr 4, 2005
 *
 */
package com.avian.xvr.media;

import com.avian.xvr.resource.IAudioResource;

/**
 * @author Zack
 *
 */
public interface ISynthesizer {
	public final static byte IDLE = 0;
	public final static byte SYNTHESIZING = 1;

	public IAudioResource getSynthesizedText(String text);
	public void cancel();
	public byte getStatus();
	public void init();
}
