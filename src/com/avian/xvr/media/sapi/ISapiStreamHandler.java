package com.avian.xvr.media.sapi;
import java.nio.ByteBuffer;

public interface ISapiStreamHandler {
	public void write(ByteBuffer buf);
}
