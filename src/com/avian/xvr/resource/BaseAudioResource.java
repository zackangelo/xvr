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
public class BaseAudioResource implements IAudioResource {

	ByteBuffer samples;
	float rate;
	int size;
	byte encoding;
	String source;
	boolean bigEndian;
	
	public BaseAudioResource(byte[] samples,float rate,int size,byte encoding) {
		this.samples = ByteBuffer.wrap(samples);
		this.rate = rate;
		this.size = size;
		this.encoding = encoding;
	}
	
	public BaseAudioResource(ByteBuffer samples,float rate,int size,byte encoding) { 
		this.samples = samples;
		this.rate = rate;
		this.size = size;
		this.encoding = encoding;
	}
	
	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IAudioResource#getSampleData()
	 */
	public ByteBuffer getSampleData() {
		return this.samples;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IAudioResource#getSampleRate()
	 */
	public float getSampleRate() {
		return this.rate;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IAudioResource#getSampleSize()
	 */
	public int getSampleSize() {
		return this.size;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IAudioResource#getEncoding()
	 */
	public byte getEncoding() {
		return this.encoding;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IAudioResource#getSource()
	 */
	public String getUrl() {
		return source;
	}
	
	/**
	 * @param url
	 */
	public void setUrl(String source) {
		this.source = source;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}
	public void setBigEndian(boolean bigEndian) {
		this.bigEndian = bigEndian;
	}
}
