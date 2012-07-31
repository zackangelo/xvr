package com.avian.xvr.media;

public class RecognizerResult {
	private boolean dtmf;
	private String result;
	
	public boolean isDtmf() { 
		return dtmf;
	}
	
	public boolean setDtmf(boolean dtmf) { 
		return this.dtmf = dtmf;
	}
	
	public void setResult(String result) { 
		this.result = result;
	}
	
	public String getResult() { 
		return result;
	}
}
