package com.avian.xvr.util.config;

import java.util.*;

public class ConfigurationSchema {
	public ConfigurationSchema() { 
		pages = new ArrayList<ConfigurationPage>();
	}
	
	String resourcesClass;
	List<ConfigurationPage> pages;
}
