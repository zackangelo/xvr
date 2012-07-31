package com.avian.xvr.media.sphinx;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.dtmf.DtmfEvent;
import com.avian.iaf.rtp.dtmf.IDtmfHandler;
import com.avian.xvr.media.RecognizerResult;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;


/**
 * TODO generalize this class for _any_ recognizer. currently has the dtmf
 * 		timeout code munged in with the sphinx recognition stuff.
 * 
 * 
 * On DTMF timeout: 
 * This class schedules a new timer task everytime a digit is pressed.
 * If another digit is pressed before the timer executes, the task is
 * discarded and a new one is scheduled. When the task finally executes,
 * it notifies the interpreter that an event is waiting (which resets the
 * status to IDLE).
 */
public class SphinxRecognizerThread extends Thread implements IDtmfHandler {
	private final static int IDLE = 0;
	private final static int WAITING_DTMF = 1;
	private final static int WAITING_ASR = 2;
	private final static int WAITING_DTMF_AND_ASR = 3;
	
	private final static long INTER_DIGIT_TIMEOUT = 500;
	
	Recognizer reco;
	Logger logger;
	DtmfEvent lastDtmf;
	
	long lastDtmfEventTime;
	
	boolean isFirstAsrResult;
	
	int status;
	
	Timer digitTimer;
	
	static class DigitTimeoutTask extends TimerTask {
		Object monitor;
		
		public DigitTimeoutTask(Object monitor) { 
			this.monitor = monitor;
		}
		@Override
		public void run() {
			synchronized(monitor) {
				monitor.notifyAll();
			}
		} 
	}
	
	DigitTimeoutTask timeoutTask;
	
	public SphinxRecognizerThread(Recognizer reco) {
		super("SphinxRecognitionThread");
		logger = Logger.getLogger(this.getClass());
		this.reco = reco;
		this.status = IDLE;
		this.digitTimer = new Timer();
//		this.timeoutTask = new DigitTimeoutTask(this);
		isFirstAsrResult = true;
	}

	RecognizerResult currentResult;
	
	/**
	 * TODO allow recognizer mode specification (asr|dtmf)
	 * @return
	 */
	public synchronized RecognizerResult waitForResult() { 
		currentResult = null;
		
		this.status = WAITING_ASR;
		
		logger.info("Waiting for user input.");
		while(true) { 
			try { wait(); } catch(InterruptedException e) { } 
			
			if(currentResult != null) { 
				RecognizerResult temp = currentResult;
				currentResult = null;
				this.status = IDLE;
				return temp;
			}
		}
	}
	
	public void run() { 
		logger.debug("Starting recognizer thread...");
		while(true) { 
			Result result = reco.recognize();

//			String resultText = result.getBestResultNoFiller();
//			System.out.println("You said: " + resultText + "\n");

			synchronized(this) { 
				//FIXME: we have to skip the first recognizer result for
				//			some reason.
				if(isFirstAsrResult) {
					isFirstAsrResult = false;
					continue;
				}

				if (result != null) {
					if((status != WAITING_ASR) && (status != WAITING_DTMF_AND_ASR)) { 
						continue;
					}

//					logger.debug("Returning result...");
					currentResult = new RecognizerResult();
					currentResult.setResult(result.getBestResultNoFiller());
					currentResult.setDtmf(false);
					
					this.status = IDLE;
					
					notifyAll();
				} else {
					System.out.println("I can't hear what you said.\n");
			    }
			}
		}
	}

	public void handleDtmf(DtmfEvent event) {
		synchronized(this) { 
			//discard if we're not waiting for an event
//			if((status != WAITING_DTMF) && (status != WAITING_DTMF_AND_ASR)) { 
//				return;
//			}
			
			//discard if it's not an "end" event
			if(!event.isEnd()) { 
				return;
			}
			
			//discard if it's a duplicate "end" event
			if(lastDtmf != null) {
				if(
						(lastDtmf.getDigit() == event.getDigit()) &&
						(lastDtmf.isEnd()) && (event.isEnd())) {
					return;
				}
			}

//			logger.debug("DIGIT PRESSED");
			
			if(timeoutTask != null) {
				timeoutTask.cancel();
			}

			timeoutTask = new DigitTimeoutTask(this);
			
			if(currentResult == null) {
				currentResult = new RecognizerResult();
				currentResult.setResult("");
				currentResult.setDtmf(true);
			}
			
			currentResult.setResult(currentResult.getResult() + event.getDigit());
			lastDtmf = event;

			//TODO do max digit and other checks here
			digitTimer.schedule(timeoutTask,INTER_DIGIT_TIMEOUT);
//			logger.debug("DTMF event: " + event);
//			notifyAll();
		}
	}
}
