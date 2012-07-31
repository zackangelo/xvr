package com.avian.xvr.util.config;

import java.util.*;

import org.eclipse.swt.widgets.Composite;

public class ConfigurationPage {
	public ConfigurationPage() { 
		subPages = new ArrayList<ConfigurationPage>();
		sections = new ArrayList<ConfigurationSection>();
	}
	
	String name;
	String propertyBase;
	
	ConfigurationPage parentPage;
	List<ConfigurationSection> sections;
	List<ConfigurationPage> subPages;
	
	//TODO should probably refactor this functionality into an external
	//	map so our config data isn't tied to SWT
	Composite pageWidget;
	
	int depth;
}
