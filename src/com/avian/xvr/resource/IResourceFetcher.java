/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.resource;


/**
 * @author Zack
 *
 */
public interface IResourceFetcher {
	public IAudioResource fetchAudio(String url) throws ResourceFetchException;
	public IDocumentResource fetchDocument(String url) throws ResourceFetchException;

	public void init();
}
