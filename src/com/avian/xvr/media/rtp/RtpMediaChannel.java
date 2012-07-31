/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.media.rtp;

import org.apache.log4j.Logger;

import com.avian.xvr.media.IMediaChannel;
import com.avian.xvr.media.RecognizerResult;
import com.avian.xvr.resource.IAudioResource;

/**
 * @author Zack
 *
 */
public class RtpMediaChannel implements IMediaChannel {
	InboundRtpFilterGraph inRtp;
	OutboundRtpFilterGraph outRtp;
	Logger logger;
	public static final int XMIT_CHUNK_SIZE = 320;
	
	public RtpMediaChannel(String localAddr,int localPort,
							String remoteAddr,int remotePort) {
		inRtp = new InboundRtpFilterGraph(IAudioResource.ULAW_ENCODING,
				localAddr,localPort);
		outRtp = new OutboundRtpFilterGraph(IAudioResource.ULAW_ENCODING,
				XMIT_CHUNK_SIZE,remoteAddr,remotePort);
		logger = Logger.getLogger(this.getClass());
	}
	/* (non-Javadoc)
	 * @see com.avian.xvr.media.IMediaChannel#queueAudio(com.avian.xvr.resource.IAudioResource)
	 */
	public void queueAudio(IAudioResource ar) {
		logger.info("Queueing " + ar.getSampleData().remaining() + " bytes of audio @ " + ar.getUrl());
		outRtp.prompter.bufferAudio(ar);
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.media.IMediaChannel#playAudio()
	 */
	public void playAudio() {
		outRtp.prompter.playBuffer();
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.media.IMediaChannel#init()
	 */
	public void init() {
		inRtp.init();
		outRtp.init();
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.media.IMediaChannel#destroy()
	 */
	public void destroy() {
	}
	
	public RecognizerResult waitForInput() {
		return inRtp.waitForResult();
	}
	
	public void startRecording() {
		// TODO Auto-generated method stub
		
	}
	public IAudioResource stopRecording() {
		// TODO Auto-generated method stub
		return null;
	}

}
