package com.avian.xvr.interpreter;

import java.util.*;

public class Executable {

	public Executable(List<Operation> ops,Map<String,Integer> bookmarks) {
		this.bookmarks = bookmarks;
		this.ops = ops;
	}
	
	List<Operation> ops;
	Map<String,Integer> bookmarks;
}
