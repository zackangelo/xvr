/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.resource;

import java.nio.ByteBuffer;

/**
 * @author Zack
 *
 */
public interface IAudioResource {

	public final static byte PCM_ENCODING = 0;
	public final static byte ULAW_ENCODING = 1;
	public final static byte ALAW_ENCODING = 2;
	public final static byte GSM_ENCODING = 3;
	
	public ByteBuffer getSampleData();
	
	public float getSampleRate();
	public int getSampleSize();
	public byte getEncoding();
	public boolean isBigEndian();
	public String getUrl();
	public void setUrl(String url);
}
