/*
 * Created on Apr 5, 2005
 *
 */
package com.avian.xvr.signaling.sip;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.TransportNotSupportedException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import com.avian.xvr.signaling.ISignalingEventHandler;
import com.avian.xvr.signaling.ISignalingProvider;
import com.avian.xvr.signaling.SignalingException;

import java.util.*;

/**
 * @author Zack
 *
 */
public class SipSignalingProvider implements ISignalingProvider,SipListener {
	private String iface;
	private int port;
	private ISignalingEventHandler handler;
	private SipStack sipStack;
	private Logger logger;
	
	//maps call-id to signaling channel
	private Map<String,SipSignalingChannel> channelMap;
	
	public String getIpAddress() { 
		return this.iface;
	}
	
	public int getPort() { 
		return this.port;
	}
	
	public SipSignalingProvider(String iface,int port) { 
		this.iface = iface;
		this.port = port;
		this.logger = Logger.getLogger(this.getClass());
		
		channelMap = new Hashtable<String,SipSignalingChannel>();
	}
	
	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingProvider#init(com.avian.xvr.signaling.ISignalingEventHandler)
	 */
	public void init(ISignalingEventHandler handler) throws SignalingException {
		this.handler = handler;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingProvider#destroy()
	 */
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingProvider#listenForConnections()
	 */
	public void listenForConnections() throws SignalingException {
		Properties stackProperties = new Properties();
		stackProperties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		stackProperties.setProperty("javax.sip.STACK_NAME", "com.avian.xvr-server");		
		stackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
		stackProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG","xvr_sip_debug.txt");
		stackProperties.setProperty("gov.nist.javax.sip.SERVER_LOG","xvr_sip_server.txt");
		stackProperties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
		stackProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
		stackProperties.setProperty("javax.sip.IP_ADDRESS", iface);

		SipFactory sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		
		logger.debug("Creating SIP Stack...");
		try {
			sipStack = sipFactory.createSipStack(stackProperties);
		} catch (PeerUnavailableException e) {
			throw new SignalingException("Unable to create SIP stack");
		}
		
		ListeningPoint udp;
		
		logger.debug("Creating SIP listener on "+iface+":"+port+"...");
		
		try {
			udp = sipStack.createListeningPoint(port, "udp");
			SipProvider sipProvider = sipStack.createSipProvider(udp);
			sipProvider.addSipListener(this);
		} catch (TransportNotSupportedException e1) {
			throw new SignalingException("Unable to create listening point, UDP not supported.");
		} catch (InvalidArgumentException e1) {
			throw new SignalingException("Unable to create listening point, invalid port specified.");
		} catch (ObjectInUseException e) {
			//TODO: wtf is this for? 
			throw new SignalingException("Object in use?");
		} catch (TooManyListenersException e) {
			throw new SignalingException("Listener limit exceeded.");
		}
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.signaling.ISignalingProvider#stopListening()
	 */
	public void stopListening() {
	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId =
			requestEvent.getServerTransaction();
		
		logger.debug("REQUEST <<< \n" + request);
		
		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransactionId);
		}
	}

	private void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
		Request request = requestEvent.getRequest();
		String callId = ((CallIdHeader)request.getHeader(CallIdHeader.NAME)).getCallId();
		SipSignalingChannel sigChan = channelMap.get(callId);
		
		try {
			sigChan.handleBye(requestEvent);
		} catch (SignalingException e) {
			e.printStackTrace();
		}
	}

	private void processAck(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
		Request request = requestEvent.getRequest();
		String callId = ((CallIdHeader)request.getHeader(CallIdHeader.NAME)).getCallId();
		SipSignalingChannel sigChan = channelMap.get(callId);
		
		try {
			sigChan.handleAck(serverTransactionId,requestEvent);
		} catch (SignalingException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		logger.debug("SIP RESPONSE:\n"+response);
	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void processInvite(RequestEvent requestEvent,ServerTransaction serverTransaction) {
		Request request = requestEvent.getRequest();
		String callId = ((CallIdHeader)request.getHeader(CallIdHeader.NAME)).getCallId();
		SipSignalingChannel sigChan;
		
		if(serverTransaction == null) {
			SipProvider provider = (SipProvider)requestEvent.getSource();
			
			try {
				serverTransaction = provider.getNewServerTransaction(request);
			} catch (TransactionAlreadyExistsException e) {
				e.printStackTrace();
			} catch (TransactionUnavailableException e) {
				e.printStackTrace();
			}
		}
		
		try {
			if(!channelMap.containsKey(callId)) {
				//fresh inbound call..
				sigChan = new SipSignalingChannel(request,serverTransaction.getDialog(),getIpAddress());
				channelMap.put(callId,sigChan);
			} else {
				//TODO handle reinvite
				sigChan = channelMap.get(callId);
			}
			
			sigChan.handleInvite(serverTransaction,requestEvent);
		} catch (SignalingException e) {
			e.printStackTrace();
		}
	}
}
