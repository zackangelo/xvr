/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.resource;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.*;

/**
 * @author Zack
 *
 */
public class LocalResourceFetcher implements IResourceFetcher {
	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IResourceFetcher#fetchAudio(java.lang.String)
	 */
	public IAudioResource fetchAudio(String url) throws ResourceFetchException {
		//TODO: roll our own wav file parser
		try { 
			File f = new File(url);
			AudioInputStream wavFile = AudioSystem.getAudioInputStream(f);	

			byte[] buf = new byte[wavFile.available()]; 
			wavFile.read(buf);
			
			ByteBuffer b = ByteBuffer.wrap(buf);
			
			BaseAudioResource audio = 
				new BaseAudioResource(buf,wavFile.getFormat().getSampleRate(),
						wavFile.getFormat().getSampleSizeInBits(),
						IAudioResource.PCM_ENCODING);
			
			//should always be little-endian if wav file
			audio.setBigEndian(wavFile.getFormat().isBigEndian());
			audio.setUrl(url);
			
			wavFile.close();
			return audio;
		} catch(Throwable t) { 
			throw new ResourceFetchException("Error fetching audio resource " + url);
		}
	}

	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IResourceFetcher#fetchDocument(java.lang.String)
	 */
	public IDocumentResource fetchDocument(String url) throws ResourceFetchException {
		BaseDocumentResource doc = new BaseDocumentResource();
		doc.setUrl(url);
		doc.setMimeType("application/xml");
		
		try {
			byte[] buf = getBytesFromFile(new File(url));
			doc.setData(buf);
			return doc;
		} catch (FileNotFoundException e) {
			throw new ResourceFetchException("Could not find document " + url);
		} catch (IOException e) {
			throw new ResourceFetchException("Error reading from document " + url);
		}
	}
   
    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
	/* (non-Javadoc)
	 * @see com.avian.xvr.resource.IResourceFetcher#init()
	 */
	public void init() {
	}

}
