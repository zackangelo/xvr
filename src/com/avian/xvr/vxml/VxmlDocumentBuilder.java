package com.avian.xvr.vxml;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class builds a document tree of the VoiceXML. It also aids the interpreter by providing some normalization
 * in the document. This allows us to cut down on special case handling in the interpreter logic. Namely: 
 * 
 *  * <filled> tags residing in form field elements are explicitly assigned the form field's variable name.
 *  * Assigns explicit variable names to unnamed form items
 *  * Text outside of a <prompt> element is explicitly place inside of a <prompt> element. 
 * 
 *  * Bookmarks dialog fragments (forms and menus) by name
 * @author zangelo
 *
 */
public class VxmlDocumentBuilder extends DefaultHandler {
	public VxmlDocumentBuilder() { 
		tagMap = VxmlTagMapFactory.createMap();
		formItemVarCounter = 0;
	}
	public VxmlDocument getDocument() { 
		return document;
	}
	
	private VxmlDocument document;
	private StringBuilder charBuf;
	private VxmlElement eleCursor;
	private Map<String,Integer> tagMap;
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		charBuf.append(ch,start,length);
	}

	@Override
	public void endDocument() throws SAXException {	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		int tag = tagMap.get(localName);
		if(tag == IVxmlTags.VXML) { 
			eleCursor = null;
		} else { 
			appendTextNode();
			eleCursor = (VxmlElement) eleCursor.getParent();
		}
	}

	@Override
	public void startDocument() throws SAXException {
		this.document = new VxmlDocument();
		this.charBuf = new StringBuilder();
	}
	
	private void explicitFilledNamelist(VxmlElement e) { 
		if( (eleCursor.getTag() == IVxmlTags.FILLED) && (eleCursor.getParent().getTag() == IVxmlTags.FIELD) ) {
			eleCursor.setAttribute("namelist",eleCursor.getParent().getAttribute("name"));
		}
	}
	
	private int formItemVarCounter;
	private static final String FORM_ITEM_VAR_PREFIX="__xvrImplicit";
	private void explicitFormItemVariable(VxmlElement e) { 
		if(eleCursor.getParent().getTag() == IVxmlTags.FORM) {
			if( (eleCursor.getTag() == IVxmlTags.BLOCK) || 
				(eleCursor.getTag() == IVxmlTags.FIELD) || 
				(eleCursor.getTag() == IVxmlTags.SCRIPT)) {
				
				if(eleCursor.getAttribute("name") == null) { 
					eleCursor.setAttribute("name",FORM_ITEM_VAR_PREFIX+(formItemVarCounter++));				
				}
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		int tag = tagMap.get(localName);
		if(tag == IVxmlTags.VXML) { 
			/* this is the top-level document tag, parent should be null 
			 * (or document?)
			 */
			eleCursor = new VxmlElement(localName);
			
			eleCursor.setParent(null);
			
			document.setDocumentElement(eleCursor);
		} else { 
			appendTextNode();
			VxmlElement newElement = new VxmlElement(localName);
			newElement.setParent(eleCursor);
			eleCursor.appendChild(newElement);
			eleCursor = newElement;
		}
		
		eleCursor.setTag(tagMap.get(localName));
		copyAttributes(eleCursor,attributes);
		
		if(eleCursor.getTag() != IVxmlTags.VXML) { 
			/* checks for an empty namelist inside of a form-level filled element */
			explicitFilledNamelist(eleCursor);
			
			/* assign explicit names to unnamed form items */ 
			explicitFormItemVariable(eleCursor);
			
		}
		
		if(eleCursor.getTag() == IVxmlTags.FORM || eleCursor.getTag() == IVxmlTags.MENU) { 
			String idStr = eleCursor.getAttribute("id");
			
			if(idStr != null) 
				document.setBookmark(idStr,eleCursor);
		}
	}
	
	private void copyAttributes(VxmlElement element, Attributes attributes) {
		for(int i=0;i<attributes.getLength();i++) { 
			eleCursor.setAttribute(attributes.getLocalName(i),attributes.getValue(i));
		}
	}
	
	private void appendTextNode() { 
		String charStr = charBuf.toString().trim();
		
		if(charStr.length() > 0) { 
			eleCursor.appendChild(new VxmlText(charStr));
		}

		charBuf.setLength(0);
	}
}
