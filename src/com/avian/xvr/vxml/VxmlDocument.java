package com.avian.xvr.vxml;

import java.util.Map;
import java.util.HashMap;

public class VxmlDocument extends VxmlNode {
	public VxmlDocument() { 
		super();
		this.type = DOCUMENT;
		this.bookmarks = new HashMap<String,VxmlElement>();
	}
	
	public void setDocumentElement(VxmlElement docElement) { 
		this.docElement = docElement;
	}
	
	public VxmlElement getDocumentElement() { 
		return this.docElement;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	private VxmlElement docElement;
	private String url;
	private Map <String,VxmlElement> bookmarks;
	
	public VxmlElement getBookmark(String name) {
		return bookmarks.get(name);
	}
	
	public void setBookmark(String name,VxmlElement e) { 
		bookmarks.put(name,e);
	}

}
