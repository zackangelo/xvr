package com.avian.xvr.media.sphinx;

import edu.cmu.sphinx.linguist.acoustic.*;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.LogMath;
import edu.cmu.sphinx.util.props.PropertySheet;

import java.util.*;
/**
 * Thoughts on pooled recognition:
 * Ideally, we'd like to build pooling into the sphinx
 * end pointer. When actual speech is detected in the live ep (via the SpeechClassifier component), we'd load
 * the grammar and invoke a borrowed recognizer, then stream 
 * the audio into it. When the speech stops, we'd return it.
 * 
 * Right now we just have a pool of recognizers that 
 * share a language model (the part the hogs the most
 * RAM). When a call comes in, we borrow a recognizer. 
 * When the call ends, we return it. 
 * 
 * @author zangelo
 *
 */
public class SphinxRecognizerPool {
//    <component name="logMath" type="edu.cmu.sphinx.util.LogMath">
//    <property name="logBase" value="1.0001"/>
//    <property name="useAddTable" value="true"/>
//</component>
	List<Recognizer> pool;
	
	//acoustic model components
	UnitManager unitManager;
	LogMath		logMath;
	
	public void initialize() { 
		unitManager = new UnitManager();
		
//		unitManager.newProperties(new PropertySheet());
	}
	
	public synchronized Recognizer borrow() { 
		return null;
	}
	
	public synchronized void restore(Recognizer r) { 
		
	}
}
