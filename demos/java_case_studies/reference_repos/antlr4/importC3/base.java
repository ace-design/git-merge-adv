/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.semantics;

import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LabelElementPair;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Check for symbol problems; no side-effects.  Inefficient to walk rules
 *  and such multiple times, but I like isolating all error checking outside
 *  of code that actually defines symbols etc...
 *
 *  Side-effect: strip away redef'd rules.
 */
public class SymbolChecks {
    Grammar g;
    SymbolCollector collector;
    Map<String, Rule> nameToRuleMap = new HashMap<String, Rule>();
	Set<String> tokenIDs = new HashSet<String>();
    Map<String, Set<String>> actionScopeToActionNames = new HashMap<String, Set<String>>();
//	DoubleKeyMap<String, String, GrammarAST> namedActions =
//		new DoubleKeyMap<String, String, GrammarAST>();

	public ErrorManager errMgr;

    public SymbolChecks(Grammar g, SymbolCollector collector) {
        this.g = g;
        this.collector = collector;
		this.errMgr = g.tool.errMgr;

        for (GrammarAST tokenId : collector.tokenIDRefs) {
            tokenIDs.add(tokenId.getText());
        }
        /*
        System.out.println("rules="+collector.rules);
        System.out.println("rulerefs="+collector.rulerefs);
        System.out.println("tokenIDRefs="+collector.tokenIDRefs);
        System.out.println("terminals="+collector.terminals);
        System.out.println("strings="+collector.strings);
        System.out.println("tokensDef="+collector.tokensDefs);
        System.out.println("actions="+collector.actions);
        System.out.println("scopes="+collector.scopes);
         */
    }

    public void process() {
        // methods affect fields, but no side-effects outside this object
		// So, call order sensitive
		// First collect all rules for later use in checkForLabelConflict()
		if ( g.rules!=null ) {
			for (Rule r : g.rules.values()) nameToRuleMap.put(r.name, r);
		}
		checkActionRedefinitions(collector.namedActions);
		checkForTokenConflicts(collector.tokenIDRefs);  // sets tokenIDs
		checkForLabelConflicts(g.rules.values());
	}

	public void checkActionRedefinitions(List<GrammarAST> actions) {
		if ( actions==null ) return;
		String scope = g.getDefaultActionScope();
		String name;
		GrammarAST nameNode;
		for (GrammarAST ampersandAST : actions) {
			nameNode = (GrammarAST)ampersandAST.getChild(0);
			if ( ampersandAST.getChildCount()==2 ) {
				name = nameNode.getText();
			}
			else {
				scope = nameNode.getText();
                name = ampersandAST.getChild(1).getText();
            }
            Set<String> scopeActions = actionScopeToActionNames.get(scope);
            if ( scopeActions==null ) { // init scope
                scopeActions = new HashSet<String>();
                actionScopeToActionNames.put(scope, scopeActions);
            }
            if ( !scopeActions.contains(name) ) {
                scopeActions.add(name);
            }
            else {
                errMgr.grammarError(ErrorType.ACTION_REDEFINITION,
                                          g.fileName, nameNode.token, name);
            }
        }
    }

    public void checkForTokenConflicts(List<GrammarAST> tokenIDRefs) {
//        for (GrammarAST a : tokenIDRefs) {
//            Token t = a.token;
//            String ID = t.getText();
//            tokenIDs.add(ID);
//        }
    }

    /** Make sure a label doesn't conflict with another symbol.
     *  Labels must not conflict with: rules, tokens, scope names,
     *  return values, parameters, and rule-scope dynamic attributes
     *  defined in surrounding rule.  Also they must have same type
     *  for repeated defs.
     */
    public void checkForLabelConflicts(Collection<Rule> rules) {
        for (Rule r : rules) {
            checkForRuleArgumentAndReturnValueConflicts(r);
            Map<String, LabelElementPair> labelNameSpace =
                new HashMap<String, LabelElementPair>();
            for (int i=1; i<=r.numberOfAlts; i++) {
                Alternative a = r.alt[i];
                for (List<LabelElementPair> pairs : a.labelDefs.values() ) {
                    for (LabelElementPair p : pairs) {
                        checkForLabelConflict(r, p.label);
                        String name = p.label.getText();
                        LabelElementPair prev = labelNameSpace.get(name);
                        if ( prev==null ) labelNameSpace.put(name, p);
                        else checkForTypeMismatch(prev, p);
                    }
                }
            }
        }
    }

    void checkForTypeMismatch(LabelElementPair prevLabelPair,
                                        LabelElementPair labelPair)
    {
        // label already defined; if same type, no problem
        if ( prevLabelPair.type != labelPair.type ) {
            String typeMismatchExpr = labelPair.type+"!="+prevLabelPair.type;
            errMgr.grammarError(
                ErrorType.LABEL_TYPE_CONFLICT,
                g.fileName,
                labelPair.label.token,
                labelPair.label.getText(),
                typeMismatchExpr);
        }
    }

    public void checkForLabelConflict(Rule r, GrammarAST labelID) {
        ErrorType etype = ErrorType.INVALID;
        Object arg2 = null;
        String name = labelID.getText();
        if ( nameToRuleMap.containsKey(name) ) {
            etype = ErrorType.LABEL_CONFLICTS_WITH_RULE;
        }
        else if ( tokenIDs.contains(name) ) {
            etype = ErrorType.LABEL_CONFLICTS_WITH_TOKEN;
        }
        else if ( (r.retvals!=null&&r.retvals.get(name)!=null) ||
                  (r.args!=null&&r.args.get(name)!=null) )
        {
            etype = ErrorType.LABEL_CONFLICTS_WITH_RULE_ARG_RETVAL;
            arg2 = r.name;
        }
        if ( etype!=ErrorType.INVALID ) {
            errMgr.grammarError(etype,g.fileName,labelID.token,name,arg2);
        }
    }

    public void checkForRuleArgumentAndReturnValueConflicts(Rule r) {
        if ( r.retvals!=null ) {
            Set<String> conflictingKeys = r.retvals.intersection(r.args);
            if (conflictingKeys!=null) {
				for (String key : conflictingKeys) {
					errMgr.grammarError(
						ErrorType.ARG_RETVAL_CONFLICT,
						g.fileName,
						((GrammarAST) r.ast.getChild(0)).token,
						key,
						r.name);
				}
            }
        }
    }

	// CAN ONLY CALL THE TWO NEXT METHODS AFTER GRAMMAR HAS RULE DEFS (see semanticpipeline)

	public void checkRuleArgs(Grammar g, List<GrammarAST> rulerefs) {
		if ( rulerefs==null ) return;
		for (GrammarAST ref : rulerefs) {
			String ruleName = ref.getText();
			Rule r = g.getRule(ruleName);
			GrammarAST arg = (GrammarAST)ref.getChild(0);
			if ( arg!=null && (r==null || r.args==null) ) {
				errMgr.grammarError(ErrorType.RULE_HAS_NO_ARGS,
										  g.fileName, ref.token, ruleName);

			}
			else if ( arg==null && (r!=null&&r.args!=null) ) {
				errMgr.grammarError(ErrorType.MISSING_RULE_ARGS,
										  g.fileName, ref.token, ruleName);
			}
		}
	}

	public void checkForQualifiedRuleIssues(Grammar g, List<GrammarAST> qualifiedRuleRefs) {
		for (GrammarAST dot : qualifiedRuleRefs) {
			GrammarAST grammar = (GrammarAST)dot.getChild(0);
			GrammarAST rule = (GrammarAST)dot.getChild(1);
            g.tool.log("semantics", grammar.getText()+"."+rule.getText());
			Grammar delegate = g.getImportedGrammar(grammar.getText());
			if ( delegate==null ) {
				errMgr.grammarError(ErrorType.NO_SUCH_GRAMMAR_SCOPE,
										  g.fileName, grammar.token, grammar.getText(),
										  rule.getText());
			}
			else {
				if ( g.getRule(grammar.getText(), rule.getText())==null ) {
					errMgr.grammarError(ErrorType.NO_SUCH_RULE_IN_SCOPE,
											  g.fileName, rule.token, grammar.getText(),
											  rule.getText());
				}
			}
		}
	}
}
