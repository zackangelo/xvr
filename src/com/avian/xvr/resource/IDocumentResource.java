/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.resource;


/**
 * @author Zack
 *
 */
public interface IDocumentResource {
	public String getMimeType();
	public byte[] getData();
	public String getUrl();
}
