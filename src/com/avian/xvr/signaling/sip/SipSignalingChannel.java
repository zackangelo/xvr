/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.avian.xvr.signaling.sip;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Vector;

import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.Logger;


import com.avian.xvr.interpreter.InterpreterThread;
import com.avian.xvr.media.IMediaChannel;
import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.media.freetts.FreeTtsSynthesizer;
import com.avian.xvr.media.rtp.RtpMediaChannel;
import com.avian.xvr.media.sapi.Sapi5Synthesizer;
import com.avian.xvr.resource.LocalResourceFetcher;
import com.avian.xvr.resource.IDocumentResource;
import com.avian.xvr.resource.IResourceFetcher;
import com.avian.xvr.resource.ResourceFetchException;
import com.avian.xvr.resource.jakarta.HttpResourceFetcher;
import com.avian.xvr.signaling.ISignalingChannel;
import com.avian.xvr.signaling.SignalingException;
import com.avian.xvr.vxml.VxmlDocument;
import com.avian.xvr.vxml.VxmlInterpreter;
import com.avian.xvr.vxml.VxmlParser;

/**
 * @author Zack
 *
 * thoughts: create two signaling interfaces? outbound and inbound?
 */
public class SipSignalingChannel implements ISignalingChannel {
	RtpMediaChannel mediaChannel;

	//sip specific status identifiers...
	private static final int SIP_RINGING = 5;
	private static final int SIP_WAITING_FOR_INVITE_ACK = 1;
	private static final int SIP_WAITING_FOR_BYE_ACK = 2;
	private static final int SIP_ACTIVE = 3;
	private static final int SIP_CLOSED = 4;

	private int sipStatus;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private AddressFactory addressFactory;
	
	private ContactHeader contactHeader;
	
	private Dialog dialog;
	private Request initialRequest;
	private String ipAddress;
	
	private Logger logger;
	
	private int clientRtpPort;
	private String clientRtpAddr;
	private SipProvider provider;
	private long transactionId;
	
	private Object answerMonitor;
	
	public SipSignalingChannel(Request initialRequest,Dialog dialog,String ipAddress) throws SignalingException { 
		logger = Logger.getLogger(this.getClass());
		try {
			messageFactory = SipFactory.getInstance().createMessageFactory();
			headerFactory = SipFactory.getInstance().createHeaderFactory();
			addressFactory = SipFactory.getInstance().createAddressFactory();
			this.dialog = dialog;
			this.initialRequest = initialRequest;
			
			this.ipAddress = ipAddress;//((SipURI)((ContactHeader)initialRequest.getHeader("Contact")).getAddress().getURI()).getHost();
		 
			this.answerMonitor = new Object();
		} catch (PeerUnavailableException e) {
			throw new SignalingException("Error allocating signaling channel.");
		}
	}
	
	private String getIpAddress() { 
		return ipAddress;
	}
	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#init()
	 */
	public void init() throws SignalingException {
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#hangup()
	 */
	public void hangup() throws SignalingException {
		try {
			Request request = dialog.createRequest(Request.BYE);
			ClientTransaction tx = provider.getNewClientTransaction(request);
			logRequestSent(request);
			dialog.sendRequest(tx);
			setSipStatus(SIP_WAITING_FOR_BYE_ACK);
		} catch (SipException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#transfer(java.lang.String)
	 */
	public void transfer(String destination) throws SignalingException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#getStatus()
	 */
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingChannel#getMediaChannel()
	 */
	public IMediaChannel getMediaChannel() throws SignalingException {
		return mediaChannel;
	}

	private final static String RFC2833_MEDIA_ATTR = "101 telephone-event/8000";
	
	//TODO: get this from the rtp stack 
	private final static String[] SUPPORTED_MEDIA = {
		"0 pcmu/8000",
		"101 telephone-event/8000"
	};
	
	private MediaDescription coalesceMediaTypes(MediaDescription callerMd,int myRtpPort) {
		try {
			SdpFactory sdpFactory = SdpFactory.getInstance();

			int[] fmts = new int[2]; fmts[0] = 0; fmts[1] = 101;
			MediaDescription md = sdpFactory.
					createMediaDescription("audio",myRtpPort,1,"RTP/AVP",fmts);

			Vector<Attribute> attrs = callerMd.getAttributes(false);
			if(attrs == null) return null;
			
			Vector<Attribute> myAttrs = new Vector<Attribute>();
			
			for(String myPayloadType:SUPPORTED_MEDIA) {
				for(Attribute theirPayloadAttr:attrs) {
					if(myPayloadType.compareToIgnoreCase(theirPayloadAttr.getValue()) == 0) {
						myAttrs.add(sdpFactory.createAttribute("rtpmap",myPayloadType));
					}
				}
			}
			
			md.setAttributes(myAttrs);
			
			return md;
		} catch (SdpException e) {
			e.printStackTrace();
		} 	
		
		return null;
	}
	
	private SessionDescription createSdpReplyFromCaller(SessionDescription callerSdp,String ipAddress,int rtpPort)
		throws SdpException {
		SdpFactory sdpFactory = SdpFactory.getInstance();
		SessionDescription mySdp = sdpFactory.createSessionDescription();
		
		mySdp.setVersion(sdpFactory.createVersion(0));
		mySdp.setConnection(sdpFactory.createConnection("IN","IP4",ipAddress));
		mySdp.setSessionName(sdpFactory.createSessionName("com.avian.xvr.Server"));
		
		int originSession = (int)(System.currentTimeMillis()%Integer.MAX_VALUE);
		int originVersion = originSession+1;
		mySdp.setOrigin(sdpFactory.createOrigin("avian-xvr",
				originSession,originVersion,"IN","IP4",ipAddress));
		
		Vector<TimeDescription> timeDescriptions = new Vector<TimeDescription>();
		timeDescriptions.add(sdpFactory.createTimeDescription());
		mySdp.setTimeDescriptions(timeDescriptions);
	
		MediaDescription theirMd = 
			(MediaDescription)callerSdp.getMediaDescriptions(false).get(0);
		
		mySdp.getMediaDescriptions(true).add(coalesceMediaTypes(theirMd,rtpPort));
		
		return mySdp;
	}
	

	
    private void attachToTag(Response response)
    {
        ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
        try {
            if (to.getTag() == null || to.getTag().trim().length() == 0) {
                int toTag = dialog != null? dialog.hashCode():(int)System.currentTimeMillis();
                to.setTag(Integer.toString(toTag));
            }
        }
        catch (ParseException ex) {
			ex.printStackTrace();
        }
    }
	
	private ContactHeader generateContactHeader(String displayName,String host) { 
		Address myAddress;
		try {
			myAddress = addressFactory.createAddress(addressFactory.createSipURI(displayName,host));
			return headerFactory.createContactHeader(myAddress);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Reply created in advance upon INVITE
	 */
	SessionDescription sdpReply;
	Request lastInviteRequest;
	ServerTransaction lastInviteTransaction;
	
	
	public void logRequestSent(Request request) { 
		logger.debug("REQUEST >>> \n" + request);
	}
	public void logResponseSent(Response response) { 
		logger.debug("RESPONSE >>> \n" + response);
	}
	
	VxmlInterpreter interpreter;
	
	private void kickoffInterpreter() {
		IResourceFetcher rf = new HttpResourceFetcher();
		ISynthesizer synth = new FreeTtsSynthesizer();
//		ISynthesizer synth = new Sapi5Synthesizer();
		interpreter = new VxmlInterpreter(this,mediaChannel,rf,synth);
		interpreter.go("http://localhost/southpark.xml");
	}
	
	public void answer() throws SignalingException { 
		try { 
			Response okResponse;
			okResponse = messageFactory.createResponse(Response.OK,lastInviteRequest);
			okResponse.setContent(sdpReply.toString(), headerFactory.createContentTypeHeader("application", "sdp"));
			okResponse.addHeader(contactHeader);
			attachToTag(okResponse);
			
			logResponseSent(okResponse);
			lastInviteTransaction.sendResponse(okResponse);
			
			setSipStatus(SIP_WAITING_FOR_INVITE_ACK);		
			
			//TODO make timeout configurable
			synchronized(answerMonitor) {
				try {
					answerMonitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} catch (SipException e) { 
			throw new SignalingException("Unable to answer call and send OK response.",e);
		} catch (ParseException e) {
			throw new SignalingException("Unable to answer call and send OK response.",e);
		}
	}
	
	public void handleInvite(ServerTransaction transaction,RequestEvent event) throws SignalingException { 
		//save provider
		provider = (SipProvider)event.getSource();
		Request	request = event.getRequest();
		String ipAddress = getIpAddress();
		
		
		ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
		contactHeader = generateContactHeader(toHeader.getAddress().getDisplayName(),ipAddress);
		
		try {
			SessionDescription callerSdp = SdpFactory.getInstance().
				createSessionDescription(new String(request.getRawContent()));
			
			clientRtpPort = ((MediaDescription)callerSdp.getMediaDescriptions(false).get(0)).getMedia().getMediaPort();
			clientRtpAddr = callerSdp.getConnection().getAddress();
			
			int rtpPort = getNextRtpPort();
			sdpReply = createSdpReplyFromCaller(callerSdp,
					ipAddress,rtpPort);
			
			Response ringingResponse;
			
			ringingResponse = messageFactory.createResponse(Response.RINGING,request);
			attachToTag(ringingResponse);
			ringingResponse.addHeader(contactHeader);
			
			transaction.sendResponse(ringingResponse);
			logResponseSent(ringingResponse);
			
			buildRtpMediaChannel(getIpAddress(),rtpPort,clientRtpAddr,clientRtpPort);

			
			//save this request and tx
			lastInviteRequest = request;
			lastInviteTransaction = transaction;
			
			kickoffInterpreter();
		} catch(SdpParseException e) {
			throw new SignalingException("Unable to create session descriptor.",e);
		} catch (SdpException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			throw new SignalingException("Unable to send ringing response.",e);
		} catch (SipException e) {
			throw new SignalingException("Unable to send ringing response.",e);
		}
	}
	
	private static final int RTP_PORTBASE = 9500;
	private static int rtpPortCounter = 0;

	//TODO write a more robust port selection method, possibly
	//		a generic PortFinder class
	private int getNextRtpPort() throws SignalingException {
		rtpPortCounter += 2;
		return RTP_PORTBASE+rtpPortCounter;
	}

	private void buildRtpMediaChannel(String localIp,int localPort,String remoteIp,int remotePort) {
		//TODO replace with media channel factory which builds a media channel based on config
		mediaChannel = new RtpMediaChannel(localIp,localPort,remoteIp,remotePort);
	}
	
	public void handleAck(ServerTransaction transaction,RequestEvent event) throws SignalingException { 
		Request request = event.getRequest();
		provider = (SipProvider)event.getSource();
		if(getSipStatus() == SIP_WAITING_FOR_INVITE_ACK) {
			try {
				Response okResponse = messageFactory.createResponse(Response.OK,request);
				logResponseSent(okResponse);
				transaction.sendResponse(okResponse);
				setSipStatus(SIP_ACTIVE);
				
				//release threads that were blocking on an answer() call
				synchronized(answerMonitor) { 
					answerMonitor.notifyAll();
				}
				
			} catch (ParseException e) {
				throw new SignalingException("Unable to send ACK response");
			} catch (SipException e) {
				throw new SignalingException("Unable to send ACK response");
			}
		} else if(getSipStatus() == SIP_WAITING_FOR_BYE_ACK) {
			try {
				Response okResponse = messageFactory.createResponse(Response.OK,request);
				logResponseSent(okResponse);
				transaction.sendResponse(okResponse);
				setSipStatus(SIP_CLOSED);
			} catch (ParseException e) {
				throw new SignalingException("Unable to send ACK response");
			} catch (SipException e) {
				throw new SignalingException("Unable to send ACK response");
			}
		}
	}
	
	public void handleBye(RequestEvent event) throws SignalingException { 
		Request request = event.getRequest();
		SipProvider provider = (SipProvider)event.getSource();
		
		if(getSipStatus() == SIP_ACTIVE) { 
			Response okResponse;
			try {
				okResponse = messageFactory.createResponse(Response.OK,request);
				logResponseSent(okResponse);
				provider.sendResponse(okResponse);
				
				setSipStatus(SIP_WAITING_FOR_BYE_ACK);
			} catch (ParseException e) {
				throw new SignalingException("Unable to send BYE response");
			} catch (SipException e) {
				throw new SignalingException("Unable to send BYE response");
			}
		}
	}
	
	public int getSipStatus() {
		return this.sipStatus;
	}
	
	private void setSipStatus(int status) { 
		this.sipStatus = status;
	}
}
