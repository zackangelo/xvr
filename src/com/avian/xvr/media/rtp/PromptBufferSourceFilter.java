/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.media.rtp;

import java.util.Properties;

import com.avian.iaf.filters.*;
import com.avian.xvr.resource.IAudioResource;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;

/**
 * @author Zack
 *
 */
public class PromptBufferSourceFilter extends BaseSourceFilter {
	public IOutputPin outPin;
	
	//256K prompt buffer
	public static final int BUFFER_SIZE = 262144;
	
	ByteBuffer buffer;
	ByteBuffer chunkBuffer;
	int chunkSize;
	Logger logger;

	
	private static final float BUFFER_SAMPLE_RATE = 8000.0f;
	private static final int   BUFFER_SAMPLE_SIZE = 16;
	private static final byte BUFFER_ENCODING = IAudioResource.PCM_ENCODING;
	
	static AudioFormat bufferFmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			8000.0f,	//sampling rate
			16,			//bits per sample
			1,			//channels
			2,			//frame size
			8000.0f,	//frame rate
			true);		//endianness 
	
	public static final String CHUNK_SIZE = "chunkSize";

	
	public PromptBufferSourceFilter() { 
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		outPin = new MemoryOutputPin(this);
		logger = Logger.getLogger(this.getClass());
	}
	
	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.ISourceFilter#generate()
	 */
	public int generate() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		String test = p.getProperty(CHUNK_SIZE);
		chunkSize = Integer.parseInt(test);
		chunkBuffer = ByteBuffer.allocate(chunkSize);
		return true;
	}

	public void bufferAudio(IAudioResource audio) { 
		//thoughts: instead of doing audio conversion here, what about
		//when we stream? have a generator filter that takes IAudioResources
		//as inputs and then streams them along in the appropriate format?
		
		//determine if we need to do a sample rate conversion before
		//putting it in the buffer
		
//		if( (audio.getSampleRate() != BUFFER_SAMPLE_RATE) ||
//			(audio.getSampleSize() != BUFFER_SAMPLE_SIZE) || 
//			(audio.getEncoding() != BUFFER_ENCODING)) {
//			
//			logger.info("Sample rate conversion: " + 
//					audio.getSampleRate() + "hz/" + 
//					audio.getSampleSize() + "bit -> " + 
//					BUFFER_SAMPLE_RATE + "hz/" + 
//					BUFFER_SAMPLE_SIZE + "bit");
//		} else {
			logger.info("Queuing " + audio.getSampleData().limit() + " bytes of audio...");
			buffer.put(audio.getSampleData());
//		}
	}
	
	public void playBuffer() {
		//TODO block for now, but we'll probably do this operation in another thread
		int bufferSize = buffer.position();
		logger.info("Playing " + bufferSize + " bytes of queued audio.");
		
		buffer.position(0);
		buffer.limit(bufferSize);
		
		int bufferOfs = 0;
		byte[] chunkBuf = new byte[chunkSize];
		
		while(bufferOfs < bufferSize) { 
			int bytesToRead = Math.min(chunkSize,(bufferSize-buffer.position()));
			ByteBuffer graphBuf = MemoryBufferManager.requestBuffer();
			
			buffer.position(bufferOfs);
			
			buffer.get(chunkBuf,0,bytesToRead);
			graphBuf.put(chunkBuf,0,bytesToRead);
			graphBuf.limit(bytesToRead);
			outPin.push(graphBuf);
			
			try { Thread.sleep(15); } catch(Exception e) { e.printStackTrace(); } 
			
			bufferOfs += bytesToRead;
		}
		
//		flush();
		//reset when we're done
		buffer.clear();
	}
	
	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void flush() {
		this.outPin.flush();
	}

}
