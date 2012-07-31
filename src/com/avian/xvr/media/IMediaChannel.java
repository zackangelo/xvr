/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.media;

import com.avian.xvr.resource.IAudioResource;

/**
 * @author Zack
 *
 */
public interface IMediaChannel {
	/**
	 * Queues audio for playback. Non-blocking.
	 * @param ar Audio resource to queue.
	 */
	public void queueAudio(IAudioResource ar);
	
	/**
	 * Instructs this channel to begin playing queued audio. Non-blocking.
	 * 
	 */
	public void playAudio();
	
	/**
	 * Initialize this media channel.
	 *
	 */
	public void init();
	
	/**
	 * Destroy this media channel.
	 *
	 */
	public void destroy();
	
	public RecognizerResult waitForInput();
	
	/**
	 * Starts recording inbound audio data.
	 *
	 */
	public void startRecording();
	
	/**
	 * Stops recording inbound audio data.
	 * 
	 * @return The audio resource containing the recorded audio data.
	 */
	public IAudioResource stopRecording();
}
