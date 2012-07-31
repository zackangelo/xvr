/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.interpreter;

import org.apache.log4j.Logger;

import com.avian.xvr.media.IMediaChannel;
import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.resource.IAudioResource;
import com.avian.xvr.resource.IResourceFetcher;
import com.avian.xvr.resource.ResourceFetchException;
import com.avian.xvr.signaling.ISignalingChannel;
import com.avian.xvr.signaling.SignalingException;

/**
 * @author Zack
 *
 */
public class Interpreter {
	//Parent session
	Interpreter parent;
	
	private ISignalingChannel sigChannel;
	private IMediaChannel mediaChannel;
	private IResourceFetcher fetcher;
	private ISynthesizer synth;
	private Logger logger;
	
	public Interpreter(Interpreter parent,IMediaChannel mediaChannel,
			ISignalingChannel signalingChannel,IResourceFetcher resFetcher,
			ISynthesizer synth) { 
		this.parent = parent;
		this.sigChannel = signalingChannel;
		this.mediaChannel = mediaChannel;
		this.fetcher = resFetcher;
		this.synth = synth;
		
		logger = Logger.getLogger(this.getClass());
	}
	
	public void handleQueueAudio(Operation op) throws ResourceFetchException { 
		String url = op.getParam("url");
		
		logger.info("Fetching audio resource @ " + url);
	
		IAudioResource audio = fetcher.fetchAudio(url);
		mediaChannel.queueAudio(audio);
	}
	
	public void handleHangup(Operation op) {
		try {
			mediaChannel.playAudio();
			sigChannel.hangup();
		} catch (SignalingException e) {
			e.printStackTrace();
		}
	}
	
	public void handleQueueSynth(Operation op) { 
		String text = op.getParam("text");
		logger.info("TTS Synth: " + text);
		
		IAudioResource audio = synth.getSynthesizedText(text);
		mediaChannel.queueAudio(audio);
	}
	
	public void execute(Scope scope) { 
		logger.debug("execute()");
		
		try {
			for(Operation op : scope.getOperations()) {
				int type = op.getType();
				switch(type) { 
				case Operation.QUEUE_AUDIO:
					handleQueueAudio(op);
					break;
				case Operation.QUEUE_SYNTH:
					handleQueueSynth(op);
					break;
				case Operation.HANGUP:
					handleHangup(op);
					break;
				}
			}
		} catch(ResourceFetchException e) { 
			e.printStackTrace();
		}
		
		logger.debug("end execute()");
	}
}
