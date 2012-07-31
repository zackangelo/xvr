/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.interpreter;

import com.avian.xvr.resource.IDocumentResource;
import com.avian.xvr.vxml.VxmlDocument;

/**
 * @author Zack
 *
 */
public interface IParser {
	/**
	 * Parses the document contained in the byte buffer and returns
	 * the root scope of the executable.
	 * 
	 * @param file buffer containing document data
	 * @return root scope of the executable
	 */
	public VxmlDocument parse(IDocumentResource file) throws ParseException;
}
