/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.avian.xvr.resource;

/**
 * @author Zack
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BaseDocumentResource implements IDocumentResource {

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IDocumentResource#getMimeType()
	 */
	public String getMimeType() {
		return mimeType;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IDocumentResource#getData()
	 */
	public byte[] getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IDocumentResource#getUrl()
	 */
	public String getUrl() {
		return url;
	}

	String url;
	byte[] data;
	String mimeType;
	
	public void setData(byte[] data) {
		this.data = data;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
