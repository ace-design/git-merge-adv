package org.antlr.v4.test;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.*;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.*;
import java.lang.reflect.*;
import java.util.logging.*;
import org.junit.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.atn.ATNConfig;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.dfa.DFAState;

public class TestPerformance extends BaseTest{

    private static final String TOP_PACKAGE = "java.lang";
    private static final boolean RECURSIVE = true;
    private static final boolean USE_LR_GRAMMAR = true;
    private static final boolean FORCE_ATN = false;
    private static final boolean EXPORT_ATN_GRAPHS = true;
    private static final boolean DELETE_TEMP_FILES = true;
    private static final boolean PAUSE_FOR_HEAP_DUMP = false;
    private static final boolean RUN_PARSER = true;
    private static final boolean BAIL_ON_ERROR = true;
    private static final boolean BUILD_PARSE_TREES = false;
    private static final boolean BLANK_LISTENER = false;
    private static final boolean SHOW_DFA_STATE_STATS = true;
    private static final boolean SHOW_CONFIG_STATS = false;
    private static final boolean REUSE_LEXER = true;
    private static final boolean REUSE_PARSER = true;
    private static final boolean CLEAR_DFA = false;
    private static final int PASSES = 4;
    private static Lexer sharedLexer;
    private static Parser sharedParser;
    @SuppressWarnings({"FieldCanBeLocal"})
    private static ParseTreeListener<Token> sharedListener;
    private int tokenCount;
    private int currentPass;
    int configOutputSize = 0;

    @Test
    //@Ignore
    public void compileJdk() throws IOException {
        String jdkSourceRoot = System.getenv("JDK_SOURCE_ROOT");
        if (jdkSourceRoot == null) {
            jdkSourceRoot = System.getProperty("JDK_SOURCE_ROOT");
        }
        if (jdkSourceRoot == null) {
            System.err.println("The JDK_SOURCE_ROOT environment variable must be set for performance testing.");
            return;
        }

        compileParser(USE_LR_GRAMMAR);
        JavaParserFactory factory = getParserFactory();

		if (!TOP_PACKAGE.isEmpty()) {
            jdkSourceRoot = jdkSourceRoot + '/' + TOP_PACKAGE.replace('.', '/');
        }

        File directory = new File(jdkSourceRoot);
        assertTrue(directory.isDirectory());

        Collection<CharStream> sources = loadSources(directory, RECURSIVE);

		System.out.print(getOptionsDescription());

        currentPass = 0;
        parse1(factory, sources);
        for (int i = 0; i < PASSES - 1; i++) {
            currentPass = i + 1;
            if (CLEAR_DFA) {
                sharedLexer = null;
                sharedParser = null;
            }

            parse2(factory, sources);
        }

		sources.clear();
		if (PAUSE_FOR_HEAP_DUMP) {
			System.gc();
			System.out.println("Pausing before application exit.");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException ex) {
				Logger.getLogger(TestPerformance.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    }
    @Override
    protected void eraseTempDir() {
        if (DELETE_TEMP_FILES) {
            super.eraseTempDir();
        }
    }
    public static String getOptionsDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("Input=");
        if (TestPerformance.TOP_PACKAGE.isEmpty()) {
            builder.append("*");
        }
        else {
            builder.append(TOP_PACKAGE).append(".*");
        }

        builder.append(", Grammar=").append(USE_LR_GRAMMAR ? "LR" : "Standard");
        builder.append(", ForceAtn=").append(FORCE_ATN);

        builder.append('\n');

        builder.append("Op=Lex").append(RUN_PARSER ? "+Parse" : " only");
        builder.append(", Strategy=").append(BAIL_ON_ERROR ? BailErrorStrategy.class.getSimpleName() : DefaultErrorStrategy.class.getSimpleName());
        builder.append(", BuildParseTree=").append(BUILD_PARSE_TREES);
        builder.append(", WalkBlankListener=").append(BLANK_LISTENER);

        builder.append('\n');

        builder.append("Lexer=").append(REUSE_LEXER ? "setInputStream" : "newInstance");
        builder.append(", Parser=").append(REUSE_PARSER ? "setInputStream" : "newInstance");
        builder.append(", AfterPass=").append(CLEAR_DFA ? "newInstance" : "setInputStream");

        builder.append('\n');

        return builder.toString();
    }
    protected void parse1(JavaParserFactory factory, Collection<CharStream> sources) {
        System.gc();
        parseSources(factory, sources);
    }
    protected void parse2(JavaParserFactory factory, Collection<CharStream> sources) {
        System.gc();
        parseSources(factory, sources);
    }
    protected Collection<CharStream> loadSources(File directory, boolean recursive) {
        Collection<CharStream> result = new ArrayList<CharStream>();
        loadSources(directory, recursive, result);
        return result;
    }
    protected void loadSources(File directory, boolean recursive, Collection<CharStream> result) {
        assert directory.isDirectory();

        File[] sources = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".java");
            }
        });
        for (File file : sources) {
            try {
                CharStream input = new ANTLRFileStream(file.getAbsolutePath());
                result.add(input);
            } catch (IOException ex) {

            }
        }

        if (recursive) {
            File[] children = directory.listFiles();
            for (File child : children) {
                if (child.isDirectory()) {
                    loadSources(child, true, result);
                }
            }
        }
    }
    protected void compileParser(boolean leftRecursive) throws IOException {
        String grammarFileName = "Java.g";
        String sourceName = leftRecursive ? "Java-LR.g" : "Java.g";
        String body = load(sourceName, null);
        @SuppressWarnings({"ConstantConditions"})
        List<String> extraOptions = new ArrayList<String>();
        if (FORCE_ATN) {
            extraOptions.add("-Xforce-atn");
        }
        if (EXPORT_ATN_GRAPHS) {
            extraOptions.add("-atn");
        }
        String[] extraOptionsArray = extraOptions.toArray(new String[extraOptions.size()]);
        boolean success = rawGenerateAndBuildRecognizer(grammarFileName, body, "JavaParser", "JavaLexer", extraOptionsArray);
        assertTrue(success);
    }
    protected String load(String fileName, @Nullable String encoding)
        throws IOException
    {
        if ( fileName==null ) {
            return null;
        }

        String fullFileName = getClass().getPackage().getName().replace('.', '/') + '/' + fileName;
        int size = 65000;
        InputStreamReader isr;
        InputStream fis = getClass().getClassLoader().getResourceAsStream(fullFileName);
        if ( encoding!=null ) {
            isr = new InputStreamReader(fis, encoding);
        }
        else {
            isr = new InputStreamReader(fis);
        }
        try {
            char[] data = new char[size];
            int n = isr.read(data);
            return new String(data, 0, n);
        }
        finally {
            isr.close();
        }
    }
    protected void parseSources(JavaParserFactory factory, Collection<CharStream> sources) {
        long startTime = System.currentTimeMillis();
        tokenCount = 0;
        int inputSize = 0;

        for (CharStream input : sources) {
            input.seek(0);
            inputSize += input.size();
            // this incurred a great deal of overhead and was causing significant variations in performance results.
            //System.out.format("Parsing file %s\n", input.getSourceName());
            try {
                factory.parseFile(input);
            } catch (IllegalStateException ex) {
                ex.printStackTrace(System.out);
            }
        }

        System.out.format("Total parse time for %d files (%d KB, %d tokens): %dms\n",
                          sources.size(),
                          inputSize / 1024,
                          tokenCount,
                          System.currentTimeMillis() - startTime);

        if (RUN_PARSER) {
            // make sure the individual DFAState objects actually have unique ATNConfig arrays
            final ParserATNSimulator<?> interpreter = sharedParser.getInterpreter();
            final DFA[] decisionToDFA = interpreter.decisionToDFA;

            if (SHOW_DFA_STATE_STATS) {
                int states = 0;
				int configs = 0;
				Set<ATNConfig> uniqueConfigs = new HashSet<ATNConfig>();

                for (int i = 0; i < decisionToDFA.length; i++) {
                    DFA dfa = decisionToDFA[i];
                    if (dfa == null || dfa.states == null) {
                        continue;
                    }

                    states += dfa.states.size();
					for (DFAState state : dfa.states.values()) {
						configs += state.configset.size();
						uniqueConfigs.addAll(state.configset);
					}
                }

                System.out.format("There are %d DFAState instances, %d configs (%d unique).\n", states, configs, uniqueConfigs.size());
            }

            int localDfaCount = 0;
            int globalDfaCount = 0;
            int localConfigCount = 0;
            int globalConfigCount = 0;
            int[] contextsInDFAState = new int[0];

            for (int i = 0; i < decisionToDFA.length; i++) {
                DFA dfa = decisionToDFA[i];
                if (dfa == null || dfa.states == null) {
                    continue;
                }

                if (SHOW_CONFIG_STATS) {
                    for (DFAState state : dfa.states.keySet()) {
                        if (state.configset.size() >= contextsInDFAState.length) {
                            contextsInDFAState = Arrays.copyOf(contextsInDFAState, state.configset.size() + 1);
                        }

                        if (state.isAcceptState) {
                            boolean hasGlobal = false;
                            for (ATNConfig config : state.configset) {
                                if (config.reachesIntoOuterContext > 0) {
                                    globalConfigCount++;
                                    hasGlobal = true;
                                } else {
                                    localConfigCount++;
                                }
                            }

                            if (hasGlobal) {
                                globalDfaCount++;
                            } else {
                                localDfaCount++;
                            }
                        }

                        contextsInDFAState[state.configset.size()]++;
                    }
                }
            }

            if (SHOW_CONFIG_STATS && currentPass == 0) {
                System.out.format("  DFA accept states: %d total, %d with only local context, %d with a global context\n", localDfaCount + globalDfaCount, localDfaCount, globalDfaCount);
                System.out.format("  Config stats: %d total, %d local, %d global\n", localConfigCount + globalConfigCount, localConfigCount, globalConfigCount);
                if (SHOW_DFA_STATE_STATS) {
                    for (int i = 0; i < contextsInDFAState.length; i++) {
                        if (contextsInDFAState[i] != 0) {
                            System.out.format("  %d configs = %d\n", i, contextsInDFAState[i]);
                        }
                    }
                }
            }
        }
    }
    protected JavaParserFactory getParserFactory() {
        try {
            ClassLoader loader = new URLClassLoader(new URL[] { new File(tmpdir).toURI().toURL() }, ClassLoader.getSystemClassLoader());
            @SuppressWarnings({"unchecked"})
            final Class<? extends Lexer> lexerClass = (Class<? extends Lexer>)loader.loadClass("JavaLexer");
            @SuppressWarnings({"unchecked"})
            final Class<? extends Parser> parserClass = (Class<? extends Parser>)loader.loadClass("JavaParser");
            @SuppressWarnings({"unchecked"})
            final Class<? extends ParseTreeListener<Token>> listenerClass = (Class<? extends ParseTreeListener<Token>>)loader.loadClass("JavaBaseListener");
            TestPerformance.sharedListener = listenerClass.newInstance();

            final Constructor<? extends Lexer> lexerCtor = lexerClass.getConstructor(CharStream.class);
            final Constructor<? extends Parser> parserCtor = parserClass.getConstructor(TokenStream.class);

            // construct initial instances of the lexer and parser to deserialize their ATNs
            lexerCtor.newInstance(new ANTLRInputStream(""));
            parserCtor.newInstance(new CommonTokenStream());

            return new JavaParserFactory() {
                @SuppressWarnings({"PointlessBooleanExpression"})
                @Override
                public void parseFile(CharStream input) {
                    try {
                        if (REUSE_LEXER && sharedLexer != null) {
                            sharedLexer.setInputStream(input);
                        } else {
                            sharedLexer = lexerCtor.newInstance(input);
                        }

                        CommonTokenStream tokens = new CommonTokenStream(sharedLexer);
                        tokens.fill();
                        tokenCount += tokens.size();

                        if (!RUN_PARSER) {
                            return;
                        }

                        if (REUSE_PARSER && sharedParser != null) {
                            sharedParser.setInputStream(tokens);
                        } else {
                            sharedParser = parserCtor.newInstance(tokens);
                            sharedParser.setBuildParseTree(BUILD_PARSE_TREES);
                            if (!BUILD_PARSE_TREES && BLANK_LISTENER) {
								// TJP commented out for now; changed interface
//                                sharedParser.addParseListener(sharedListener);
                            }
                            if (BAIL_ON_ERROR) {
                                sharedParser.setErrorHandler(new BailErrorStrategy());
                            }
                        }

                        Method parseMethod = parserClass.getMethod("compilationUnit");
                        Object parseResult = parseMethod.invoke(sharedParser);
                        Assert.assertTrue(parseResult instanceof ParseTree);

                        if (BUILD_PARSE_TREES && BLANK_LISTENER) {
                            ParseTreeWalker.DEFAULT.walk(sharedListener, (ParseTree)parseResult);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        throw new IllegalStateException(e);
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace(System.out);
            lastTestFailed = true;
            Assert.fail(e.getMessage());
            throw new IllegalStateException(e);
        }
    }
}
