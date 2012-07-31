package com.avian.xvr.vxml;

import java.util.*;

public abstract class VxmlNode {
	public final static int DOCUMENT = 0;
	public final static int ELEMENT = 1;
	public final static int TEXT = 2;
	
	public VxmlNode() { 
		children = new ArrayList<VxmlNode>();
	}
	
	public VxmlNode(VxmlElement parent) { 
		this();
		setParent(parent);
	}
	
	public VxmlElement getParent() { 
		return parent;
	}
	
	public void setParent(VxmlElement parent) { 
		this.parent = parent;
	}
	
	public Collection<VxmlNode> getChildren() {
		return children;
	}
	
	public void appendChild(VxmlNode child) { 
		children.add(child);
	}
	
	public int getType() { 
		return type;
	}
	
	public VxmlElementIterator childElementIterator() {
		return new VxmlElementIterator(getChildren().iterator());
	}
	
	VxmlElement parent;
	List<VxmlNode> children;
	int type;
}
