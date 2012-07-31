/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.media.rtp;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import com.avian.iaf.filters.IFilterGraph;
import com.avian.iaf.filters.InvalidGraphOperationException;
import com.avian.iaf.filters.MuLawToPcmDecoderFilter;
import com.avian.iaf.filters.NullSinkFilter;
import com.avian.iaf.filters.Rfc2833DecoderFilter;
import com.avian.iaf.filters.RtpReceiverSourceFilter;
import com.avian.iaf.filters.ShortToDoubleConverterFilter;
import com.avian.iaf.filters.SpeakerSinkFilter;
import com.avian.xvr.media.RecognizerResult;
import com.avian.xvr.media.sphinx.SphinxRecognizerFilter;
import com.avian.xvr.media.sphinx.SphinxRecognizerThread;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;

/**
 * @author Zack
 *
 */
public class InboundRtpFilterGraph implements IFilterGraph {
	private RtpReceiverSourceFilter rtpSource;
	private MuLawToPcmDecoderFilter mulawDecoder;
	private Rfc2833DecoderFilter dtmfDecoder;
	private NullSinkFilter nullSink;
	private SpeakerSinkFilter speaker;
	private SphinxRecognizerFilter sphinx;
	
	private Properties rtpProps;
	private ShortToDoubleConverterFilter shortToDoubleConv;
	
	private SphinxRecognizerThread srt;
	
	public InboundRtpFilterGraph(byte encoding,String iface,int port) { 
		rtpSource = new RtpReceiverSourceFilter();
		mulawDecoder = new MuLawToPcmDecoderFilter();
		shortToDoubleConv = new ShortToDoubleConverterFilter();
		
		speaker = new SpeakerSinkFilter();
		nullSink = new NullSinkFilter();
		
		dtmfDecoder = new Rfc2833DecoderFilter();
		
		rtpProps = new Properties();
		rtpProps.put("address",iface);
		rtpProps.put("port",""+port);
	}

	public static class SphinxSerializationWrapper implements java.io.Serializable {
		ConfigurationManager config;
	}
	
	//TODO replace with factory class (which'll be specified in config)
	private void createRecognizer() throws IOException, PropertyException, InstantiationException { 
		URL url = SphinxRecognizerFilter.class.getResource("sphinx.config.xml");

		// recognizer filter has to be created here so it'll get 
		// inserted into the sphinx pipeline
		ConfigurationManager cm = new ConfigurationManager(url);
		Recognizer reco = (Recognizer) cm.lookup("recognizer");
		sphinx = (SphinxRecognizerFilter) cm.lookup("iafFilter");
		reco.allocate();
		
//		FileOutputStream fos = new FileOutputStream("sphinx.ser");
//		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
//		SphinxSerializationWrapper wrapper = new SphinxSerializationWrapper();
//		wrapper.config = cm;
//		oos.writeObject(wrapper);
//		oos.close();
		
//
//		java.util.logging.Logger.getLogger("edu.cmu.sphinx.recognizer.Recognizer").setLevel(java.util.logging.Level.FINER);
//
		srt = new SphinxRecognizerThread(reco);
		dtmfDecoder.setHandler(srt);
		srt.start();
	}
	
	
	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilterGraph#init()
	 */
	public void init() {
		try {
			createRecognizer();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (PropertyException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		}
		
		// wire the graph
		try {
			rtpSource.ulawPin.connect(mulawDecoder.inPin);
			rtpSource.dtmfPin.connect(dtmfDecoder.inPin);
//			mulawDecoder.outPin.connect(speaker.inPin);
			mulawDecoder.outPin.connect(shortToDoubleConv.inPin);
			shortToDoubleConv.outPin.connect(sphinx.inPin);
		} catch (InvalidGraphOperationException e) {
			e.printStackTrace();
			return;
		}
		
		//source goes last, sink first
//		speaker.initialize(null);
		sphinx.initialize(null);
		mulawDecoder.initialize(null);
//		nullSink.initialize(null);
		shortToDoubleConv.initialize(null);
		rtpSource.initialize(rtpProps);
		
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilterGraph#start()
	 */
	public void start() {
		//rtpSource.initialize(rtpProps);	
	}
	
	public RecognizerResult waitForResult() { 
		return srt.waitForResult();
	}
}
