package com.avian.xvr.util.config;

import org.eclipse.swt.widgets.Composite;

public class ConfigurationProperty {
	public enum WidgetType { 
		TEXT,
		COMBOSELECT,
		LISTSELECT,
		CHECKLISTSELECT
	};

	String name;
	String label;
	WidgetType widgetType;
	Composite widget;
	ConfigurationPage page;
	
	/**
	 * Extracts a value from this property based on the 
	 * configuration widget used.
	 */
	public String getValue() { 
		return "";
	}
	
	private String _getFqName(ConfigurationPage pg,String name) { 
		if(pg.parentPage != null) { 
			return _getFqName(pg.parentPage,pg.propertyBase+"."+name);
		} else { 
			return name;
		}
	}
	
	public String getFqName() { 
		return _getFqName(page,name);
	}
}
