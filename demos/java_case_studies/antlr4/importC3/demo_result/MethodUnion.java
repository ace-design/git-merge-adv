package org.antlr.v4.semantics;
import org.antlr.v4.parse.ANTLRParser;
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
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.tool.Attribute;
import org.antlr.v4.tool.AttributeDict;

public class SymbolChecks {

    Grammar g;
    SymbolCollector collector;
    Map<String, Rule> nameToRuleMap = new HashMap<String, Rule>();
    Set<String> tokenIDs = new HashSet<String>();
    Map<String, Set<String>> actionScopeToActionNames = new HashMap<String, Set<String>>();
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
    public void checkForLabelConflicts(Collection<Rule> rules) {
        for (Rule r : rules) {
            checkForAttributeConflicts(r);
            Map<String, LabelElementPair> labelNameSpace =
                new HashMap<String, LabelElementPair>();
            for (int i=1; i<=r.numberOfAlts; i++) {
				if (r.hasAltSpecificContexts()) {
					labelNameSpace.clear();
				}

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
    public void checkForLabelConflict(Rule r, GrammarAST labelID) {
		String name = labelID.getText();
		if (nameToRuleMap.containsKey(name)) {
			ErrorType etype = ErrorType.LABEL_CONFLICTS_WITH_RULE;
			errMgr.grammarError(etype, g.fileName, labelID.token, name, r.name);
		}

		if (tokenIDs.contains(name)) {
			ErrorType etype = ErrorType.LABEL_CONFLICTS_WITH_TOKEN;
			errMgr.grammarError(etype, g.fileName, labelID.token, name, r.name);
		}

		if (r.args != null && r.args.get(name) != null) {
			ErrorType etype = ErrorType.LABEL_CONFLICTS_WITH_ARG;
			errMgr.grammarError(etype, g.fileName, labelID.token, name, r.name);
		}

		if (r.retvals != null && r.retvals.get(name) != null) {
			ErrorType etype = ErrorType.LABEL_CONFLICTS_WITH_RETVAL;
			errMgr.grammarError(etype, g.fileName, labelID.token, name, r.name);
		}

		if (r.locals != null && r.locals.get(name) != null) {
			ErrorType etype = ErrorType.LABEL_CONFLICTS_WITH_LOCAL;
			errMgr.grammarError(etype, g.fileName, labelID.token, name, r.name);
		}
	}
    public void checkForAttributeConflicts(Rule r) {
		checkDeclarationRuleConflicts(r, r.args, nameToRuleMap.keySet(), ErrorType.ARG_CONFLICTS_WITH_RULE);
		checkDeclarationRuleConflicts(r, r.args, tokenIDs, ErrorType.ARG_CONFLICTS_WITH_TOKEN);

		checkDeclarationRuleConflicts(r, r.retvals, nameToRuleMap.keySet(), ErrorType.RETVAL_CONFLICTS_WITH_RULE);
		checkDeclarationRuleConflicts(r, r.retvals, tokenIDs, ErrorType.RETVAL_CONFLICTS_WITH_TOKEN);

		checkDeclarationRuleConflicts(r, r.locals, nameToRuleMap.keySet(), ErrorType.LOCAL_CONFLICTS_WITH_RULE);
		checkDeclarationRuleConflicts(r, r.locals, tokenIDs, ErrorType.LOCAL_CONFLICTS_WITH_TOKEN);

		checkLocalConflictingDeclarations(r, r.retvals, r.args, ErrorType.RETVAL_CONFLICTS_WITH_ARG);
		checkLocalConflictingDeclarations(r, r.locals, r.args, ErrorType.LOCAL_CONFLICTS_WITH_ARG);
		checkLocalConflictingDeclarations(r, r.locals, r.retvals, ErrorType.LOCAL_CONFLICTS_WITH_RETVAL);
	}
    protected void checkDeclarationRuleConflicts(@NotNull Rule r, @Nullable AttributeDict attributes, @NotNull Set<String> ruleNames, @NotNull ErrorType errorType) {
		if (attributes == null) {
			return;
		}

		for (Attribute attribute : attributes.attributes.values()) {
			if (ruleNames.contains(attribute.name)) {
				errMgr.grammarError(
					errorType,
					g.fileName,
					attribute.token != null ? attribute.token : ((GrammarAST)r.ast.getChild(0)).token,
					attribute.name,
					r.name);
			}
		}
	}
    protected void checkLocalConflictingDeclarations(@NotNull Rule r, @Nullable AttributeDict attributes, @Nullable AttributeDict referenceAttributes, @NotNull ErrorType errorType) {
		if (attributes == null || referenceAttributes == null) {
			return;
		}

		Set<String> conflictingKeys = attributes.intersection(referenceAttributes);
		for (String key : conflictingKeys) {
			errMgr.grammarError(
				errorType,
				g.fileName,
				attributes.get(key).token != null ? attributes.get(key).token : ((GrammarAST) r.ast.getChild(0)).token,
				key,
				r.name);
		}
	}
    public void checkRuleArgs(Grammar g, List<GrammarAST> rulerefs) {
		if ( rulerefs==null ) return;
		for (GrammarAST ref : rulerefs) {
			String ruleName = ref.getText();
			Rule r = g.getRule(ruleName);
			GrammarAST arg = (GrammarAST)ref.getFirstChildWithType(ANTLRParser.ARG_ACTION);
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
}
