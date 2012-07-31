package com.avian.xvr.media.sapi;

import java.nio.*;


public class ByteBufferStreamHandler implements ISapiStreamHandler {
	private int size;
	private ByteBuffer buffer;
	
	public ByteBufferStreamHandler() { 
		this.size = 0;
		this.buffer = ByteBuffer.allocate(1024*256);
	}
	
	public void write(ByteBuffer buf) {
		System.out.println("WRITING " + buf.capacity() + " BYTES!");
		size += buf.capacity();
		buffer.put(buf);		
	}

	public ByteBuffer getBuffer() { 
		buffer.limit(buffer.position());
		buffer.position(0);
		
		return this.buffer;
	}
}
