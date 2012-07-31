/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.vxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.avian.xvr.interpreter.IParser;
import com.avian.xvr.resource.IDocumentResource;



/**
 * @author Zack
 *
 */
public class VxmlParser implements IParser {
	public VxmlParser() { 
		logger = Logger.getLogger(this.getClass());
	}

	Logger logger;
	public final static String SAX_PARSER_CLASS = "org.apache.xerces.parsers.SAXParser";
	
	/* (non-Javadoc)
	 * @see com.avian.xvr.interpreter.IParser#parse(java.nio.ByteBuffer)
	 */
	public VxmlDocument parse(IDocumentResource doc) throws VxmlParseException {
		VxmlDocumentBuilder builder = new VxmlDocumentBuilder();
		XMLReader xmlReader ;
		
		logger.debug("Starting parser...");
		
		try {
			xmlReader = XMLReaderFactory.createXMLReader(SAX_PARSER_CLASS);
			xmlReader.setContentHandler(builder);
			xmlReader.setErrorHandler(builder);
			xmlReader.setDTDHandler(builder);
		
			xmlReader.parse(new InputSource(new ByteArrayInputStream(doc.getData())));
			
			VxmlDocument document = builder.getDocument();

			return document;
		} catch (Throwable e) {	
			throw new VxmlParseException("Error parsing VoiceXML document.",e);
		} 
	}
}
