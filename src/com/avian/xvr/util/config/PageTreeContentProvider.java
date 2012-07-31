package com.avian.xvr.util.config;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class PageTreeContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldObj, Object newObj) {

	}

	public final static int DEPTH_THRESHOLD = 3;
	public Object[] getChildren(Object parent) {
		ConfigurationPage parentPage = (ConfigurationPage) parent;
		if(parentPage.depth < DEPTH_THRESHOLD) return parentPage.subPages.toArray();
		else return new Object[0];
	}

	public Object getParent(Object parent) {
		if(parent instanceof ConfigurationSchema) { 
			return null;
		} else {
			ConfigurationPage parentPage = (ConfigurationPage) parent;
			return parentPage.parentPage;
		}
	}

	public boolean hasChildren(Object parent) {
		ConfigurationPage parentPage = (ConfigurationPage) parent;
		return ((parentPage.subPages.size() > 0) && (parentPage.depth < DEPTH_THRESHOLD));
	}
	

	public Object[] getElements(Object parent) {
		return ((ConfigurationSchema)parent).pages.toArray();
	}

}
