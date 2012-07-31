package com.avian.xvr.vxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

public class VxmlElement extends VxmlNode {
	public VxmlElement(String name) { 
		this.type = ELEMENT;
		this.attributes = new HashMap<String,String>();
		
		setName(name);
	}
	
	public String getName() { 
		return name;
	}
	
	private void setName(String name) { 
		this.name = name;
	}
	
	public void setAttribute(String name,String value) { 
		attributes.put(name,value);
	}
	
	public String getAttribute(String name) { 
		return attributes.get(name);
	}
	
	public String toString() { 
		StringBuilder b = new StringBuilder();
		
		b.append("<" + getName() + " ");
		Iterator<Entry<String,String>> it = (Iterator<Entry<String,String>>) attributes.entrySet().iterator();
		
		while(it.hasNext()) { 
			Entry<String,String> en = it.next();
			b.append(en.getKey() + "=" + en.getValue() + " ");
		}
		
		b.append("/>");
		
		return b.toString();
	}
	
	public int getTag() { 
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}
	
	public boolean hasAttribute(String name) { 
		return attributes.containsKey(name);
	}
	
	private Map<String,String> attributes;
	private String name;
	private int tag;
}
