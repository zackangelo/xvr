package com.avian.xvr.util.config;

import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.avian.xvr.util.config.ConfigurationProperty.WidgetType;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationSchemaParser {
	public static class SchemaParserHandler extends DefaultHandler  {
		enum SchemaTag {
			CONFIGGUI,
			RESOURCES,
			PAGE,
			PROPERTY,
			SECTION
		}
		
		private Map<String,SchemaTag> tagMap;
		
		/**
		 * The schema we're parsing.
		 */
		ConfigurationSchema schema;
		
		/**
		 * Contextual properties
		 */
		ConfigurationPage currentPage; //page we're working on
		ConfigurationSection currentSection;
		int pageDepth;
		
		
		public SchemaParserHandler() { 
			initTagMap();
			schema = new ConfigurationSchema();
			currentPage = null;
			pageDepth = 0;
		}
		
		public void initTagMap() { 
			if(tagMap == null) { 
				tagMap = new HashMap<String,SchemaTag>();
				
				tagMap.put("configGui",SchemaTag.CONFIGGUI);
				tagMap.put("page",SchemaTag.PAGE);
				tagMap.put("resources",SchemaTag.RESOURCES);
				tagMap.put("property",SchemaTag.PROPERTY);
				tagMap.put("section",SchemaTag.SECTION);
			}
		}
		
		public SchemaTag getTag(String name) { 
			return tagMap.get(name);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			SchemaTag tag = getTag(localName);
			
			//if we're exiting a page we need to do a pop
			if(tag == SchemaTag.PAGE) { 
				currentPage = currentPage.parentPage;
				pageDepth--;
			}
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			// TODO Auto-generated method stub
			super.setDocumentLocator(locator);
		}

		@Override
		public void startDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.startDocument();
		}

		public static WidgetType getWidgetTypeFromAttribute(String attr) 
			throws SAXException { 
			if(attr.equalsIgnoreCase("text")) { 
				return WidgetType.TEXT;
			} else if(attr.equalsIgnoreCase("checkselect")){ 
				return WidgetType.CHECKLISTSELECT;
			} else if(attr.equalsIgnoreCase("comboselect")) { 
				return WidgetType.COMBOSELECT;
			} else if(attr.equalsIgnoreCase("listselect")) { 
				return WidgetType.LISTSELECT;
			}
			
			throw new SAXException("Invalid widget type \""+attr+"\" specified for property.");
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			
			SchemaTag tag = getTag(localName);
			
			if(tag == SchemaTag.CONFIGGUI) {
				currentPage = null;
			} else if(tag == SchemaTag.PAGE) {
				ConfigurationPage pageToAdd = 
					new ConfigurationPage();
				
				pageToAdd.name = attributes.getValue("name");
				pageToAdd.parentPage = currentPage;
				pageToAdd.propertyBase = attributes.getValue("propertyBase");
				pageToAdd.depth = ++pageDepth;
				
				if(currentPage != null) { 
					currentPage.subPages.add(pageToAdd);
				} else { 
					schema.pages.add(pageToAdd);
				}
				
				currentPage = pageToAdd;
			} else if(tag == SchemaTag.PROPERTY) {
				if(currentPage == null) { 
					throw new SAXException("Property tag not allowed as root-level element.");
				}
				
				ConfigurationProperty propertyToAdd = 
					new ConfigurationProperty();
				
				propertyToAdd.name = attributes.getValue("name");
				propertyToAdd.label = attributes.getValue("label");
				propertyToAdd.page = currentPage;
				propertyToAdd.widgetType = 
					getWidgetTypeFromAttribute(attributes.getValue("type"));
				
				if(currentSection != null) {
					currentSection.properties.add(propertyToAdd);
				} else { 
					throw new SAXException("Properties not allowed outside of a section.");
				}
			} else if(tag == SchemaTag.SECTION) {
				if(currentPage == null) { 
					throw new SAXException("Section tag not allowed as root-level element.");
				}
				
				currentSection = new ConfigurationSection();
				currentSection.name = attributes.getValue("name");
				currentSection.parentPage = currentPage;
				
				currentPage.sections.add(currentSection);
				
			} else if(tag == SchemaTag.RESOURCES) {
				if(currentPage != null) { 
					throw new SAXException("Resources tag only allowed as root-level element.");
				}
				
				schema.resourcesClass = attributes.getValue("class");
			}
		}
		
		public ConfigurationSchema getSchema() { 
			return schema;
		}
		
	} 
	
	public ConfigurationSchemaParser() { }
	
	public final static String SAX_PARSER_CLASS = "org.apache.xerces.parsers.SAXParser";

	public ConfigurationSchema parse(InputStream file)
		throws ConfigurationSchemaParseException { 
		
		XMLReader xmlReader ;
		SchemaParserHandler handler = new SchemaParserHandler();
		
		try {
			xmlReader = XMLReaderFactory.createXMLReader(SAX_PARSER_CLASS);
			xmlReader.setContentHandler(handler);
			xmlReader.setErrorHandler(handler);
			xmlReader.setDTDHandler(handler);
		
			xmlReader.parse(new InputSource(file));
			
			ConfigurationSchema schema = handler.getSchema();

			return schema;
		} catch (Throwable e) {	
			throw new ConfigurationSchemaParseException("Error parsing configuration schema.",e);
		} 
	}
}
