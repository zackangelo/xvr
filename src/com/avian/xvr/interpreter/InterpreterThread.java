/*
 * Created on Apr 6, 2005
 *
 */
package com.avian.xvr.interpreter;

import com.avian.xvr.vxml.VxmlDocument;
import com.avian.xvr.vxml.VxmlInterpreter;

/**
 * @author Zack
 *
 *(Moved threading into VxmlInterpreter.java)
 */
public class InterpreterThread extends Thread {
	VxmlInterpreter inter;
	VxmlDocument doc;
	
	public InterpreterThread(VxmlInterpreter inter,VxmlDocument doc) {
		this.inter = inter;
		this.doc = doc;
	}
	
	public void run() {
//		try {
////			inter.execute(doc);
//		} catch (InterpreterException e) {
//			e.printStackTrace();
//		}
	}
}
