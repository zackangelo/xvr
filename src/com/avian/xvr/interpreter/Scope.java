/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.xvr.interpreter;

import java.util.*;
/**
 * @author Zack
 *
 */
public class Scope {

	public Scope(String name,Scope parent) { 
		this.parent = parent;
		this.name = name;
		
		operations = new ArrayList<Operation>();
		subscopes = new HashMap<String,Scope>();
		eventHandlers = new HashMap<String,Scope>();
		variables = new HashMap<String,String>();
	}
	
	public void appendOperation(Operation o) { 
		operations.add(o);
	}
	
	public List<Operation> getOperations() { 
		return operations;
	}
	
	public void appendSubscope(String n,Scope s) { 
		subscopes.put(n,s);
	}
	
	public Scope getEventHandler(String eventName) { 
		return eventHandlers.get(eventName);
	}
	
	public String getVariableValue(String name) { 
		return variables.get(name);
	}
	
	public Scope getParent() { 
		return parent;
	}
	
	List<Operation> operations;	
	Map<String,Scope> subscopes;
	Map<String,Scope> eventHandlers;
	
	//note: will probably store the variables in an 
	//ecmascript context in the future
	Map<String,String> variables; 
	
	String name;
	Scope parent;
}
