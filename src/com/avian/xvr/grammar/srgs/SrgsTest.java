package com.avian.xvr.grammar.srgs;

import java.io.IOException;
import java.net.URL;


import com.avian.xvr.grammar.srgs.sphinx.SrgsGrammar;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

public class SrgsTest {

	public static void main(String[] args) { 
		Recognizer recognizer;
		SrgsGrammar srgsGrammar;
		Microphone microphone;
		
		URL url = SrgsTest.class.getResource("config.xml");
        ConfigurationManager cm;
        
        System.out.println("Sphinx SRGS Demo");
        
		try {
			cm = new ConfigurationManager(url);

			recognizer = (Recognizer) cm.lookup("recognizer");
	        srgsGrammar = (SrgsGrammar) cm.lookup("srgsGrammar");
	        microphone = (Microphone) cm.lookup("microphone");

	        System.out.println("Allocating...");
	        recognizer.allocate();
	        
	        srgsGrammar.dumpRandomSentences(10);
	        
	        microphone.startRecording();
	        
	        while(true) { 
	        	System.out.println("Recognizing...");
	        	Result result = recognizer.recognize();
	        	
	        	if(result != null)
	        		System.out.println(result.getBestResultNoFiller());
	        }
	        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertyException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		
	}
}
