package org.antlr.v4.runtime;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.*;
import org.antlr.v4.runtime.misc.Interval;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BufferedTokenStream implements TokenStream{

    protected TokenSource tokenSource;
    protected List<Token> tokens = new ArrayList<Token>(100);
    protected int lastMarker;
    protected int p = -1;

    public BufferedTokenStream() { }
    public BufferedTokenStream(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }
    @Override
    public TokenSource getTokenSource() { return tokenSource; }
    @Override
	public int index() { return p; }
    @Override
    public int mark() {
		return 0;
	}
    @Override
	public void release(int marker) {
		// no resources to release
	}
    public void reset() {
        p = 0;
        lastMarker = 0;
    }
    @Override
    public void seek(int index) {
        if (p == -1) {
            setup();
        }

        p = index;
    }
    @Override
    public int size() { return tokens.size(); }
    @Override
    public void consume() {
        if ( p == -1 ) setup();
        p++;
        sync(p);
    }
    protected void sync(int i) {
        int n = i - tokens.size() + 1; // how many more elements we need?
        //System.out.println("sync("+i+") needs "+n);
        if ( n > 0 ) fetch(n);
    }
    protected void fetch(int n) {
        for (int i=1; i<=n; i++) {
            Token t = tokenSource.nextToken();
            if ( t instanceof WritableToken ) {
                ((WritableToken)t).setTokenIndex(tokens.size());
            }
            tokens.add(t);
            if ( t.getType()==Token.EOF ) break;
        }
    }
    @Override
    public Token get(int i) {
        if ( i < 0 || i >= tokens.size() ) {
            throw new IndexOutOfBoundsException("token index "+i+" out of range 0.."+(tokens.size()-1));
        }
        return tokens.get(i);
    }
    public List<Token> get(int start, int stop) {
		if ( start<0 || stop<0 ) return null;
		if ( p == -1 ) setup();
		List<Token> subset = new ArrayList<Token>();
		if ( stop>=tokens.size() ) stop = tokens.size()-1;
		for (int i = start; i <= stop; i++) {
			Token t = tokens.get(i);
			if ( t.getType()==Token.EOF ) break;
			subset.add(t);
		}
		return subset;
	}
    @Override
	public int LA(int i) { return LT(i).getType(); }
    protected Token LB(int k) {
        if ( (p-k)<0 ) return null;
        return tokens.get(p-k);
    }
    @Override
    public Token LT(int k) {
        if ( p == -1 ) setup();
        if ( k==0 ) return null;
        if ( k < 0 ) return LB(-k);

		int i = p + k - 1;
		sync(i);
        if ( i >= tokens.size() ) { // return EOF token
            // EOF must be last token
            return tokens.get(tokens.size()-1);
        }
//		if ( i>range ) range = i;
        return tokens.get(i);
    }
    protected void setup() { sync(0); p = 0; }
    public void setTokenSource(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
        tokens.clear();
        p = -1;
    }
    public List<Token> getTokens() { return tokens; }
    public List<Token> getTokens(int start, int stop) {
        return getTokens(start, stop, null);
    }
    public List<Token> getTokens(int start, int stop, Set<Integer> types) {
        if ( p == -1 ) setup();
		if ( start<0 || stop>=tokens.size() ||
			 stop<0  || start>=tokens.size() )
		{
			throw new IndexOutOfBoundsException("start "+start+" or stop "+stop+
												" not in 0.."+(tokens.size()-1));
		}
        if ( start>stop ) return null;

        // list = tokens[start:stop]:{T t, t.getType() in types}
        List<Token> filteredTokens = new ArrayList<Token>();
        for (int i=start; i<=stop; i++) {
            Token t = tokens.get(i);
            if ( types==null || types.contains(t.getType()) ) {
                filteredTokens.add(t);
            }
        }
        if ( filteredTokens.isEmpty() ) {
            filteredTokens = null;
        }
        return filteredTokens;
    }
    public List<Token> getTokens(int start, int stop, int ttype) {
		HashSet<Integer> s = new HashSet<Integer>(ttype);
		s.add(ttype);
		return getTokens(start,stop, s);
    }
    protected int nextTokenOnChannel(int i, int channel) {
		sync(i);
		Token token = tokens.get(i);
		if ( i>=size() ) return -1;
		while ( token.getChannel()!=channel ) {
			if ( token.getType()==Token.EOF ) return -1;
			i++;
			sync(i);
			token = tokens.get(i);
		}
		return i;
	}
    protected int previousTokenOnChannel(int i, int channel) {
		while ( i>=0 && tokens.get(i).getChannel()!=channel ) {
			i--;
		}
		return i;
	}
    public List<Token> getHiddenTokensToRight(int tokenIndex, int channel) {
		if ( p == -1 ) setup();
		if ( tokenIndex<0 || tokenIndex>=tokens.size() ) {
			throw new IndexOutOfBoundsException(tokenIndex+" not in 0.."+(tokens.size()-1));
		}

		int nextOnChannel =
			nextTokenOnChannel(tokenIndex + 1, Lexer.DEFAULT_TOKEN_CHANNEL);
		int to;
		int from = tokenIndex+1;
		// if none onchannel to right, nextOnChannel=-1 so set to = last token
		if ( nextOnChannel == -1 ) to = size()-1;
		else to = nextOnChannel;

		return filterForChannel(from, to, channel);
	}
    public List<Token> getHiddenTokensToRight(int tokenIndex) {
		return getHiddenTokensToRight(tokenIndex, -1);
	}
    public List<Token> getHiddenTokensToLeft(int tokenIndex, int channel) {
		if ( p == -1 ) setup();
		if ( tokenIndex<0 || tokenIndex>=tokens.size() ) {
			throw new IndexOutOfBoundsException(tokenIndex+" not in 0.."+(tokens.size()-1));
		}

		int prevOnChannel =
			previousTokenOnChannel(tokenIndex - 1, Lexer.DEFAULT_TOKEN_CHANNEL);
		if ( prevOnChannel == tokenIndex - 1 ) return null;
		// if none onchannel to left, prevOnChannel=-1 then from=0
		int from = prevOnChannel+1;
		int to = tokenIndex-1;

		return filterForChannel(from, to, channel);
	}
    public List<Token> getHiddenTokensToLeft(int tokenIndex) {
		return getHiddenTokensToLeft(tokenIndex, -1);
	}
    protected List<Token> filterForChannel(int from, int to, int channel) {
		List<Token> hidden = new ArrayList<Token>();
		for (int i=from; i<=to; i++) {
			Token t = tokens.get(i);
			if ( channel==-1 ) {
				if ( t.getChannel()!= Lexer.DEFAULT_TOKEN_CHANNEL ) hidden.add(t);
			}
			else {
				if ( t.getChannel()==channel ) hidden.add(t);
			}
		}
		if ( hidden.size()==0 ) return null;
		return hidden;
	}
    @Override
    public String getSourceName() {	return tokenSource.getSourceName();	}
    @NotNull
	@Override
	public String getText() {
		if ( p == -1 ) setup();
		fill();
		return getText(Interval.of(0,size()-1));
	}
    @NotNull
    @Override
    public String getText(Interval interval) {
		int start = interval.a;
		int stop = interval.b;
        if ( start<0 || stop<0 ) return "";
        if ( p == -1 ) setup();
        if ( stop>=tokens.size() ) stop = tokens.size()-1;

		StringBuilder buf = new StringBuilder();
		for (int i = start; i <= stop; i++) {
			Token t = tokens.get(i);
			if ( t.getType()==Token.EOF ) break;
			buf.append(t.getText());
		}
		return buf.toString();
    }
    @NotNull
	@Override
	public String getText(RuleContext ctx) {
		return getText(ctx.getSourceInterval());
	}
    @NotNull
    @Override
    public String getText(Token start, Token stop) {
        if ( start!=null && stop!=null ) {
            return getText(Interval.of(start.getTokenIndex(), stop.getTokenIndex()));
        }

		return "";
    }
    public void fill() {
        if ( p == -1 ) setup();
        if ( tokens.get(p).getType()==Token.EOF ) return;

        int i = p+1;
        sync(i);
        while ( tokens.get(i).getType()!=Token.EOF ) {
            i++;
            sync(i);
        }
    }
}
