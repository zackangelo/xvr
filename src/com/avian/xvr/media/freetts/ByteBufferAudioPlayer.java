/*
 * Created on Apr 4, 2005
 *
 */
package com.avian.xvr.media.freetts;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.audio.AudioPlayer;
import java.util.*;
/**
 * @author Zack
 *
 */
public class ByteBufferAudioPlayer implements AudioPlayer {

	AudioFormat format;
	
	ByteBuffer buffer,outputBuffer;
	List<ByteBuffer> bufferList;
	int totalBufferSize;
	
	public ByteBufferAudioPlayer() { 
		bufferList = new ArrayList<ByteBuffer>();
		totalBufferSize = 0;
	}
	
	long firstSampleTime;
	
	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#setAudioFormat(javax.sound.sampled.AudioFormat)
	 */
	public void setAudioFormat(AudioFormat format) {
		this.format = format;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#getAudioFormat()
	 */
	public AudioFormat getAudioFormat() {
		return format;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#pause()
	 */
	public void pause() { }

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#resume()
	 */
	public void resume() {}

	/**
	 * FreeTTS calls this method when it's ready to process a new set of utterances.
	 * 
	 * (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#reset()
	 */
	public void reset() {}

	/**
	 * FreeTTS calls this method when it's ready for the entire set of utterances to be played.
	 *  (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#drain()
	 */
	public boolean drain() {
		ByteBuffer concatBuffer = ByteBuffer.allocate(totalBufferSize);
		for(ByteBuffer buf : bufferList) { 
			concatBuffer.put(buf);
		}
		
		concatBuffer.limit(concatBuffer.position());
		concatBuffer.position(0);
		
		outputBuffer = concatBuffer;
		
		return true;
	}

	/**
	 * FreeTTS calls this method when it's ready to process a single utterance.
	 * 
	 *  (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#begin(int)
	 */
	public void begin(int size) {
		buffer = ByteBuffer.allocate(size);
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#end()
	 */
	public boolean end() {
		buffer.limit(buffer.position());
		totalBufferSize += buffer.position();
		buffer.position(0);
		bufferList.add(buffer);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#cancel()
	 */
	public void cancel() {	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#close()
	 */
	public void close() {	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#getVolume()
	 */
	public float getVolume() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#setVolume(float)
	 */
	public void setVolume(float arg0) {	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#getTime()
	 */
	public long getTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#resetTime()
	 */
	public void resetTime() { }

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#startFirstSampleTimer()
	 */
	public void startFirstSampleTimer() {
		firstSampleTime = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#write(byte[])
	 */
	public boolean write(byte[] data) {
		buffer.put(data);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#write(byte[], int, int)
	 */
	public boolean write(byte[] data, int ofs, int len) {
		buffer.put(data,ofs,len);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.sun.speech.freetts.audio.AudioPlayer#showMetrics()
	 */
	public void showMetrics() {	}
	
	public ByteBuffer getBuffer() { 
		return outputBuffer;
	}

}
