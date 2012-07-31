package com.avian.xvr.vxml;

import java.util.*;

public class VxmlTagMapFactory {

	public static Map<String,Integer> createMap() { 
		Map<String,Integer> tagMap = new HashMap<String,Integer>();
		tagMap.put("audio",IVxmlTags.AUDIO);
		tagMap.put("block",IVxmlTags.BLOCK);
		tagMap.put("form",IVxmlTags.FORM);
		tagMap.put("vxml",IVxmlTags.VXML);
		tagMap.put("prompt",IVxmlTags.PROMPT);
		tagMap.put("disconnect",IVxmlTags.DISCONNECT);
		tagMap.put("field",IVxmlTags.FIELD);
		tagMap.put("menu",IVxmlTags.MENU);
		tagMap.put("script",IVxmlTags.SCRIPT);
		tagMap.put("value",IVxmlTags.VALUE);
		tagMap.put("var",IVxmlTags.VAR);
		tagMap.put("filled",IVxmlTags.FILLED);
		tagMap.put("if",IVxmlTags.IF);
		tagMap.put("elseif",IVxmlTags.ELSEIF);
		tagMap.put("else",IVxmlTags.ELSE);
		tagMap.put("goto",IVxmlTags.GOTO);
		return tagMap;
	}
}
