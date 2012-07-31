package com.avian.xvr.vxml;

import com.avian.xvr.interpreter.ScriptException;
import com.avian.xvr.interpreter.Scripter;
import java.util.*;

public class VxmlInterpreterContext {
	public final static String SESSION_SCOPE_NAME = "session";
	public final static String APPLICATION_SCOPE_NAME = "application";
	public final static String DOCUMENT_SCOPE_NAME = "document";
	
	static class Scope {
		public Scope(String name) { 
			this.name = name;
			eventHandlers = new HashMap<String,VxmlElement>();
		}
		
		/* name of the scope, null if anonymous */
		String name;	
		Map<String,VxmlElement> eventHandlers;
	}
	
	static class Context { 
		public Context() { 
			scopes = new Stack<Scope>();
			scripter = new Scripter();
			
			scripter.init();
			currentScope = new Scope(null);
		}
		
		Scripter scripter;
		Stack<Scope> scopes;
		
		Scope currentScope;
	}
	
	public VxmlInterpreterContext() { 
		contexts = new Stack<Context>();
		currentContext = new Context();
	}
	
	Stack<Context> contexts;
	Context currentContext;
	
	
	/**
	 * We primarily use this method when transitioning to a subdialog.
	 * The entire script scope and scope stack are pushed, and a new
	 * execution environment is created. When the subdialog is finished,
	 * the it's scope is popped and the calling scope is restored.
	 *
	 */
	public void pushContext() { 
		contexts.push(currentContext);
		currentContext = new Context();
	}
	
	public void popContext() { 
		Context popped = contexts.pop();
		currentContext = popped;
	}
	
	public void pushScope() throws ScriptException { 
		pushScope(null);
	}
	
	public void pushScope(String name) throws ScriptException { 
		currentContext.scopes.push(currentContext.currentScope);
		currentContext.currentScope = new Scope(name);
		
		currentContext.scripter.pushScope(name);
	}
	
	public void popScope() {
		currentContext.currentScope = currentContext.scopes.pop();
		currentContext.scripter.popScope();
	}
	
	public void popScopeToDocument() { }
	public void popScopeToApplication() { }
	public void popScopeToSession() { }
	
	public String evalScriptToString(String script) throws ScriptException { 
		return currentContext.scripter.evalScriptToString(script); 
	}
	
	public void evalScript(String script) throws ScriptException { 
		currentContext.scripter.evalScript(script); 
	}
	
	public void declareScriptVar(String varName,String expr) throws ScriptException { 
		currentContext.scripter.declareVar(varName,expr); 
	}
	
	public boolean isScriptVarDefined(String varName) throws ScriptException { 
		return currentContext.scripter.isVarDefined(varName); 
	}
	
	public void setScriptVar(String varName,String value) throws ScriptException { 
		currentContext.scripter.setVar(varName,value); 
	}
	
	public void registerEventHandler(String name,VxmlElement handler) { 
		currentContext.currentScope.eventHandlers.put(name,handler);
	}
	
	public VxmlElement findEventHandler(String name) { 
		Iterator<Scope> scopeIter = currentContext.scopes.iterator();
		
		while(scopeIter.hasNext()) { 
			Scope scope = scopeIter.next();
			if(scope.eventHandlers.containsKey(name)) {
				return scope.eventHandlers.get(name);
			}
		}
		
		return null;
	}
}
