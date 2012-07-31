package com.avian.xvr.sandbox;

import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.media.sapi.Sapi5Synthesizer;
import com.avian.xvr.resource.IAudioResource;

public class SapiTtsTest {
	public static void main(String[] args) { 
		ISynthesizer synth = new Sapi5Synthesizer();
		
		System.out.println("Invoking synthesizer...");
		IAudioResource res = 
			synth.getSynthesizedText("Test synthesis.");
		
		int bufSize = res.getSampleData().remaining();
		
		Integer i = new Integer(4095);
		
		System.out.println("Got " + bufSize + " bytes of audio.");
	}
}
