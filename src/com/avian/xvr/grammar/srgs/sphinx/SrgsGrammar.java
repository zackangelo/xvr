/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

package com.avian.xvr.grammar.srgs.sphinx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import javax.speech.EngineException;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.GrammarSyntaxDetail;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.Rule;
import javax.speech.recognition.RuleAlternatives;
import javax.speech.recognition.RuleCount;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleName;
import javax.speech.recognition.RuleParse;
import javax.speech.recognition.RuleSequence;
import javax.speech.recognition.RuleTag;
import javax.speech.recognition.RuleToken;

import com.avian.xvr.grammar.srgs.SrgsParser;
import com.avian.xvr.resource.IDocumentResource;
import com.avian.xvr.resource.LocalResourceFetcher;
import com.avian.xvr.resource.ResourceFetchException;
import com.sun.speech.engine.recognition.BaseRecognizer;

import edu.cmu.sphinx.linguist.language.grammar.Grammar;
import edu.cmu.sphinx.linguist.language.grammar.GrammarNode;
import edu.cmu.sphinx.util.LogMath;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.PropertyType;
import edu.cmu.sphinx.util.props.Registry;

public class SrgsGrammar extends Grammar {

    /**
     * Sphinx property that defines the location of the JSGF grammar file.
     */
    public final static String PROP_BASE_GRAMMAR_URL = "grammarLocation";

    /**
     * Sphinx property that defines the location of the JSGF grammar file.
     */
    public final static String PROP_GRAMMAR_NAME = "grammarName";

    /**
     * Default value for PROP_GRAMMAR_NAME
     */
    public final static String PROP_GRAMMAR_NAME_DEFAULT = "default.gram";
    
    /**
     * Sphinx property that defines the logMath component. 
     */
    
    public final static String PROP_LOG_MATH = "logMath";
    

    // ---------------------
    // Configurable data
    // ---------------------
    private RuleGrammar ruleGrammar;
    private RuleStack ruleStack;
    private Recognizer recognizer;
    private String grammarName;
    private URL baseURL = null;
    private LogMath logMath;

    private boolean loadGrammar = true;
    private GrammarNode firstNode = null;

    /*
     * (non-Javadoc)
     * 
     * @see edu.cmu.sphinx.util.props.Configurable#register(java.lang.String,
     *      edu.cmu.sphinx.util.props.Registry)
     */
    public void register(String name, Registry registry)
            throws PropertyException {
        super.register(name, registry);
        registry.register(PROP_BASE_GRAMMAR_URL, PropertyType.RESOURCE);
        registry.register(PROP_GRAMMAR_NAME, PropertyType.STRING);
        registry.register(PROP_LOG_MATH, PropertyType.COMPONENT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.cmu.sphinx.util.props.Configurable#newProperties(edu.cmu.sphinx.util.props.PropertySheet)
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        baseURL = ps.getResource(PROP_BASE_GRAMMAR_URL);
        logMath = (LogMath) ps.getComponent(PROP_LOG_MATH, LogMath.class);

        grammarName = ps
                .getString(PROP_GRAMMAR_NAME, PROP_GRAMMAR_NAME_DEFAULT);
        loadGrammar = true;
    }

    /**
     * Returns the RuleGrammar of this JSGFGrammar.
     * 
     * @return the RuleGrammar
     */
    public RuleGrammar getRuleGrammar() {
        return ruleGrammar;
    }

    /**
     * Sets the URL context of the JSGF grammars.
     *
     * @param url the URL context of the grammars
     */
    public void setBaseURL(URL url) {
        baseURL = url;
    }

    /**
     * The JSGF grammar specified by grammarName will be loaded from
     * the base url (tossing out any previously loaded grammars)
     *
     * @param grammarName the name of the grammar
     * @throws IOException if an error occurs while loading or
     * compiling the grammar
     */
    public void loadJSGF(String grammarName) throws IOException {
        this.grammarName = grammarName;
        loadGrammar = true;
        commitChanges();
    }

    /**
     * Creates the grammar.
     * 
     * @return the initial node of the Grammar
     */
    protected GrammarNode createGrammar() throws IOException {
        commitChanges();
        return firstNode;
    }

    /**
     * Returns the initial node for the grammar
     * 
     * @return the initial grammar node
     */
    public GrammarNode getInitialNode() {
        return firstNode;
    }

    /**
     * Parses the given Rule into a network of GrammarNodes.
     * 
     * @param rule
     *                the Rule to parse
     * 
     * @return a grammar graph
     */
    private GrammarGraph parseRule(Rule rule) throws GrammarException {
        GrammarGraph result;

        if (rule != null) {
            debugPrintln("parseRule: " + rule.toString());
        }

        if (rule instanceof RuleAlternatives) {
            result = parseRuleAlternatives((RuleAlternatives) rule);
        } else if (rule instanceof RuleCount) {
            result = parseRuleCount((RuleCount) rule);
        } else if (rule instanceof RuleName) {
            result = parseRuleName((RuleName) rule);
        } else if (rule instanceof RuleSequence) {
            result = parseRuleSequence((RuleSequence) rule);
        } else if (rule instanceof RuleTag) {
            result = parseRuleTag((RuleTag) rule);
        } else if (rule instanceof RuleToken) {
            result = parseRuleToken((RuleToken) rule);
        } else if (rule instanceof RuleParse) {
            throw new IllegalArgumentException(
                    "Unsupported Rule type: RuleParse: " + rule.toString());
        } else {
            throw new IllegalArgumentException("Unsupported Rule type: "
                    + rule.toString());
        }
        return result;
    }

    /**
     * Parses the given RuleName into a network of GrammarNodes.
     * 
     * @param initialRuleName
     *                the RuleName rule to parse
     * 
     * @return a grammar graph
     */
    private GrammarGraph parseRuleName(RuleName initialRuleName)
            throws GrammarException {
        debugPrintln("parseRuleName: " + initialRuleName.toString());
        GrammarGraph result = (GrammarGraph) ruleStack.contains(initialRuleName
                .getRuleName());

        if (result != null) { // its a recursive call
            return result;
        } else {
            result = new GrammarGraph();
            ruleStack.push(initialRuleName.getRuleName(), result);
        }
        RuleName ruleName = ruleGrammar.resolve(initialRuleName);

        if (ruleName == RuleName.NULL) {
            result.getStartNode().add(result.getEndNode(), 0.0f);
        } else if (ruleName == RuleName.VOID) {
            // no connection for void
        } else {
            if (ruleName == null) {
                throw new GrammarException("Can't resolve " + initialRuleName
                        + " g " + initialRuleName.getFullGrammarName());
            }
            
          /* it appears that it goes to the recognizer here to get external grammars, since all of
           	grammars are local for now, let's just use the RuleGrammar we already have. */
//            RuleGrammar rg = recognizer.getRuleGrammar(ruleName
//                    .getFullGrammarName());
//            if (rg == null) {
//                throw new GrammarException("Can't resolve grammar name "
//                        + ruleName.getFullGrammarName());
//            }

            Rule rule = ruleGrammar.getRule(ruleName.getSimpleRuleName());
            if (rule == null) {
                throw new GrammarException("Can't resolve rule: "
                        + ruleName.getRuleName());
            }
            GrammarGraph ruleResult = parseRule(rule);
            if (result != ruleResult) {
                result.getStartNode().add(ruleResult.getStartNode(), 0.0f);
                ruleResult.getEndNode().add(result.getEndNode(), 0.0f);
            }
        }
        ruleStack.pop();
        return result;
    }

    /**
     * Parses the given RuleCount into a network of GrammarNodes.
     * 
     * @param ruleCount
     *                the RuleCount object to parse
     * 
     * @return a grammar graph
     */
    private GrammarGraph parseRuleCount(RuleCount ruleCount)
            throws GrammarException {
        debugPrintln("parseRuleCount: " + ruleCount);
        GrammarGraph result = new GrammarGraph();
        int count = ruleCount.getCount();
        GrammarGraph newNodes = parseRule(ruleCount.getRule());

        result.getStartNode().add(newNodes.getStartNode(), 0.0f);
        newNodes.getEndNode().add(result.getEndNode(), 0.0f);

        // if this is optional, add a bypass arc

        if (count == RuleCount.ZERO_OR_MORE || count == RuleCount.OPTIONAL) {
            result.getStartNode().add(result.getEndNode(), 0.0f);
        }

        // if this can possibly occur more than once, add a loopback

        if (count == RuleCount.ONCE_OR_MORE || count ==RuleCount.ZERO_OR_MORE) {
            newNodes.getEndNode().add(newNodes.getStartNode(), 0.0f);
        }
        return result;
    }

    /**
     * Parses the given RuleAlternatives into a network of GrammarNodes.
     * 
     * @param ruleAlternatives
     *                the RuleAlternatives to parse
     * 
     * @return a grammar graph
     */
    private GrammarGraph parseRuleAlternatives(RuleAlternatives ruleAlternatives)
            throws GrammarException {
        debugPrintln("parseRuleAlternatives: " + ruleAlternatives.toString());
        GrammarGraph result = new GrammarGraph();

        Rule[] rules = ruleAlternatives.getRules();
        float[] weights = ruleAlternatives.getWeights();
        normalizeWeights(weights);

        // expand each alternative, and connect them in parallel
        for (int i = 0; i < rules.length; i++) {
            Rule rule = rules[i];
            float weight = 0.0f;
            if (weights != null) {
                weight = weights[i];
            }
            debugPrintln("Alternative: " + rule.toString());
            GrammarGraph newNodes = parseRule(rule);
            result.getStartNode().add(newNodes.getStartNode(), weight);
            newNodes.getEndNode().add(result.getEndNode(), 0.0f);
        }

        return result;
    }

    /**
     * Normalize the weights. The weights should always be zero or greater. We
     * need to convert the weights to a log probability.
     * 
     * @param weights
     *                the weights to normalize
     */
    private void normalizeWeights(float[] weights) {
        if (weights != null) {
            double sum = 0.0;
            for (int i = 0; i < weights.length; i++) {
                if (weights[i] < 0) {
                    throw new IllegalArgumentException("negative weight");
                }
                sum += weights[i];
            }
            for (int i = 0; i < weights.length; i++) {
                if (sum == 0.0f) {
                    weights[i] = LogMath.getLogZero();
                } else {
                    weights[i] = logMath.linearToLog(weights[i] / sum);
                }
            }
        }
    }

    /**
     * Parses the given RuleSequence into a network of GrammarNodes.
     * 
     * @param ruleSequence
     *                the RuleSequence to parse
     * 
     * @return the first and last GrammarNodes of the network
     */
    private GrammarGraph parseRuleSequence(RuleSequence ruleSequence)
            throws GrammarException {

        GrammarNode startNode = null;
        GrammarNode endNode = null;
        debugPrintln("parseRuleSequence: " + ruleSequence);

        Rule[] rules = ruleSequence.getRules();

        GrammarNode lastGrammarNode = null;

        // expand and connect each rule in the sequence serially
        for (int i = 0; i < rules.length; i++) {
            Rule rule = rules[i];
            GrammarGraph newNodes = parseRule(rule);

            // first node
            if (i == 0) {
                startNode = newNodes.getStartNode();
            }

            // last node
            if (i == (rules.length - 1)) {
                endNode = newNodes.getEndNode();
            }

            if (i > 0) {
                lastGrammarNode.add(newNodes.getStartNode(), 0.0f);
            }
            lastGrammarNode = newNodes.getEndNode();
        }

        return new GrammarGraph(startNode, endNode);
    }

    /**
     * Parses the given RuleTag into a network GrammarNodes.
     * 
     * @param ruleTag
     *                the RuleTag to parse
     * 
     * @return the first and last GrammarNodes of the network
     */
    private GrammarGraph parseRuleTag(RuleTag ruleTag) throws GrammarException {
        debugPrintln("parseRuleTag: " + ruleTag);
        Rule rule = ruleTag.getRule();
        return parseRule(rule);
    }

    /**
     * Creates a GrammarNode with the word in the given RuleToken.
     * 
     * @param ruleToken
     *                the RuleToken that contains the word
     * 
     * @return a GrammarNode with the word in the given RuleToken
     */
    private GrammarGraph parseRuleToken(RuleToken ruleToken) {

        GrammarNode node = createGrammarNode(ruleToken.getText());
        return new GrammarGraph(node, node);
    }

    /**
     * Dumps out a grammar exception
     * 
     * @param ge
     *                the grammar exception
     *  
     */
    private void dumpGrammarException(GrammarException ge) {
        System.out.println("Grammar exception " + ge);
        GrammarSyntaxDetail[] gsd = ge.getDetails();
        if (gsd != null) {
            for (int i = 0; i < gsd.length; i++) {
                System.out.println("Grammar Name: " + gsd[i].grammarName);
                System.out.println("Grammar Loc : " + gsd[i].grammarLocation);
                System.out.println("Import Name : " + gsd[i].importName);
                System.out.println("Line number : " + gsd[i].lineNumber);
                System.out.println("char number : " + gsd[i].charNumber);
                System.out.println("Rule name   : " + gsd[i].ruleName);
                System.out.println("Message     : " + gsd[i].message);
            }
        }
    }

    /**
     *  Commit changes to all loaded grammars and all changes of
     *  grammar since the last commitChange
     */
    public void commitChanges() throws IOException {
        try {
            if (loadGrammar) {
//                recognizer = new BaseRecognizer();
//                recognizer.allocate();
//                ruleGrammar = recognizer.loadJSGF(baseURL, grammarName);
//                ruleGrammar.setEnabled(true);
            	
            	SrgsParser srgsParser = new SrgsParser();
            	LocalResourceFetcher f = new LocalResourceFetcher();
            	
            	try {
					IDocumentResource res = f.fetchDocument("src/com/avian/xvr/grammar/srgs/politecmd.grxml");
					ruleGrammar = srgsParser.parse(res);
            	} catch (Throwable e) {
            		e.printStackTrace();
					throw new IOException("Couldn't load grammar document.");
				}
            	
                loadGrammar = false;
            }

//            recognizer.commitChanges();
            ruleStack = new RuleStack();
            newGrammar();

            firstNode = createGrammarNode("<sil>");
            GrammarNode finalNode = createGrammarNode("<sil>");
            finalNode.setFinalNode(true);

            // go through each rule and create a network of GrammarNodes
            // for each of them

            String[] ruleNames = ruleGrammar.listRuleNames();
            for (int i = 0; i < ruleNames.length; i++) {
                String ruleName = ruleNames[i];
                if (ruleGrammar.isRulePublic(ruleName)) {
                    String fullName = getFullRuleName(ruleName);
                    GrammarGraph publicRuleGraph = new GrammarGraph();
                    ruleStack.push(fullName, publicRuleGraph);
                    Rule rule = ruleGrammar.getRule(ruleName);
                    GrammarGraph graph = parseRule(rule);
                    ruleStack.pop();

                    firstNode.add(publicRuleGraph.getStartNode(), 0.0f);
                    publicRuleGraph.getEndNode().add(finalNode, 0.0f);
                    publicRuleGraph.getStartNode().add(
                                graph.getStartNode(), 0.0f);
                    graph.getEndNode().add( publicRuleGraph.getEndNode(), 0.0f);
                }
            }
            postProcessGrammar();
//        } catch (EngineException ee) {
//            // ee.printStackTrace();
//            throw new IOException(ee.toString());
        } catch (GrammarException ge) {
            // ge.printStackTrace();
            dumpGrammarException(ge);
            throw new IOException("GrammarException: " + ge);
        } catch (MalformedURLException mue) {
            throw new IOException("bad base grammar url " + baseURL + " "
                    + mue);
        }
    }

    /**
     * Gets the fully resolved rule name
     *
     * @param ruleName the partial name
     * @return the fully resovled name
     *
     * @throws GrammarException 
     */
    private String getFullRuleName(String ruleName) throws GrammarException {
        RuleName rname = ruleGrammar.resolve(new RuleName(ruleName));
        return rname.getRuleName();
    }



    /**
     * Debugging println
     * 
     * @param message
     *                the message to optionally print
     */
    private void debugPrintln(String message) {
        if (false) {
            System.out.println(message);
        }
    }

    /**
     * Dumps interesting things about this grammar
     */
    private void dumpGrammar() {
        System.out.println("Imported rules { ");
        RuleName[] imports = ruleGrammar.listImports();

        for (int i = 0; i < imports.length; i++) {
            System.out
                    .println("  Import " + i + " " + imports[i].getRuleName());
        }
        System.out.println("}");

        System.out.println("Rulenames { ");
        String[] names = ruleGrammar.listRuleNames();

        for (int i = 0; i < names.length; i++) {
            System.out.println("  Name " + i + " " + names[i]);
        }
        System.out.println("}");
    }

    /**
     * Represents a graph of grammar nodes. A grammar graph has a single
     * starting node and a single ending node
     */
    class GrammarGraph {
        private GrammarNode startNode;
        private GrammarNode endNode;

        /**
         * Creates a grammar graph with the given nodes
         * 
         * @param startNode
         *                the staring node of the graph
         * @param endNode
         *                the ending node of the graph
         */
        GrammarGraph(GrammarNode startNode, GrammarNode endNode) {
            this.startNode = startNode;
            this.endNode = endNode;
        }

        /**
         * Creates a graph with non-word nodes for the start and ending nodes
         */
        GrammarGraph() {
            startNode = createGrammarNode(false);
            endNode = createGrammarNode(false);
        }

        /**
         * Gets the starting node
         * 
         * @return the starting node for the graph
         */
        GrammarNode getStartNode() {
            return startNode;
        }

        /**
         * Gets the ending node
         * 
         * @return the ending node for the graph
         */
        GrammarNode getEndNode() {
            return endNode;
        }
    }


    /**
     * Manages a stack of grammar graphs that can be accessed by grammar name
     */
    class RuleStack {
        private List stack;
        private HashMap map;

        /**
         * Creates a name stack
         */
        public RuleStack() {
            clear();
        }

        /**
         * Pushes the grammar graph on the stack
         */
        public void push(String name, GrammarGraph g) {
            stack.add(0, name);
            map.put(name, g);
        }

        /**
         * remove the top graph on the stack
         */
        public void pop() {
            map.remove(stack.remove(0));
        }

        /**
         * Checks to see if the stack contains a graph with the given name
         *
         * @param name  the graph name
         * @return the grammar graph associated with the name if found,
         * otherwise null
         */
        public GrammarGraph contains(String name) {
            if (stack.contains(name)) {
                return (GrammarGraph) (GrammarGraph) map.get(name);
            } else {
                return null;
            }
        }

        /**
         * Clears this name stack
         */
        public void clear() {
            stack = new LinkedList();
            map = new HashMap();
        }
    }
}
