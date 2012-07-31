package com.avian.xvr.grammar.srgs.jsapi;

import javax.speech.recognition.Rule;

public class SrgsRule {
	public SrgsRule(String name,Rule rule,boolean ispublic) { 
		this.name = name;
		this.baseRule = rule;
		this.ispublic = ispublic;
		this.enabled = true;
	}
	
	String name;
	Rule baseRule;
	boolean ispublic;
	boolean enabled;
	
	public Rule getBaseRule() {
		return baseRule;
	}
	
	public void setBaseRule(Rule baseRule) {
		this.baseRule = baseRule;
	}
	
	public boolean isPublic() {
		return ispublic;
	}
	public void setPublic(boolean ispublic) {
		this.ispublic = ispublic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
