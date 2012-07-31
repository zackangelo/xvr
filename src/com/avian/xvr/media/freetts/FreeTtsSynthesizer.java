package com.avian.xvr.media.freetts;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.resource.BaseAudioResource;
import com.avian.xvr.resource.IAudioResource;

/**
 * @author Zack
 *
 */
public class FreeTtsSynthesizer implements ISynthesizer {
	VoiceManager manager;
	Voice voice8k;
	//TODO: add destroy for voice.deallocate()
	public final static String VOICE_8K_NAME = "kevin";
	
	public FreeTtsSynthesizer() { 

	}
	
	
	//TODO have throw init exception
	public void init() {
		manager = VoiceManager.getInstance();
		voice8k = manager.getVoice(VOICE_8K_NAME);
		voice8k.allocate();
	}
	
	/* (non-Javadoc)
	 * @see com.avian.xvr.media.ISynthesizer#getSynthesizedText(java.lang.String)
	 */
	public IAudioResource getSynthesizedText(String text) {
		ByteBufferAudioPlayer player = new ByteBufferAudioPlayer();
		
		voice8k.setAudioPlayer(player);
		voice8k.speak(text);
		
		BaseAudioResource audio = new BaseAudioResource(player.getBuffer(),
				player.getAudioFormat().getSampleRate(),
				player.getAudioFormat().getSampleSizeInBits(),
				IAudioResource.PCM_ENCODING);
		
		return audio;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.media.ISynthesizer#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.media.ISynthesizer#getStatus()
	 */
	public byte getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

}
