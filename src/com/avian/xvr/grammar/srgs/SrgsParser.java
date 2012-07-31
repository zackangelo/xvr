package com.avian.xvr.grammar.srgs;

import java.io.ByteArrayInputStream;

import javax.speech.recognition.*;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.avian.xvr.grammar.GrammarParseException;
import com.avian.xvr.grammar.srgs.jsapi.SrgsRule;
import com.avian.xvr.grammar.srgs.jsapi.SrgsRuleGrammar;
import com.avian.xvr.resource.IDocumentResource;

import java.util.*;
import java.util.Map.Entry;
/**
 * <h2>General Grammar Notes/Thoughts</h2>
 * 
 * This class parses a document resource into JSAPI grammar structures that are then transformable into
 * a graph that the S4 can read.  I'm pretty sure I can use the JSAPI intermediate structures
 * as a normalization layer, and hopefully, we can move from one grammar format to another with
 * minimal effort. 
 * 
 * @author zangelo
 *
 */
class SrgsParserHandler extends DefaultHandler {
	enum SrgsTag { 
		GRAMMAR,
		RULE,
		ONEOF,
		ITEM,
		RULEREF
	}
	
	private Map<String,SrgsTag> createTagMap() { 
		Map<String,SrgsTag> m = new HashMap<String,SrgsTag>();
		
		m.put("grammar",SrgsTag.GRAMMAR);
		m.put("one-of",SrgsTag.ONEOF);
		m.put("rule",SrgsTag.RULE);
		m.put("item",SrgsTag.ITEM);
		m.put("ruleref",SrgsTag.RULEREF);
		
		return m;
	}
	
	Map<String,SrgsTag> tagMap;
	Logger logger;
	RuleParse output;
	
	
	HashMap<String, SrgsElement> ruleNodeMap;
	SrgsElement publicRule;
	
	SrgsElement currentElement;
	StringBuilder currentText;
	
	// first stage parse structures
	private static class SrgsNode { 
		public SrgsNode(SrgsElement parent) { 
			this.parent = parent;
		}
		SrgsElement parent;
	}
	private static class SrgsElement extends SrgsNode { 
		public SrgsElement(SrgsElement parent,Attributes attributes) { 
			super(parent);
			this.attributes = new HashMap<String,String>();
			this.children = new ArrayList<SrgsNode>();
			setAttributes(attributes);
		}
		
		public String getAttribute(String attr) { 
			return attributes.get(attr);
		}
		
		public void setAttribute(String name,String value) { 
			this.attributes.put(name,value);
		}
		
		public void setAttributes(Attributes attributes) {
			for(int i=0;i<attributes.getLength();i++) { 
				setAttribute(attributes.getLocalName(i),attributes.getValue(i));
			}
		}
		
		SrgsTag tag;
		List<SrgsNode> children;
		Map<String,String> attributes;
		String name;
	}
	
	private static class SrgsText extends SrgsNode { 
		public SrgsText(SrgsElement parent,String value) { 
			super(parent);
			this.value = value;
		}
		String value;
	}
	
	public SrgsParserHandler() { 
		tagMap = createTagMap();
		logger = Logger.getLogger(this.getClass());
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(currentText != null) { 
			currentText.append(ch,start,length);
		} else { 
			logger.debug("Characters encountered with a null currentText instance.");
		}
	}
	
	private SrgsElement findRuleByName(String name) throws SAXException {
		
		if(!name.startsWith("#")) {
			throw new SAXException("Only local rule references supported.");
		}

		SrgsElement rule = ruleNodeMap.get(name.substring(1));

		//TODO handle external rule references
		if(rule != null) { 
			return rule;
		} else { 
			throw new SAXException("Can't find rule: " + name);
		}
	}
	
	private SrgsElement findPublicRule() throws SAXException { 
		Iterator<SrgsElement> ruleIter = ruleNodeMap.values().iterator();
		
		while(ruleIter.hasNext()) { 
			SrgsElement rule = ruleIter.next();
			String scope = rule.getAttribute("scope");
			if((scope != null) && (scope.equals("public"))) return rule;
		} 
	
		throw new SAXException("Public rule not found.");
	}
	
	private RuleToken[] getTokensFromText(String text) { 
		ArrayList<RuleToken> ruleTokens = new ArrayList<RuleToken>();
		//TODO split on any whitespace (not just " "), use regex .split() method
		StringTokenizer tzr = new StringTokenizer(text," ");
		
		while(tzr.hasMoreTokens()) { 
			String tok = tzr.nextToken();
			ruleTokens.add(new RuleToken(tok));
		}
		
		return ruleTokens.toArray(new RuleToken[0]);
	}
	
	private Rule parse(SrgsNode node) throws SAXException { 
		if(node instanceof SrgsElement) { 
			SrgsElement element = (SrgsElement) node;
			switch(element.tag) { 
			case ONEOF:
				{
					List<Rule> altList = new ArrayList<Rule>();
					for(SrgsNode n:element.children) { 
						if(n instanceof SrgsElement) {
							SrgsElement e = (SrgsElement)n;
							if(e.tag == SrgsTag.ITEM) {
								altList.add(parse(e));
							} else {
								throw new SAXException("Only items allowed in one-of.");
							}
						} /*else {
							throw new SAXException("Freestanding text in one-of");
						}*/
					}
					
					return new RuleAlternatives(altList.toArray(new Rule[0]));
				}
			case ITEM: 
				{
					List<Rule> itemList = new ArrayList<Rule>();
					for(SrgsNode n:element.children) { 
							itemList.add(parse(n));
					}
					
					RuleSequence itemSeq = new RuleSequence(itemList.toArray(new Rule[0]));
					
					String repeatAttr = element.getAttribute("repeat");
					if(repeatAttr != null) { 
						int repeatOption;
						if(repeatAttr.equals("0-1")) {
							repeatOption = RuleCount.OPTIONAL;
						} else if(repeatAttr.equals("1-")) {
							repeatOption = RuleCount.ONCE_OR_MORE;
						} else if(repeatAttr.equals("0-")) {
							repeatOption = RuleCount.ZERO_OR_MORE;
						} else {
							throw new SAXException("Unrecognized repeat option.");
						}
						
						return new RuleCount(itemSeq,repeatOption);
					}
					
					return itemSeq;
				}
			case RULE: 
				{
					List<Rule> ruleList = new ArrayList<Rule>();
					for(SrgsNode n:element.children) { 
							ruleList.add(parse(n));
					}
					
					return new RuleSequence(ruleList.toArray(new Rule[0]));
				}
			case RULEREF: 
				{
					String ruleId = element.getAttribute("uri");
					
					if(ruleId == null) { 
						throw new SAXException("Missing rule reference name.");
					}
					
					if(!ruleId.startsWith("#")) {
						throw new SAXException("Only local rule references allowed."); 
					}
				
					//chop off #
					ruleId = ruleId.substring(1);
					
					if(!ruleNodeMap.containsKey(ruleId)) { 
						throw new SAXException("Rule referenced does not exist.");
					}
					
					RuleName ruleName = new RuleName(ruleId);
					return ruleName;
				}
			default: 
				throw new SAXException("Element " + element.name + " not recognized.");
			}
		} else if (node instanceof SrgsText) { 
			return new RuleSequence( getTokensFromText(((SrgsText)node).value) );
		} else {
			throw new SAXException("Unrecognized node.");
		}
	}

	private void checkGrammarVersion(Attributes attr) throws SAXException { 
		String ver = attr.getValue("version");
		
		if((ver == null)) {
			throw new SAXException("SRGS grammar not specified.");
		}
		
		if(!(ver.equals("1.0"))) {
			throw new SAXException("Unsupported SRGS grammar version: " + ver);
		}
	}
	
	private void checkRuleAttributes(Attributes attr) throws SAXException { 
		
		String ruleId = attr.getValue("id");
		
		if(ruleId == null) { 
			throw new SAXException("Rule missing id attribute");
		} 
		
		//TODO check to make sure we're not using any illegal characters
	}
	
	private void initializeGrammar() { 
		ruleNodeMap = new HashMap<String,SrgsElement>();
		currentText = new StringBuilder();
		currentElement = null;
	}
	
	private void appendTextNode() { 
		//TODO whitespace normalization
	
		if(currentText.length() > 0) { 
			if(currentElement != null) {
				String text = currentText.toString().trim();
				
				if(!text.equals("")) {
					currentElement.children.add(new SrgsText(currentElement, text));
				}
			}
		}
		
		currentText = new StringBuilder();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		SrgsTag tag = tagMap.get(localName);
		
		if(currentText != null) appendTextNode();
		
		switch(tag) {
		case GRAMMAR:
			checkGrammarVersion(attributes);
			initializeGrammar();
		break;
		
		case RULE:
			checkRuleAttributes(attributes);		
		default:	//fall through for all other elements...
			SrgsElement elementToAdd = new SrgsElement(currentElement,attributes);
			
			if(currentElement !=  null) 
				currentElement.children.add(elementToAdd);
			
			currentElement = elementToAdd;
			currentElement.tag = tag;
		break;
			
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		appendTextNode();		
		if(currentElement != null) {
			if(currentElement.tag == SrgsTag.RULE) {
				this.ruleNodeMap.put(currentElement.getAttribute("id"),currentElement);
			}
			currentElement = currentElement.parent;
		}
	}
	
//	public void createRootRule() throws SAXException { 
//		this.rootRule = parse(findPublicRule());
//	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		ruleGrammar = createRuleGrammar();
	}
	
//	public Rule getRootRule() { 
//		return rootRule;
//	}
	
	RuleGrammar ruleGrammar;
	
	private RuleGrammar createRuleGrammar() throws SAXException {
		Map<String,SrgsRule> ruleMap = new HashMap<String,SrgsRule>();
		Set<Entry<String,SrgsElement>> entries = ruleNodeMap.entrySet();
		
		for(Entry<String,SrgsElement> e:entries) { 
			String pubAttr = e.getValue().getAttribute("scope");
			boolean isPublic = ((pubAttr != null) && pubAttr.equals("public"));
			SrgsRule srgsRule = new SrgsRule(e.getKey(),parse(e.getValue()),isPublic);
			ruleMap.put(e.getKey(),srgsRule);
		}
		
		return new SrgsRuleGrammar(ruleMap);
	}
	
	public RuleGrammar getRuleGrammar() { 
		return ruleGrammar;
	}
}

public class SrgsParser {
	public SrgsParser() { } 
	public final static String SAX_PARSER_CLASS = "org.apache.xerces.parsers.SAXParser";

	public RuleGrammar parse(IDocumentResource doc) throws GrammarParseException { 
		XMLReader xmlReader ;
		SrgsParserHandler handler;
		
		try {
			handler = new SrgsParserHandler();
			xmlReader = XMLReaderFactory.createXMLReader(SAX_PARSER_CLASS);
			xmlReader.setContentHandler(handler);
			xmlReader.setErrorHandler(handler);
			xmlReader.setDTDHandler(handler);
		
			xmlReader.parse(new InputSource(new ByteArrayInputStream(doc.getData())));
			
			return handler.getRuleGrammar();
		} catch (Throwable e) {	
			throw new GrammarParseException("Error parsing SRGS grammar.",e);
		} 
	}
	
}
