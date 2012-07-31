package com.avian.xvr.util.config;

import java.util.*;
public class ConfigurationSection {
	public ConfigurationSection() { 
		properties = new ArrayList<ConfigurationProperty>();
	}
	
	String name;
	ConfigurationPage parentPage;
	
	List<ConfigurationProperty> properties;
}
