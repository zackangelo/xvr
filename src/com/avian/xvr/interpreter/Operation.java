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
public class Operation {
	
	public Operation(int type) { 
		this.type = type;
		params = new HashMap<String,String>();
	}
	
	/* operation types */
	public final static int QUEUE_AUDIO = 0;
	public final static int PLAY_AUDIO = 1;
	public final static int WAIT_FOR_INPUT = 2;
	public final static int LOG = 3;
	public final static int SPAWN = 4;
	public final static int EXEC_SUBSCOPE = 5;
	public final static int HANGUP = 6;
	public final static int QUEUE_SYNTH = 7;
	public final static int LOAD_GRAMMAR = 8;
	public final static int GOTO_IF_FALSE = 9;
	public static final int GOTO = 10;
	
	
	public int getType() { 
		return type;
	}
	
	public void setParam(String param,String value) { 
		params.put(param,value);
	}
	
	public String getParam(String param) { 
		return params.get(param);
	}
	
	public boolean paramExists(String param) { 
		return params.containsKey(param);
	}
	
	public void setData(String data) { 
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	int type;
	String data;
	Map<String,String> params;
	
	public static final String PARAM_TEXT = "text";
	public static final String PARAM_GRAMMAR_DATA = "grammar-data";
	public static final String PARAM_GOTO_TARGET = "goto-target";
	public static final String PARAM_COND = "cond";

	
}
