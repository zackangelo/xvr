package com.avian.xvr.media.sapi;

import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.resource.BaseAudioResource;
import com.avian.xvr.resource.IAudioResource;
import java.nio.ByteBuffer;

public class Sapi5Synthesizer implements ISynthesizer {

	static { 
		System.loadLibrary("xvrsapi");
	}
	
	private native void synthToBuffer(String text,ISapiStreamHandler handler);
	
	public IAudioResource getSynthesizedText(String text) {
		ByteBufferStreamHandler bbsh = new ByteBufferStreamHandler();
		synthToBuffer("test",bbsh);
		
		ByteBuffer buf = bbsh.getBuffer();
		byte[] arr = new byte[buf.remaining()];
		buf.get(arr);
		
		BaseAudioResource bar = new BaseAudioResource(arr,8000.0f,8,IAudioResource.PCM_ENCODING);
		return bar;
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public byte getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void init() {
		// TODO Auto-generated method stub

	}

}
