package com.avian.xvr.util.config;

import org.eclipse.jface.viewers.LabelProvider;

public class PageTreeLabelProvider extends LabelProvider {
  public String getText(Object element)
  {
	  if(element instanceof ConfigurationSchema) { 
		  return "Server Configuration";
	  } else {
		return ((ConfigurationPage)element).name;
	  }
  }
}
