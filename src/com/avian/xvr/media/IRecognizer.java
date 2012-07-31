package com.avian.xvr.media;

public interface IRecognizer {
	public static final int WAITING_FOR_INPUT = 0;
	public static final int IDLE = 1;
	public static final int WORKING = 2;
	
	public void waitForInput();
	public void loadGrammar();
	public int getStatus();
}
