package com.avian.xvr.grammar.srgs.jsapi;

import javax.speech.recognition.FinalRuleResult;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.GrammarListener;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.ResultListener;
import javax.speech.recognition.Rule;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleName;
import javax.speech.recognition.RuleParse;

import java.util.*;

public class SrgsRuleGrammar implements RuleGrammar {
	/**
	 * Creates an SrgsRuleGrammar. 
	 * @param ruleMap A map of rules, keyed by rule name to wrap this grammar around.
	 */
	public SrgsRuleGrammar(Map<String,SrgsRule> ruleMap) { 
		this.ruleMap = ruleMap;
	}
	
	/**
	 * Map of rules keyed on name
	 */
	Map<String,SrgsRule> ruleMap;		
	
	public void addImport(RuleName imp) {
		throw new RuntimeException("Not yet implemented.");
	}

	public void deleteRule(String arg0) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented.");			
	}

	public Rule getRule(String ruleName) {
		return ruleMap.get(ruleName).getBaseRule().copy();
	}

	public Rule getRuleInternal(String ruleName) {
		return ruleMap.get(ruleName).getBaseRule();
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isEnabled(String ruleName) throws IllegalArgumentException {
		return ruleMap.get(ruleName).isEnabled();
	}

	public boolean isRulePublic(String ruleName) throws IllegalArgumentException {
		return ruleMap.get(ruleName).isPublic();
	}

	public RuleName[] listImports() {
		return new RuleName[0];
	}

	public String[] listRuleNames() {
		return ruleMap.keySet().toArray(new String[0]);
	}

	public RuleParse parse(String text, String ruleName) throws GrammarException {
		throw new RuntimeException("Operation not supported.");
	}

	public RuleParse parse(FinalRuleResult arg0, int arg1, String arg2)
			throws GrammarException {
		throw new RuntimeException("Operation not supported.");
	}

	public RuleParse parse(String[] tokens, String ruleName) throws GrammarException {
		throw new RuntimeException("Not yet implemented.");
	}

	public void removeImport(RuleName imp) throws IllegalArgumentException {
		throw new RuntimeException("Not yet implemented.");
	}

	public RuleName resolve(RuleName ruleName) throws GrammarException {
//		throw new RuntimeException("Operation not supported.");
		return ruleName;
	}

	public Rule ruleForJSGF(String arg0) throws GrammarException {
		throw new RuntimeException("Not yet implemented.");
	}

	public void setEnabled(String ruleName, boolean enabled)
			throws IllegalArgumentException {
		throw new RuntimeException("Operation not supported.");
	}

	public void setEnabled(boolean enabled) {
		throw new RuntimeException("Operation not supported");
	}

	public void setEnabled(String[] ruleNames, boolean enabled)
			throws IllegalArgumentException {
		throw new RuntimeException("Operation not supported.");
	}

	public void setRule(String arg0, Rule arg1, boolean arg2)
			throws NullPointerException, IllegalArgumentException {
		throw new RuntimeException("Operatio not supported.");
	}

	public void addGrammarListener(GrammarListener listener) {
		throw new RuntimeException("Operation not supported.");
	}

	public void addResultListener(ResultListener listener) {
		throw new RuntimeException("Operation not supported.");
	}

	public int getActivationMode() {
		throw new RuntimeException("Operation not supported.");
	}

	public String getName() {
		return "SRGS Grammar";
	}

	public Recognizer getRecognizer() {
		throw new RuntimeException("Operation not supported.");
	}

	public boolean isActive() {
		return true;
	}

	public void removeGrammarListener(GrammarListener arg0) {
		throw new RuntimeException("Operation not supported");
	}

	public void removeResultListener(ResultListener arg0) {
		throw new RuntimeException("Operation not supported.");
	}

	public void setActivationMode(int arg0) throws IllegalArgumentException {
		throw new RuntimeException("Operation not supported.");
	}

}
