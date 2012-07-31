package com.avian.xvr.vxml;

import java.util.*;

import com.avian.xvr.interpreter.InterpreterException;

public class VxmlFormState {
	public VxmlFormState(VxmlElement e) throws InterpreterException {
		if(e.getTag() != IVxmlTags.FORM) {
			throw new InterpreterException("Attempted to create a form state on something other than a form element.");
		}
		
		this.formElement = e;
		this.items = new ArrayList<VxmlFormItem>();
		this.currentItem = null;
	}

	public void addItem(VxmlFormItem item) { 
		items.add(item);
	}
	
	public Iterator<VxmlFormItem> itemIterator() { 
		return items.iterator();
	}
	
	private VxmlElement formElement;
	private List<VxmlFormItem> items;
	private VxmlFormItem currentItem;
	
	public VxmlElement getFormElement() {
		return formElement;
	}

	public void setFormElement(VxmlElement formElement) {
		this.formElement = formElement;
	}
	
	public Collection<VxmlFormItem> getFormItems() { 
		return items;
	}

	public VxmlFormItem getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(VxmlFormItem currentItem) {
		this.currentItem = currentItem;
	}
}
