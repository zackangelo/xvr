/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.avian.xvr.signaling.ISignalingEventHandler;
import com.avian.xvr.signaling.ISignalingProvider;
import com.avian.xvr.signaling.SignalingException;
import com.avian.xvr.signaling.sip.SipSignalingProvider;

/**
 * @author Zack
 * 
 */
public class Bootstrap {
	public static void printHeader() { 
		System.out.println("Avian XVR Server " + IGlobalConstants.VERSION);
		System.out.println("Copyright (c) Avian 2005. All rights reserved.\n");
	}
	
	private static int getLocalPort() { 
		return 5060;
	}
	
	private static String getLocalIpAddress() throws XvrException { 
//		try {
//	        InetAddress addr = InetAddress.getLocalHost();
//	        return addr.getHostAddress();
//	    } catch (UnknownHostException e) {
//	    }

		return "127.0.0.1";
	}
	public static void main(String[] args) {
		//Configure Log4J
		BasicConfigurator.configure();
		Logger logger = Logger.getLogger(Bootstrap.class);
		
		printHeader();
		
		logger.info("Starting up...");
		
//		//build channels
//		IResourceFetcher rf = new BaseResourceFetcher();
//		IMediaChannel mc = new RtpMediaChannel("127.0.0.1", 9004, "127.0.0.1", 8000);
//		
//		mc.init();
//		rf.init();
//	
//		VxmlParser parser = new VxmlParser();
//		
//		IDocumentResource doc;

//		try {
//			doc = rf.fetchDocument("res/xml/sayprompt.xml");
////			doc = rf.fetchDocument("res/xml/playaudio.xml");
//	        Scope root = parser.parse(doc);
//	        ISynthesizer synth = new FreeTtsSynthesizer();
//	        Interpreter in = new Interpreter(null,mc,null,rf,synth);
//	        in.execute(root);
//		} catch (ResourceFetchException e) {
//			e.printStackTrace();
//		}
	
		
		try {
			ISignalingProvider sigProv = new SipSignalingProvider(getLocalIpAddress(),getLocalPort());
			ISignalingEventHandler dispatcher = new CallDispatcher();

			sigProv.init(dispatcher);
			sigProv.listenForConnections();
		} catch (SignalingException e) {
			e.printStackTrace();
			return;
		} catch (XvrException e) {
			e.printStackTrace();
			return;
		}

		while(true) {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
//
		
//		IResourceFetcher rf = new BaseResourceFetcher();
//		rf.init();
//		VxmlParser parser = new VxmlParser();
//		IDocumentResource doc;
//		try {
//			doc = rf.fetchDocument("res/xml/sayprompt.xml");
//			parser.parse(doc);
//		} catch (ResourceFetchException e) {
//			e.printStackTrace();
//		}
		
		
	}
}
