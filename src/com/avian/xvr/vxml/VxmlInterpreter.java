package com.avian.xvr.vxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.avian.xvr.interpreter.InterpreterException;
import com.avian.xvr.interpreter.ParseException;
import com.avian.xvr.interpreter.ScriptException;
import com.avian.xvr.interpreter.TransitionException;
import com.avian.xvr.media.IMediaChannel;
import com.avian.xvr.media.ISynthesizer;
import com.avian.xvr.media.RecognizerResult;
import com.avian.xvr.resource.IAudioResource;
import com.avian.xvr.resource.IDocumentResource;
import com.avian.xvr.resource.IResourceFetcher;
import com.avian.xvr.resource.ResourceFetchException;
import com.avian.xvr.signaling.ISignalingChannel;
import com.avian.xvr.signaling.SignalingException;

import java.net.MalformedURLException;
import java.net.URL;

public class VxmlInterpreter {
	private ISignalingChannel sigChannel;
	private IMediaChannel mediaChannel;
	private IResourceFetcher fetcher;
	private ISynthesizer synth;
	private Logger logger;
//	private Scripter scripter;
	private Thread intThread;
//	private String baseUri;
	
	private URL baseUrl,currentUrl;
	private VxmlDocument currentDocument;
	private VxmlInterpreterContext context;
	private static int threadIndex;
	
	public void go(final String initialUrl) {
		intThread = new Thread(
			new Runnable() { 
				public void run(){

					try {
						logger.debug("Starting interpreter...");
						
						boolean invalidResources = 
							( (sigChannel == null) || (mediaChannel == null) || 
								(fetcher == null) || (synth == null) );
						
						if(invalidResources) { 
							throw new InterpreterException("Interpreter initialized with invalid resources.");
						}
						
						fetcher.init();
						synth.init();
						mediaChannel = sigChannel.getMediaChannel();
						mediaChannel.init();
//						scripter.init();  //moved to VxmlInterpreterContext
						
						context = new VxmlInterpreterContext();
						
//						answer the call after we've fired everything up
						sigChannel.answer();
						
						performTransition(initialUrl);
					} catch(MalformedURLException e) { 
						logger.error(e.getMessage(),e);
					} catch (InterpreterException e) {
						logger.error(e.getMessage(),e);
					} catch (SignalingException e) {
						logger.error(e.getMessage(),e);
					} catch (ScriptException e) {
						logger.error(e.getMessage(),e);
					} catch (ResourceFetchException e) {
						logger.error(e.getMessage(),e);
					} catch (ParseException e) {
						logger.error(e.getMessage(),e);
					}
				}
			},"VxmlInterpreter-"+(threadIndex++)
		);
		
		intThread.start();
	}
	
	/**
	 * Performs a transition to the specified document or fragment.
	 * @param initialTarget
	 * @throws MalformedURLException
	 * @throws InterpreterException
	 * @throws ScriptException
	 * @throws ResourceFetchException
	 * @throws ParseException 
	 */
	private void performTransition(String initialTarget) throws MalformedURLException, InterpreterException, ScriptException, ResourceFetchException, ParseException  { 
//		is this is our first transition if the base url is null 
		if(baseUrl == null) { 
			if(initialTarget.startsWith("#")) { 
				throw new InterpreterException("Attempt to transition to a fragment as the initial document.");
			}
			baseUrl = new URL(initialTarget);
		} 
		
		String target = initialTarget;
		
		while(true) {
			if(target == null) { 
				logger.info("End of execution reached, disconnecting...");
				handleDisconnect(null);
				break;
			}
			
			logger.info("Transitioning to " + target + "...");
			
	//		set the current url in the context of the base url 
			currentUrl = new URL(baseUrl,target);
			
			try {
		//		are we staying within this document? 
				if(target.startsWith("#")) { //yes
					transitionToFragment();
				} else { 
					transitionToDocument();
				}
				
				//if we get this far, it means no one threw a TransitionException
				//and we've got no where left to go.
				target = null;
			} catch(TransitionException e) { 
				target = e.getTarget();
			}
		}
	}
	
	
	/**
	 * Transitions to the new document as specified in the currentUrl property.
	 * @throws ResourceFetchException 
	 * @throws ParseException 
	 * @throws InterpreterException 
	 * @throws ScriptException 
	 * @throws TransitionException 
	 *
	 */
	private void transitionToDocument() throws ResourceFetchException, ParseException, ScriptException, InterpreterException, TransitionException { 
		//TODO pop variable stack back to the session scope
		
		//TODO push new document scope
		
		
//		Fetch and parse our document located at currentUrl
		IDocumentResource vxmlDoc = fetcher.fetchDocument(currentUrl.toString());
		VxmlParser parser = new VxmlParser();
		currentDocument = parser.parse(vxmlDoc);
	
		//TODO check to see if the application root has changed, if so, reinitialize the application context and scope
		executeTopLevelElements(currentDocument.getDocumentElement());	
	}
	
	/**
	 * Transitions to the fragment as specified in the currentUrl property.
	 * @throws ResourceFetchException 
	 * @throws InterpreterException 
	 * @throws ScriptException 
	 * @throws TransitionException 
	 *
	 */
	private void transitionToFragment() throws ScriptException, InterpreterException, ResourceFetchException, TransitionException { 
		//TODO pop variable stack back to the document scope
		String fragName = currentUrl.getRef();
//		logger.info("Transitioning to fragment " + fragName);
		
		VxmlElement fragElement = currentDocument.getBookmark(fragName);
		switch(fragElement.getTag()) { 
		case IVxmlTags.FORM:
			handleForm(fragElement); break;
		case IVxmlTags.MENU:
			handleMenu(fragElement); break;
		}
	}
	
	public VxmlInterpreter(ISignalingChannel signalingChannel,
			IMediaChannel mediaChannel,
			IResourceFetcher resFetcher,
			ISynthesizer synth) { 

		this.sigChannel = signalingChannel;
		this.mediaChannel = mediaChannel;
		this.fetcher = resFetcher;
		this.synth = synth;
//		this.scripter = new Scripter();
	
		logger = Logger.getLogger(this.getClass());
	}
	
	private void executeTopLevelElements(VxmlElement vxml) throws ScriptException,InterpreterException, ResourceFetchException, TransitionException {
		VxmlElementIterator it = vxml.childElementIterator();
	
		while(it.hasNext()) { 
			VxmlElement e = it.next();
			
			switch(e.getTag()) { 
			case IVxmlTags.FORM:
				handleForm(e); break;
			case IVxmlTags.MENU:
				handleMenu(e); break;
			case IVxmlTags.SCRIPT:
				handleScript(e); break;
			}
		}
	}

	private void handleScript(VxmlElement script) {
		Iterator<VxmlNode> it = script.getChildren().iterator();
		
		/* get all the text data from inside the element
		 * (should only be one node, but just in case)
		 */
		StringBuilder scriptData = new StringBuilder();
		while(it.hasNext()) { 
			VxmlNode n = it.next();
			if(n.getType() == VxmlNode.TEXT) {
				scriptData.append(((VxmlText)n).getText());
			}
		}
		
		try {
			evalScript(scriptData.toString());
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	private void evalScript(String script) throws ScriptException {
		context.evalScript(script);
	}

	private void handleDisconnect(VxmlElement e) {
		try {
			Thread.sleep(3000);
			sigChannel.hangup();
		} catch (SignalingException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private void handleForm(VxmlElement form) throws ScriptException,InterpreterException, ResourceFetchException, TransitionException {
		logger.debug("Entering form: " + form);
		
		VxmlFormState formState = new VxmlFormState(form);

		handleFormInitPhase(formState);
		
		VxmlFormItem item ;
		
		// loop until we can't find an item with its guard condition 
		// unsatisfied
		while((item=handleFormSelectPhase(formState)) != null) {
			formState.setCurrentItem(item);
			handleFormCollectPhase(formState);
			handleFormProcessPhase(formState);
		}
			
			
	}
	
	private void handleFormProcessPhase(VxmlFormState state) throws InterpreterException,ScriptException,ResourceFetchException, TransitionException { 
		/* do any field-level <filled> elements */ 
		VxmlElementIterator iter = state.getCurrentItem().getElement().childElementIterator();
		List<VxmlElement> filledsToProcess = new ArrayList<VxmlElement>();
		
		while(iter.hasNext()) { 
			VxmlElement ele = iter.next();
			
			if(ele.getTag() == IVxmlTags.FILLED) {
				if(isFilledTriggered(ele,state)) filledsToProcess.add(ele);
			}
		}
		
		/* now let's find any form-level <filled> elements that match our current filled fields */ 
		iter = state.getFormElement().childElementIterator();
		
		while(iter.hasNext()) { 
			VxmlElement ele = iter.next();
			
			if(ele.getTag() == IVxmlTags.FILLED) {
				if(isFilledTriggered(ele,state)) filledsToProcess.add(ele);
			}
		}
		
		Iterator<VxmlElement> filledIter = filledsToProcess.iterator();
		while(filledIter.hasNext()) { 
			handleFilled(filledIter.next());
		}	
	}
	
	/**
	 * This method is called when, according to the FIA, the content within the specified <code>&lt;filled&gt;</code> element
	 * is ready to be executed.
	 * 
	 * @param filled The <code>&lt;filled&gt;</code> element containing the executable content.
	 * @throws InterpreterException 
	 * @throws TransitionException 
	 */
	private void handleFilled(VxmlElement filled) throws ResourceFetchException, ScriptException, InterpreterException, TransitionException{ 
		logger.debug("Entering " + filled);
		handleExecutableContent(filled);
	}
	
	private boolean isFilledTriggered(VxmlElement filled,VxmlFormState state) throws InterpreterException,ScriptException { 
		//build a namelist of all form items
		//TODO refactor into calling method
		Collection<VxmlFormItem> items = state.getFormItems();
		StringBuilder namesBuilder = new StringBuilder();
		int i = 0;
		for(VxmlFormItem formItem : items) { 
			if(i > 0) { 
				namesBuilder.append(" ");
			}
			
			namesBuilder.append(formItem.getElement().getAttribute("name"));
			i++;
		}
		
		String allNames = namesBuilder.toString();
		String namesToCheck;
		
		if(filled.getAttribute("namelist") == null) { 
			namesToCheck = allNames;
		} else { 
			namesToCheck = filled.getAttribute("namelist");
		}
		
		String[] itemNames = namesToCheck.split(" ");
		
		String mode = filled.getAttribute("mode");
		if(mode == null || mode.equals("any")) { 
			for(String itemName:itemNames) { 
				if(isScriptVarDefined(itemName)) {
					return true;
				}
			}
			
			return false;
		} else if(mode.equals("all")) {
			for(String itemName:itemNames) { 
				if(isScriptVarDefined(itemName)) {
					continue;
				} else {
					return false;
				}
			}
			
			return true;
		} else { 
			throw new InterpreterException("Element " + filled + " has an invalid mode specified.");
		}
	}

	private boolean isScriptVarDefined(String varName) throws ScriptException {
		return context.isScriptVarDefined(varName);
	}
	
	private void handleFormCollectPhase(VxmlFormState state) throws ScriptException, InterpreterException, ResourceFetchException, TransitionException {
//	    unless ( the last loop iteration ended with
//	            a catch that had no <reprompt>, 
//	        and the active dialog was not changed )
//	    {
//
//	        Select the appropriate prompts for an input item or <initial>.
//	        Queue the selected prompts for play prior to
//	        the next collect operation.
//
//	        Increment an input item's or <initial>'s prompt counter.
//	    }
//
//	    // Activate grammars for the form item.
//
//	    if ( the form item is modal )
//	        Set the active grammar set to the form item grammars,
//	        if any. (Note that some form items, e.g. <block>,
//	        cannot have any grammars).
//	    else
//	        Set the active grammar set to the form item
//	        grammars and any grammars scoped to the form,
//	        the current document, and the application root
//	        document.
//
//	    // Execute the form item.
//
//	    if ( a <field> was selected )
//	        Collect an utterance or an event from the user.
//	    else if ( a <record> was chosen )
//	        Collect an utterance (with a name/value pair
//	        for the recorded bytes) or event from the user.
//	    else if ( an <object> was chosen )
//	        Execute the object, setting the <object>'s
//	        form item variable to the returned ECMAScript value.
//	    else if ( a <subdialog> was chosen )
//	        Execute the subdialog, setting the <subdialog>'s
//	        form item variable to the returned ECMAScript value.
//	    else if ( a <transfer> was chosen )
//	        Do the transfer, and (if wait is true) set the
//	        <transfer> form item variable to the returned
//	        result status indicator.
//	    else if ( an <initial> was chosen )
//	        Collect an utterance or an event from the user.
//	    else if ( a <block> was chosen )
//	    {
//	        Set the block's form item variable to a defined value.
//	        Execute the block's executable context.
//	    }

		VxmlElement itemElement = state.getCurrentItem().getElement();
		switch(itemElement.getTag()) {
		case IVxmlTags.BLOCK:
			setScriptVar(itemElement.getAttribute("name"), "true");
			handleBlock(itemElement);
			mediaChannel.playAudio();
			return;
		case IVxmlTags.FIELD:
			logger.debug("Entering field: " + itemElement);
			activateFieldGrammars(itemElement);
			queueFieldPrompts(itemElement);
			mediaChannel.playAudio();
			RecognizerResult r = mediaChannel.waitForInput();
			logger.info("Setting field \""+itemElement.getAttribute("name")+"\" to \""+r.getResult()+"\"");
			context.setScriptVar(itemElement.getAttribute("name"),"'"+r.getResult()+"'");
			return;
		}
	}

	private void setScriptVar(String varName, String varValue) throws ScriptException {
		context.setScriptVar(varName,varValue);
	}

	private void activateFieldGrammars(VxmlElement field) { 
		
	}
	
	private void queueFieldPrompts(VxmlElement field) throws ResourceFetchException { 
		VxmlElementIterator iter = field.childElementIterator();
		while(iter.hasNext()) {
			VxmlElement prompt = iter.next();
			switch(prompt.getTag()) {
			case IVxmlTags.AUDIO: handleAudio(prompt);
			case IVxmlTags.PROMPT: handlePrompt(prompt);
			}
		}
	}
	
	private void handleFormInitPhase(VxmlFormState state) throws ScriptException, InterpreterException { 
		 
//		  --- Initialization Phase ---
//		foreach ( <var>, <script> and form item, in document order )
//		   if ( the element is a <var> )
//		     Declare the variable, initializing it to the value of
//		     the "expr" attribute, if any, or else to undefined.
//		   else if ( the element is a <script> )
//		     Evaluate the contents of the script if inlined or else
//		     from the location specified by the "src" attribute.
//		   else if ( the element is a form item )
//		     Create a variable from the "name" attribute, if any, or
//		     else generate an internal name.  Assign to this variable
//		     the value of the "expr" attribute, if any, or else undefined.
//		            foreach ( input item and <initial> element )
//		                 Declare a prompt counter and set it to 1.
		                
		VxmlElementIterator elements = state.getFormElement().childElementIterator();
		while(elements.hasNext()) {
			VxmlElement e = elements.next();
			switch(e.getTag()) {
			case IVxmlTags.SCRIPT:
				handleScript(e); break;
			case IVxmlTags.VAR:
				handleVar(e); break;
			case IVxmlTags.FIELD:
			case IVxmlTags.BLOCK:
			case IVxmlTags.SUBDIALOG:
				//implicitly sets prompt counter to 1
				state.addItem(new VxmlFormItem(e));
				
				//set the form items variable
				declareScriptVar(e.getAttribute("name"), e.getAttribute("expr"));
			}
		}		
	}

	private void declareScriptVar(String varName, String varExpr) throws ScriptException {
		context.declareScriptVar(varName,varExpr);
	}
	
	private boolean isElementFormItem(VxmlElement e) { 
		switch(e.getTag()) { 
		case IVxmlTags.BLOCK:
		case IVxmlTags.FIELD:
		case IVxmlTags.SUBDIALOG:
			return true;
		default:
			return false;
		}
	}
	
	private VxmlFormItem handleFormSelectPhase(VxmlFormState state) throws ScriptException { 
//		--- Select Phase ---
//	    if ( the last main loop iteration ended
//	              with a <goto nextitem> )
//	        Select that next form item.
//
//	    else if (there is a form item with an
//	              unsatisfied guard condition )
//	        Select the first such form item in document order.
//
//	    else
//	        Do an <exit/> -- the form is full and specified no transition.
		Iterator<VxmlFormItem> items = state.itemIterator();
		while(items.hasNext()) {
			VxmlFormItem fi = items.next();
			if(isElementFormItem(fi.getElement())) {
				if(!isScriptVarDefined(fi.getElement().getAttribute("name"))) {
					//field var is not defined; unsatisfied.
					return fi;
				}
			}
			
		}
		
		return null;
	}
	
	private void handleVar(VxmlElement var) throws ScriptException {
		declareScriptVar(var.getAttribute("name"),var.getAttribute("expr"));
	}

	private void handleBlock(VxmlElement block) throws ResourceFetchException, InterpreterException, ScriptException, TransitionException {
		logger.debug("Entering block: " + block);
		handleExecutableContent(block);
	}
	
	private void handleGoto(VxmlElement g) throws TransitionException {
		StringBuilder target = new StringBuilder();
		if(g.hasAttribute("next")) { 
			throw new TransitionException(g.getAttribute("next"));
		}
	}
	private void handleIf(VxmlElement e) throws InterpreterException, ScriptException, ResourceFetchException, TransitionException { 
		String ifCond = e.getAttribute("cond");
		
		if(ifCond != null) {
			boolean executing,hasExecuted;
			if(evalScriptToString(ifCond).equals("true")) {
				executing = true;
				hasExecuted = true;
			} else { 
				executing = false;
				hasExecuted = false;
			}
			
			VxmlElementIterator ifIter = e.childElementIterator();
			
			while(ifIter.hasNext()) { 
				VxmlElement child = ifIter.next();
				
				switch(child.getTag()) { 
					case IVxmlTags.ELSEIF:
						{
							if(hasExecuted) return;
							String elseIfCond = child.getAttribute("cond");
							if(elseIfCond != null) { 
								if(evalScriptToString(elseIfCond).equals("true")) {
									executing = true;
									hasExecuted = true;
								}
							} else { 
								throw new InterpreterException("Missing condition in elseif element: " + child);
							}
						}
					break;
					
					case IVxmlTags.ELSE:
						{
							if(hasExecuted) return;
							executing=true;
						}
					break;
					
					default:
						if(executing) 
							handleExecutableElement(child);
					break;
				}
					
				}
				
		} else { 
			throw new InterpreterException("Missing condition in if element: " + e);
		}
	}

	private String evalScriptToString(String expr) throws ScriptException {
		return context.evalScriptToString(expr);
	}
	
	private void handleExecutableElement(VxmlElement element) throws ResourceFetchException, InterpreterException, ScriptException, TransitionException { 
		switch(element.getTag()) { 
		case IVxmlTags.PROMPT:
			handlePrompt(element);
			break;
		case IVxmlTags.AUDIO:
			handleAudio(element);
			break;
		case IVxmlTags.IF:
			handleIf(element);
			break;
		case IVxmlTags.GOTO:
			handleGoto(element);
			break;
		case IVxmlTags.DISCONNECT:
			handleDisconnect(element);
			break;
		}		
	}
	
	/**
	 * Executes the content inside specified element
	 * @param element Element that contains content to be executed.
	 * @throws TransitionException 
	 */
	private void handleExecutableContent(VxmlElement element) throws ResourceFetchException, InterpreterException, ScriptException, TransitionException { 
		VxmlElementIterator it = element.childElementIterator();
		
		while(it.hasNext()) { 
			VxmlElement e = it.next();
			handleExecutableElement(e);
		}
		
		mediaChannel.playAudio();
	}

	private void handleAudio(VxmlElement audio) throws ResourceFetchException { 
		IAudioResource audioFile = fetcher.fetchAudio(audio.getAttribute("src"));
		mediaChannel.queueAudio(audioFile);
	}
	
	private void handlePrompt(VxmlElement prompt) {
		logger.debug("Entering prompt: " + prompt);
		
		Iterator<VxmlNode> it = prompt.getChildren().iterator();
		
		StringBuilder promptText = new StringBuilder();
		try {
			while(it.hasNext()) {
				VxmlNode n = it.next();
				
				if(n.getType() == VxmlNode.TEXT) {
					VxmlText t = (VxmlText) n;
					promptText.append(t.getText());
				} else if(n.getType() == VxmlNode.ELEMENT) {
					VxmlElement e = (VxmlElement)n;
					switch(e.getTag()) { 
					case IVxmlTags.VALUE:
						promptText.append(evalScriptToString(e.getAttribute("expr")));
						break;
					}
				}
			}
			
			queueSynth(promptText.toString());
		} catch(ScriptException e) { 
			e.printStackTrace();
		}
	}

	private void queueSynth(String text) {
		logger.debug("Queueing speech: " + text);
		mediaChannel.queueAudio(synth.getSynthesizedText(text));
	}

	private void handleMenu(VxmlElement menu) {
	}
}
