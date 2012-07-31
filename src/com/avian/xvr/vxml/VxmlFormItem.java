package com.avian.xvr.vxml;

import com.avian.xvr.interpreter.InterpreterException;

public class VxmlFormItem {
	public VxmlFormItem(VxmlElement e) throws InterpreterException { 
		int t = e.getTag();
		boolean isFormItem = 
			((t == IVxmlTags.BLOCK) || (t == IVxmlTags.FIELD));
		
		if(!isFormItem) { 
			throw new InterpreterException("Attempted to create a form item on an invalid element.");
		}
		
		this.element = e;
		this.promptCounter = 1;
	}
	
	private VxmlElement element;
	private int promptCounter;
	
	public VxmlElement getElement() {
		return element;
	}
	public void setElement(VxmlElement element) {
		this.element = element;
	}
	public int getPromptCounter() {
		return promptCounter;
	}
	public void setPromptCounter(int prompCounter) {
		this.promptCounter = prompCounter;
	}
}
