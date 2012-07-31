package com.avian.xvr.resource.jakarta;

import com.avian.xvr.resource.*;

import java.io.*;

import javax.sound.sampled.*;

public class HttpResourceFetcher extends BaseResourceFetcher {
	private HttpClientPool pool;
	
	public HttpResourceFetcher() { 
		pool = HttpClientPoolFactory.getInstance().createPool();
	}
	private byte[] fetchHttpData(String url) throws ResourceFetchException { 
		try {
			return pool.queueRequestAndBlock(url);
		} catch (Exception e) { 
			throw new ResourceFetchException("Error fetching resource: " + url,e);
		}
	}
	
	public IAudioResource fetchAudio(String url) throws ResourceFetchException {
		
		byte[] fileBuf = fetchHttpData(url);
		ByteArrayInputStream bais = new ByteArrayInputStream(fileBuf);
		AudioInputStream wavFile;
		try {
			wavFile = AudioSystem.getAudioInputStream(bais);
			BaseAudioResource bar = new BaseAudioResource(fileBuf,wavFile.getFormat().getSampleRate(),
					wavFile.getFormat().getSampleSizeInBits(),
					IAudioResource.PCM_ENCODING);
			
			
			return bar;
		} catch (UnsupportedAudioFileException e) {
			throw new ResourceFetchException("Unable to fetch resource at: " + url);
		} catch (IOException e) {
			throw new ResourceFetchException("Unable to fetch resource at: " + url);
		}		
	}

	public IDocumentResource fetchDocument(String url)
			throws ResourceFetchException {
		BaseDocumentResource doc = new BaseDocumentResource();
		doc.setUrl(url);
		doc.setMimeType("application/xml");
		
		byte[] buf = fetchHttpData(url);
		doc.setData(buf);
		return doc;
	}

	public void init() {

	}

}
