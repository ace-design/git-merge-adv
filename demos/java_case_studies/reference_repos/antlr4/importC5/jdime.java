package org.antlr.v4.semantics;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;
import java.util.List;

/** Do as much semantic checking as we can and fill in grammar
 *  with rules, actions, and token definitions.
 *  The only side effects are in the grammar passed to process().
 *  We consume a bunch of memory here while we build up data structures
 *  to perform checking, but all of it goes away after this pipeline object
 *  gets garbage collected.
 *
 *  After this pipeline finishes, we can be sure that the grammar
 *  is syntactically correct and that it's semantically correct enough for us
 *  to attempt grammar analysis. We have assigned all token types.
 *  Note that imported grammars bring in token and rule definitions
 *  but only the root grammar and any implicitly created lexer grammar
 *  get their token definitions filled up. We are treating the
 *  imported grammars like includes.
 *
 *  The semantic pipeline works on root grammars (those that do the importing,
 *  if any). Upon entry to the semantic pipeline, all imported grammars
 *  should have been loaded into delegate grammar objects with their
 *  ASTs created.  The pipeline does the BasicSemanticChecks on the
 *  imported grammar before collecting symbols. We cannot perform the
 *  simple checks such as undefined rule until we have collected all
 *  tokens and rules from the imported grammars into a single collection.
 */
public class SemanticPipeline {
  public Grammar g;

  public SemanticPipeline(Grammar g) {
    this.g = g;
  }

  public void process() {
    if (g.ast == null) {
      return;
    }
    RuleCollector ruleCollector = new RuleCollector(g);
    ruleCollector.process(g.ast);
    BasicSemanticChecks basics = new BasicSemanticChecks(g, ruleCollector);
    basics.process();
    LeftRecursiveRuleTransformer lrtrans = new LeftRecursiveRuleTransformer(g.ast, ruleCollector.rules.values(), g.tool);
    lrtrans.translateLeftRecursiveRules();
    for (Rule r : ruleCollector.rules.values()) {
      g.defineRule(r);
    }
    SymbolCollector collector = new SymbolCollector(g);
    collector.process(g.ast);
    SymbolChecks symcheck = new SymbolChecks(g, collector);
    symcheck.process();
    for (GrammarAST a : collector.namedActions) {
      g.defineAction(a);
    }
    for (Rule r : g.rules.values()) {
      for (int i = 1; i <= r.numberOfAlts; i++) {
        r.alt[i].ast.alt = r.alt[i];
      }
    }
    g.importTokensFromTokensFile();
    if (g.isLexer()) {
      assignLexerTokenTypes(g, collector.tokensDefs);
    } else {
      assignTokenTypes(g, collector.tokensDefs, collector.tokenIDRefs, collector.terminals);
    }
    symcheck.checkRuleArgs(g, collector.rulerefs);
    identifyStartRules(collector);
    symcheck.checkForQualifiedRuleIssues(g, collector.qualifiedRulerefs);
    if (g.tool.getNumErrors() > 0) {
      return;
    }
    AttributeChecks.checkAllAttributeExpressions(g);
    UseDefAnalyzer.trackTokenRuleRefsInActions(g);
  }

  void identifyStartRules(SymbolCollector collector) {
    for (GrammarAST ref : collector.rulerefs) {
      String ruleName = ref.getText();
      Rule r = g.getRule(ruleName);
      if (r != null) {
        r.isStartRule = false;
      }
    }
  }

  void assignLexerTokenTypes(Grammar g, List<GrammarAST> tokensDefs) {
    Grammar G = g.getOutermostGrammar();
    for (GrammarAST def : tokensDefs) {
      if (def.getType() == ANTLRParser.ID) {
        G.defineTokenName(def.getText());
      }
    }
    for (Rule r : g.rules.values()) {
      if (!r.isFragment() && !hasTypeOrMoreCommand(r)) {
        G.defineTokenName(r.name);
      }
    }
    List<Pair<GrammarAST, GrammarAST>> litAliases = Grammar.getStringLiteralAliasesFromLexerRules(g.ast);
    if (litAliases != null) {
      for (Pair<GrammarAST, GrammarAST> pair : litAliases) {
        GrammarAST nameAST = pair.a;
        GrammarAST litAST = pair.b;
        if (!G.stringLiteralToTypeMap.containsKey(litAST.getText())) {
          G.defineTokenAlias(nameAST.getText(), litAST.getText());
        } else {
          g.tool.errMgr.grammarError(ErrorType.ALIAS_REASSIGNMENT, g.fileName, nameAST.token, litAST.getText(), nameAST.getText());
        }
      }
    }
  }

  boolean hasTypeOrMoreCommand(@NotNull Rule r) {
    GrammarAST ast = r.ast;
    if (ast == null) {
      return false;
    }
    GrammarAST altActionAst = (GrammarAST) ast.getFirstDescendantWithType(ANTLRParser.LEXER_ALT_ACTION);
    if (altActionAst == null) {
      return false;
    }
    for (int i = 1; i < altActionAst.getChildCount(); i++) {
      GrammarAST node = (GrammarAST) altActionAst.getChild(i);
      if (node.getType() == ANTLRParser.LEXER_ACTION_CALL) {
        if ("type".equals(node.getChild(0).getText())) {
          return true;
        }
      } else {
        if ("more".equals(node.getText())) {
          return true;
        }
      }
    }
    return false;
  }

  void assignTokenTypes(Grammar g, List<GrammarAST> tokensDefs, List<GrammarAST> tokenIDs, List<GrammarAST> terminals) {
    for (GrammarAST alias : tokensDefs) {
      if (alias.getType() == ANTLRParser.ASSIGN) {
        String name = alias.getChild(0).getText();
        String lit = alias.getChild(1).getText();
        g.defineTokenAlias(name, lit);
      } else {
        g.defineTokenName(alias.getText());
      }
    }
    for (GrammarAST idAST : tokenIDs) {
      if (g.getTokenType(idAST.getText()) == Token.INVALID_TYPE) {
        g.tool.errMgr.grammarError(ErrorType.IMPLICIT_TOKEN_DEFINITION, g.fileName, idAST.token, idAST.getText());
      }
      g.defineTokenName(idAST.getText());
    }
    for (GrammarAST termAST : terminals) {
      if (termAST.getType() != ANTLRParser.STRING_LITERAL) {
        continue;
      }
      if (g.getTokenType(termAST.getText()) == Token.INVALID_TYPE) {
        g.tool.errMgr.grammarError(ErrorType.IMPLICIT_STRING_DEFINITION, g.fileName, termAST.token, termAST.getText());
      }
    }
    g.tool.log("semantics", "tokens=" + g.tokenNameToTypeMap);
    g.tool.log("semantics", "strings=" + g.stringLiteralToTypeMap);
  }
}