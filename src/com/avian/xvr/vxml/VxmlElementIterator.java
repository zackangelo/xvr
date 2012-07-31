package com.avian.xvr.vxml;

import java.util.Iterator;

public class VxmlElementIterator {
	Iterator<VxmlNode> it;
	VxmlElement next;
	boolean hasNext = false;
	
	public VxmlElementIterator(Iterator<VxmlNode> it) { 
		this.it = it;
		setNextElement();
	}
	
	private void setNextElement() { 
		next = null;
		hasNext = false;
		while(it.hasNext()) { 
			VxmlNode n = it.next();
			if(n.getType() == VxmlNode.ELEMENT) {
				hasNext = true;
				next = (VxmlElement)n;
				break;
			}
		}
	}
	
	public boolean hasNext() { 
		return hasNext;
	}
	
	public VxmlElement next() { 
		if(!hasNext()) return null;
		VxmlElement e = next;
		setNextElement();
		return e;
	}
}
