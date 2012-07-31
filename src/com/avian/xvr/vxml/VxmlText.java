package com.avian.xvr.vxml;

public class VxmlText extends VxmlNode {
	public VxmlText() { 
		super();
		this.type = TEXT;
	}
	
	public VxmlText(String text) {
		this();
		setText(text);
	}
	
	public String getText() { 
		return this.text;
	}
	
	public void setText(String text) { 
		this.text = text;
	}
	
	private String text;
}
