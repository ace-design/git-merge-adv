package org.antlr.v4.automata;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.misc.CharSupport;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.ATNBuilder;
import org.antlr.v4.parse.GrammarASTAdaptor;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.AbstractPredicateTransition;
import org.antlr.v4.runtime.atn.ActionTransition;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.BlockEndState;
import org.antlr.v4.runtime.atn.BlockStartState;
import org.antlr.v4.runtime.atn.EpsilonTransition;
import org.antlr.v4.runtime.atn.LoopEndState;
import org.antlr.v4.runtime.atn.NotSetTransition;
import org.antlr.v4.runtime.atn.PlusBlockStartState;
import org.antlr.v4.runtime.atn.PlusLoopbackState;
import org.antlr.v4.runtime.atn.PrecedencePredicateTransition;
import org.antlr.v4.runtime.atn.PredicateTransition;
import org.antlr.v4.runtime.atn.RuleStartState;
import org.antlr.v4.runtime.atn.RuleStopState;
import org.antlr.v4.runtime.atn.RuleTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.StarBlockStartState;
import org.antlr.v4.runtime.atn.StarLoopEntryState;
import org.antlr.v4.runtime.atn.StarLoopbackState;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.atn.WildcardTransition;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.semantics.UseDefAnalyzer;
import org.antlr.v4.tool.ErrorManager;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LeftRecursiveRule;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.BlockAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTWithOptions;
import org.antlr.v4.tool.ast.PredAST;
import org.antlr.v4.tool.ast.QuantifierAST;
import org.antlr.v4.tool.ast.TerminalAST;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import org.antlr.v4.runtime.atn.ATNType;
import org.antlr.v4.runtime.atn.BasicBlockStartState;
import org.antlr.v4.runtime.atn.BasicState;
import org.antlr.v4.runtime.atn.LL1Analyzer;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.tool.LexerGrammar;
import java.util.ArrayList;

public class ParserATNFactory implements ATNFactory{

    @NotNull
	public final Grammar g;
    @NotNull
	public final ATN atn;
    public Rule currentRule;
    public int currentOuterAlt;
    protected final List<Triple<Rule, ATNState, ATNState>> preventEpsilonClosureBlocks =
		new ArrayList<Triple<Rule, ATNState, ATNState>>();
    protected final List<Triple<Rule, ATNState, ATNState>> preventEpsilonOptionalBlocks =
		new ArrayList<Triple<Rule, ATNState, ATNState>>();

    @Override
	public void setCurrentRuleName(String name) {
		this.currentRule = g.getRule(name);
	}
    @Override
	public void setCurrentOuterAlt(int alt) {
		currentOuterAlt = alt;
	}
    @Override
	public Handle rule(GrammarAST ruleAST, String name, Handle blk) {
		Rule r = g.getRule(name);
		RuleStartState start = atn.ruleToStartState[r.index];
		epsilon(start, blk.left);
		RuleStopState stop = atn.ruleToStopState[r.index];
		epsilon(blk.right, stop);
		Handle h = new Handle(start, stop);
//		ATNPrinter ser = new ATNPrinter(g, h.left);
//		System.out.println(ruleAST.toStringTree()+":\n"+ser.asString());
		ruleAST.atnState = start;
		return h;
	}
    @Override
	public Handle tokenRef(TerminalAST node) {
		ATNState left = newState(node);
		ATNState right = newState(node);
		int ttype = g.getTokenType(node.getText());
		left.addTransition(new AtomTransition(right, ttype));
		node.atnState = left;
		return new Handle(left, right);
	}
    @Override
	public Handle set(GrammarAST associatedAST, List<GrammarAST> terminals, boolean invert) {
		ATNState left = newState(associatedAST);
		ATNState right = newState(associatedAST);
		IntervalSet set = new IntervalSet();
		for (GrammarAST t : terminals) {
			int ttype = g.getTokenType(t.getText());
			set.add(ttype);
		}
		if ( invert ) {
			left.addTransition(new NotSetTransition(right, set));
		}
		else {
			left.addTransition(new SetTransition(right, set));
		}
		associatedAST.atnState = left;
		return new Handle(left, right);
	}
    @Override
	public Handle range(GrammarAST a, GrammarAST b) {
		throw new UnsupportedOperationException();
	}
    protected int getTokenType(GrammarAST atom) {
		int ttype;
		if ( g.isLexer() ) {
			ttype = CharSupport.getCharValueFromGrammarCharLiteral(atom.getText());
		}
		else {
			ttype = g.getTokenType(atom.getText());
		}
		return ttype;
	}
    @Override
	public Handle stringLiteral(TerminalAST stringLiteralAST) {
		return tokenRef(stringLiteralAST);
	}
    @Override
	public Handle charSetLiteral(GrammarAST charSetAST) {
		return null;
	}
    @Override
	public Handle ruleRef(GrammarAST node) {
		Handle h = _ruleRef(node);
		return h;
	}
    public void addFollowLink(int ruleIndex, ATNState right) {
		// add follow edge from end of invoked rule
		RuleStopState stop = atn.ruleToStopState[ruleIndex];
//        System.out.println("add follow link from "+ruleIndex+" to "+right);
		epsilon(stop, right);
	}
    @Override
	public Handle epsilon(GrammarAST node) {
		ATNState left = newState(node);
		ATNState right = newState(node);
		epsilon(left, right);
		node.atnState = left;
		return new Handle(left, right);
	}
    @Override
	public Handle action(ActionAST action) {
		//System.out.println("action: "+action);
		ATNState left = newState(action);
		ATNState right = newState(action);
		ActionTransition a = new ActionTransition(right, currentRule.index);
		left.addTransition(a);
		action.atnState = left;
		return new Handle(left, right);
	}
    @Override
	public Handle action(String action) {
		return null;
	}
    protected Handle makeBlock(BlockStartState start, GrammarAST blkAST, List<Handle> alts) {
		BlockEndState end = newState(BlockEndState.class, blkAST);
		start.endState = end;
		for (Handle alt : alts) {
			// hook alts up to decision block
			epsilon(start, alt.left);
			epsilon(alt.right, end);
			// no back link in ATN so must walk entire alt to see if we can
			// strip out the epsilon to 'end' state
			TailEpsilonRemover opt = new TailEpsilonRemover(atn);
			opt.visit(alt.left);
		}
		Handle h = new Handle(start, end);
//		FASerializer ser = new FASerializer(g, h.left);
//		System.out.println(blkAST.toStringTree()+":\n"+ser);
		blkAST.atnState = start;
		return h;
	}
    @NotNull
	@Override
	public Handle alt(@NotNull List<Handle> els) {
		return elemList(els);
	}
    @NotNull
	@Override
	public Handle wildcard(GrammarAST node) {
		ATNState left = newState(node);
		ATNState right = newState(node);
		left.addTransition(new WildcardTransition(right));
		node.atnState = left;
		return new Handle(left, right);
	}
    public int addEOFTransitionToStartRules() {
		int n = 0;
		ATNState eofTarget = newState(null); // one unique EOF target for all rules
		for (Rule r : g.rules.values()) {
			ATNState stop = atn.ruleToStopState[r.index];
			if ( stop.getNumberOfTransitions()>0 ) continue;
			n++;
			Transition t = new AtomTransition(eofTarget, Token.EOF);
			stop.addTransition(t);
		}
		return n;
	}
    @Override
	public Handle label(Handle t) {
		return t;
	}
    @Override
	public Handle listLabel(Handle t) {
		return t;
	}
    @NotNull
	@Override
	public ATNState newState() { return newState(null); }
    public boolean expectNonGreedy(@NotNull BlockAST blkAST) {
		if ( blockHasWildcardAlt(blkAST) ) {
			return true;
		}

		return false;
	}
    public static boolean blockHasWildcardAlt(@NotNull GrammarAST block) {
		for (Object alt : block.getChildren()) {
			if ( !(alt instanceof AltAST) ) continue;
			AltAST altAST = (AltAST)alt;
			if ( altAST.getChildCount()==1 ) {
				Tree e = altAST.getChild(0);
				if ( e.getType()==ANTLRParser.WILDCARD ) {
					return true;
				}
			}
		}
		return false;
	}
    @Override
	public Handle lexerAltCommands(Handle alt, Handle cmds) {
		return null;
	}
    @Override
	public String lexerCallCommand(GrammarAST ID, GrammarAST arg) {
		return null;
	}
    @Override
	public String lexerCommand(GrammarAST ID) {
		return null;
	}
    public ParserATNFactory(@NotNull Grammar g) {
		if (g == null) {
			throw new NullPointerException("g");
		}

		this.g = g;

		ATNType atnType = g instanceof LexerGrammar ? ATNType.LEXER : ATNType.PARSER;
		int maxTokenType = g.getMaxTokenType();
		this.atn = new ATN(atnType, maxTokenType);
	}
    @Override
	public ATN createATN() {
		_createATN(g.rules.values());
		assert atn.maxTokenType == g.getMaxTokenType();
        addRuleFollowLinks();
		addEOFTransitionToStartRules();
		ATNOptimizer.optimize(g, atn);

		for (Triple<Rule, ATNState, ATNState> pair : preventEpsilonClosureBlocks) {
			LL1Analyzer analyzer = new LL1Analyzer(atn);
			if (analyzer.LOOK(pair.b, pair.c, null).contains(org.antlr.v4.runtime.Token.EPSILON)) {
				ErrorType errorType = pair.a instanceof LeftRecursiveRule ? ErrorType.EPSILON_LR_FOLLOW : ErrorType.EPSILON_CLOSURE;
				g.tool.errMgr.grammarError(errorType, g.fileName, ((GrammarAST)pair.a.ast.getChild(0)).getToken(), pair.a.name);
			}
		}

		optionalCheck:
		for (Triple<Rule, ATNState, ATNState> pair : preventEpsilonOptionalBlocks) {
			int bypassCount = 0;
			for (int i = 0; i < pair.b.getNumberOfTransitions(); i++) {
				ATNState startState = pair.b.transition(i).target;
				if (startState == pair.c) {
					bypassCount++;
					continue;
				}

				LL1Analyzer analyzer = new LL1Analyzer(atn);
				if (analyzer.LOOK(startState, pair.c, null).contains(org.antlr.v4.runtime.Token.EPSILON)) {
					g.tool.errMgr.grammarError(ErrorType.EPSILON_OPTIONAL, g.fileName, ((GrammarAST)pair.a.ast.getChild(0)).getToken(), pair.a.name);
					continue optionalCheck;
				}
			}

			if (bypassCount != 1) {
				throw new UnsupportedOperationException("Expected optional block with exactly 1 bypass alternative.");
			}
		}

		return atn;
	}
    protected void _createATN(Collection<Rule> rules) {
		createRuleStartAndStopATNStates();

		GrammarASTAdaptor adaptor = new GrammarASTAdaptor();
		for (Rule r : rules) {
			// find rule's block
			GrammarAST blk = (GrammarAST)r.ast.getFirstChildWithType(ANTLRParser.BLOCK);
			CommonTreeNodeStream nodes = new CommonTreeNodeStream(adaptor,blk);
			ATNBuilder b = new ATNBuilder(nodes,this);
			try {
				setCurrentRuleName(r.name);
				Handle h = b.ruleBlock(null);
				rule(r.ast, r.name, h);
			}
			catch (RecognitionException re) {
				ErrorManager.fatalInternalError("bad grammar AST structure", re);
			}
		}
	}
    @Override
	public Handle block(BlockAST blkAST, GrammarAST ebnfRoot, List<Handle> alts) {
		if ( ebnfRoot==null ) {
			if ( alts.size()==1 ) {
				Handle h = alts.get(0);
				blkAST.atnState = h.left;
				return h;
			}
			BlockStartState start = newState(BasicBlockStartState.class, blkAST);
			if ( alts.size()>1 ) atn.defineDecisionState(start);
			return makeBlock(start, blkAST, alts);
		}
		switch ( ebnfRoot.getType() ) {
			case ANTLRParser.OPTIONAL :
				BlockStartState start = newState(BasicBlockStartState.class, blkAST);
				atn.defineDecisionState(start);
				Handle h = makeBlock(start, blkAST, alts);
				return optional(ebnfRoot, h);
			case ANTLRParser.CLOSURE :
				BlockStartState star = newState(StarBlockStartState.class, ebnfRoot);
				if ( alts.size()>1 ) atn.defineDecisionState(star);
				h = makeBlock(star, blkAST, alts);
				return star(ebnfRoot, h);
			case ANTLRParser.POSITIVE_CLOSURE :
				PlusBlockStartState plus = newState(PlusBlockStartState.class, ebnfRoot);
				if ( alts.size()>1 ) atn.defineDecisionState(plus);
				h = makeBlock(plus, blkAST, alts);
				return plus(ebnfRoot, h);
		}
		return null;
	}
    protected Handle makeBlock(BlockStartState start, BlockAST blkAST, List<Handle> alts) {
		BlockEndState end = newState(BlockEndState.class, blkAST);
		start.endState = end;
		for (Handle alt : alts) {
			// hook alts up to decision block
			epsilon(start, alt.left);
			epsilon(alt.right, end);
			// no back link in ATN so must walk entire alt to see if we can
			// strip out the epsilon to 'end' state
			TailEpsilonRemover opt = new TailEpsilonRemover(atn);
			opt.visit(alt.left);
		}
		Handle h = new Handle(start, end);
//		FASerializer ser = new FASerializer(g, h.left);
//		System.out.println(blkAST.toStringTree()+":\n"+ser);
		blkAST.atnState = start;

		return h;
	}
    @NotNull
	public Handle elemList(@NotNull List<Handle> els) {
		int n = els.size();
		for (int i = 0; i < n - 1; i++) {	// hook up elements (visit all but last)
			Handle el = els.get(i);
			// if el is of form o-x->o for x in {rule, action, pred, token, ...}
			// and not last in alt
            Transition tr = null;
            if ( el.left.getNumberOfTransitions()==1 ) tr = el.left.transition(0);
            boolean isRuleTrans = tr instanceof RuleTransition;
            if ( el.left.getStateType() == ATNState.BASIC &&
				el.right.getStateType()== ATNState.BASIC &&
				tr!=null && (isRuleTrans && ((RuleTransition)tr).followState == el.right || tr.target == el.right) )
			{
				// we can avoid epsilon edge to next el
				if ( isRuleTrans ) ((RuleTransition)tr).followState = els.get(i+1).left;
                else tr.target = els.get(i+1).left;
				atn.removeState(el.right); // we skipped over this state
			}
			else { // need epsilon if previous block's right end node is complicated
				epsilon(el.right, els.get(i+1).left);
			}
		}
		Handle first = els.get(0);
		Handle last = els.get(n -1);
		if ( first==null || last==null ) {
			g.tool.errMgr.toolError(ErrorType.INTERNAL_ERROR, "element list has first|last == null");
		}
		return new Handle(first.left, last.right);
	}
    @NotNull
	@Override
	public Handle optional(@NotNull GrammarAST optAST, @NotNull Handle blk) {
		BlockStartState blkStart = (BlockStartState)blk.left;
		ATNState blkEnd = blk.right;
		preventEpsilonOptionalBlocks.add(new Triple<Rule, ATNState, ATNState>(currentRule, blkStart, blkEnd));

		boolean greedy = ((QuantifierAST)optAST).isGreedy();
		blkStart.nonGreedy = !greedy;
		epsilon(blkStart, blk.right, !greedy);

		optAST.atnState = blk.left;
		return blk;
	}
    @NotNull
	@Override
	public Handle plus(@NotNull GrammarAST plusAST, @NotNull Handle blk) {
		PlusBlockStartState blkStart = (PlusBlockStartState)blk.left;
		BlockEndState blkEnd = (BlockEndState)blk.right;
		preventEpsilonClosureBlocks.add(new Triple<Rule, ATNState, ATNState>(currentRule, blkStart, blkEnd));

		PlusLoopbackState loop = newState(PlusLoopbackState.class, plusAST);
		loop.nonGreedy = !((QuantifierAST)plusAST).isGreedy();
		atn.defineDecisionState(loop);
		LoopEndState end = newState(LoopEndState.class, plusAST);
		blkStart.loopBackState = loop;
		end.loopBackState = loop;

		plusAST.atnState = blkStart;
		epsilon(blkEnd, loop);		// blk can see loop back

		BlockAST blkAST = (BlockAST)plusAST.getChild(0);
		if ( ((QuantifierAST)plusAST).isGreedy() ) {
			if (expectNonGreedy(blkAST)) {
				g.tool.errMgr.grammarError(ErrorType.EXPECTED_NON_GREEDY_WILDCARD_BLOCK, g.fileName, plusAST.getToken(), plusAST.getToken().getText());
			}

			epsilon(loop, blkStart);	// loop back to start
			epsilon(loop, end);			// or exit
		}
		else {
			// if not greedy, priority to exit branch; make it first
			epsilon(loop, end);			// exit
			epsilon(loop, blkStart);	// loop back to start
		}

		return new Handle(blkStart, end);
	}
    @NotNull
	@Override
	public Handle star(@NotNull GrammarAST starAST, @NotNull Handle elem) {
		StarBlockStartState blkStart = (StarBlockStartState)elem.left;
		BlockEndState blkEnd = (BlockEndState)elem.right;
		preventEpsilonClosureBlocks.add(new Triple<Rule, ATNState, ATNState>(currentRule, blkStart, blkEnd));

		StarLoopEntryState entry = newState(StarLoopEntryState.class, starAST);
		entry.nonGreedy = !((QuantifierAST)starAST).isGreedy();
		atn.defineDecisionState(entry);
		LoopEndState end = newState(LoopEndState.class, starAST);
		StarLoopbackState loop = newState(StarLoopbackState.class, starAST);
		entry.loopBackState = loop;
		end.loopBackState = loop;

		BlockAST blkAST = (BlockAST)starAST.getChild(0);
		if ( ((QuantifierAST)starAST).isGreedy() ) {
			if (expectNonGreedy(blkAST)) {
				g.tool.errMgr.grammarError(ErrorType.EXPECTED_NON_GREEDY_WILDCARD_BLOCK, g.fileName, starAST.getToken(), starAST.getToken().getText());
			}

			epsilon(entry, blkStart);	// loop enter edge (alt 1)
			epsilon(entry, end);		// bypass loop edge (alt 2)
		}
		else {
			// if not greedy, priority to exit branch; make it first
			epsilon(entry, end);		// bypass loop edge (alt 1)
			epsilon(entry, blkStart);	// loop enter edge (alt 2)
		}
		epsilon(blkEnd, loop);		// block end hits loop back
		epsilon(loop, entry);		// loop back to entry/exit decision

		starAST.atnState = entry;	// decision is to enter/exit; blk is its own decision
		return new Handle(entry, end);
	}
    protected void epsilon(ATNState a, @NotNull ATNState b) {
		epsilon(a, b, false);
	}
    protected void epsilon(ATNState a, @NotNull ATNState b, boolean prepend) {
		if ( a!=null ) {
			int index = prepend ? 0 : a.getNumberOfTransitions();
			a.addTransition(index, new EpsilonTransition(b));
		}
	}
    public void addRuleFollowLinks() {
        for (ATNState p : atn.states) {
            if ( p!=null &&
                 p.getStateType() == ATNState.BASIC && p.getNumberOfTransitions()==1 &&
                 p.transition(0) instanceof RuleTransition )
            {
                RuleTransition rt = (RuleTransition) p.transition(0);
                addFollowLink(rt.ruleIndex, rt.followState);
            }
        }
    }
    @NotNull
	public <T extends ATNState> T newState(@NotNull Class<T> nodeType, GrammarAST node) {
		Exception cause;
		try {
			Constructor<T> ctor = nodeType.getConstructor();
			T s = ctor.newInstance();
			if ( currentRule==null ) s.setRuleIndex(-1);
			else s.setRuleIndex(currentRule.index);
			atn.addState(s);
			return s;
		} catch (InstantiationException ex) {
			cause = ex;
		} catch (IllegalAccessException ex) {
			cause = ex;
		} catch (IllegalArgumentException ex) {
			cause = ex;
		} catch (InvocationTargetException ex) {
			cause = ex;
		} catch (NoSuchMethodException ex) {
			cause = ex;
		} catch (SecurityException ex) {
			cause = ex;
		}

		String message = String.format("Could not create %s of type %s.", ATNState.class.getName(), nodeType.getName());
		throw new UnsupportedOperationException(message, cause);
	}
    @NotNull
	public ATNState newState(@Nullable GrammarAST node) {
		ATNState n = new BasicState();
		n.setRuleIndex(currentRule.index);
		atn.addState(n);
		return n;
	}
    public Handle _ruleRef(GrammarAST node) {
		Rule r = g.getRule(node.getText());
		if ( r==null ) {
			g.tool.errMgr.toolError(ErrorType.INTERNAL_ERROR, "Rule "+node.getText()+" undefined");
			return null;
		}
		RuleStartState start = atn.ruleToStartState[r.index];
		ATNState left = newState(node);
		ATNState right = newState(node);
		int precedence = 0;
		if (((GrammarASTWithOptions)node).getOptionString(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME) != null) {
			precedence = Integer.parseInt(((GrammarASTWithOptions)node).getOptionString(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME));
		}
		RuleTransition call = new RuleTransition(start, r.index, precedence, right);
		left.addTransition(call);

		node.atnState = left;
		return new Handle(left, right);
	}
    @Override
	public Handle sempred(PredAST pred) {
		//System.out.println("sempred: "+ pred);
		ATNState left = newState(pred);
		ATNState right = newState(pred);

		AbstractPredicateTransition p;
		if (pred.getOptionString(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME) != null) {
			int precedence = Integer.parseInt(pred.getOptionString(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME));
			p = new PrecedencePredicateTransition(right, precedence);
		}
		else {
			boolean isCtxDependent = UseDefAnalyzer.actionIsContextDependent(pred);
			p = new PredicateTransition(right, currentRule.index, g.sempreds.get(pred), isCtxDependent);
		}

		left.addTransition(p);
		pred.atnState = left;
		return new Handle(left, right);
	}
    void createRuleStartAndStopATNStates() {
		atn.ruleToStartState = new RuleStartState[g.rules.size()];
		atn.ruleToStopState = new RuleStopState[g.rules.size()];
		for (Rule r : g.rules.values()) {
			RuleStartState start = newState(RuleStartState.class, r.ast);
			RuleStopState stop = newState(RuleStopState.class, r.ast);
			start.stopState = stop;
			start.isPrecedenceRule = r instanceof LeftRecursiveRule;
			start.setRuleIndex(r.index);
			stop.setRuleIndex(r.index);
			atn.ruleToStartState[r.index] = start;
			atn.ruleToStopState[r.index] = stop;
		}
	}
}
