/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.media.rtp;

import java.util.Properties;

import com.avian.iaf.filters.IFilterGraph;
import com.avian.iaf.filters.InvalidGraphOperationException;
import com.avian.iaf.filters.PcmToMuLawEncoderFilter;
import com.avian.iaf.filters.RtpTransmitterSinkFilter;

/**
 * @author Zack
 *
 */
public class OutboundRtpFilterGraph implements IFilterGraph {
	public PromptBufferSourceFilter prompter;
	PcmToMuLawEncoderFilter encoder;
	RtpTransmitterSinkFilter rtpSink;
	
	Properties prompterProps,rtpProps;
	
	public OutboundRtpFilterGraph(byte encoding,int chunkSize,String iface,int port) { 
		prompter = new PromptBufferSourceFilter();
		encoder = new PcmToMuLawEncoderFilter();
		rtpSink = new RtpTransmitterSinkFilter();	
	
		prompterProps = new Properties();
		prompterProps.put(PromptBufferSourceFilter.CHUNK_SIZE,chunkSize+"");
		
		rtpProps = new Properties();
		rtpProps.put("address",iface);
		rtpProps.put("port",""+port);
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilterGraph#init()
	 */
	public void init() {
		prompter.initialize(prompterProps);
		encoder.initialize(null);
		rtpSink.initialize(rtpProps);
		
		try {
			prompter.outPin.connect(encoder.inPin);
			encoder.outPin.connect(rtpSink.inPin);
		} catch (InvalidGraphOperationException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilterGraph#start()
	 */
	public void start() {}
	
	
}
