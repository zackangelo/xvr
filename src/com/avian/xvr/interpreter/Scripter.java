package com.avian.xvr.interpreter;

import org.apache.log4j.Logger;
import org.mozilla.javascript.*;


import java.util.Stack;

/**
 * Class used to interact with Rhino for ECMAScript interpreting.  We'll
 * probably be able to reuse this object for CCXML as well.
 * 
 * @author Zack
 *
 */
public class Scripter {

	private static class ScopeData { 
		Scriptable scope;
	}

	/**
	 * Rhino execution context
	 */
	private Context context;

	private Logger logger;
	private Stack<ScopeData> scopeStack;
	private ScopeData rootScope;
	private ScopeData currentScope;
	
	public Scripter() { 
		logger = Logger.getLogger(this.getClass());
		scopeStack = new Stack<ScopeData>();
	}
	
	public void init() { 
		logger.debug("Creating initial ECMA scope.");
		context = Context.enter();
		rootScope = new ScopeData();
		rootScope.scope = context.initStandardObjects();
		currentScope = rootScope;		
	}
	
	public void pushScope(String name) throws ScriptException { 
		try {
			ScopeData newScope = new ScopeData();
			newScope.scope = context.newObject(currentScope.scope);
			newScope.scope.setParentScope(currentScope.scope);
			scopeStack.push(currentScope);
			currentScope = newScope;
		} catch (EvaluatorException e) {
			throw new ScriptException("Error creating new scope.",e);
		} catch (JavaScriptException e) {
			throw new ScriptException("Error creating new scope.",e);
		}
	}
	
	public void popScope() { 
		currentScope = scopeStack.pop();
	}
	
	public Object evalScript(String expr) throws ScriptException { 
		try {
			return context.evaluateString(currentScope.scope,expr,"<cmd>",1,null);
		} catch (JavaScriptException e) {
			throw new ScriptException("Error evaluating expression or script",e);
		}
	}
	
	public String evalScriptToString(String expr) throws ScriptException {
		if (expr == null || (expr.length() == 0) /*|| (!isVarDefined(expr)) || expr.equals("ccxml")*/) {
			return null;
		}
		Object rc = evalScript(expr);
		return Context.toString(rc);
	}
	
	public void declareVar(String name,String val) throws ScriptException { 
		if(val == null) { 
			val = "undefined";
		}
		
		evalScript("var " + name + " = " + val);
	}
	
	public boolean isVarDefined(String name) throws ScriptException { 
		return !Context.getUndefinedValue().equals(evalScript(name));
	}

	public void setVar(String name, String val) throws ScriptException {
		if(val == null) { 
			val = "undefined";
		}
		
		evalScript(name + " = " + val);
		
	}
}
