package io.cucumber.core.options;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;
import io.cucumber.core.snippets.SnippetType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import java.util.Collections;
import java.util.HashSet;

public final class RuntimeOptions implements FeatureOptions, FilterOptions, PluginOptions, RunnerOptions{

    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagFilters = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final Map<URI, Set<Integer>> lineFilters = new HashMap<>();
    private final SortedSet<URI> featurePaths = new TreeSet<>();
    private final List<String> junitOptions = new ArrayList<>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
    private int count = 0;
    private final List<String> pluginFormatterNames = new ArrayList<>();
    private final List<String> pluginStepDefinitionReporterNames = new ArrayList<>();
    private final List<String> pluginSummaryPrinterNames = new ArrayList<>();
    private final List<FeatureWithLines> featurePaths = new ArrayList<>();

    private RuntimeOptions() {

    }
    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }
    public void addUndefinedStepsPrinterIfSummaryNotDefined() {
        if (pluginSummaryPrinterNames.isEmpty()) {
            pluginSummaryPrinterNames.add("undefined");
        }
    }
    public int getCount() {
        return count;
    }
    List<String> getPluginFormatterNames() {
        return pluginFormatterNames;
    }
    List<String> getPluginStepDefinitionReporterNames() {
        return pluginStepDefinitionReporterNames;
    }
    List<String> getPluginSummaryPrinterNames() {
        return pluginSummaryPrinterNames;
    }
    public boolean isMultiThreaded() {
        return getThreads() > 1;
    }
    @Override
    public List<String> getPluginNames() {
        List<String> pluginNames = new ArrayList<>();
        pluginNames.addAll(getPluginFormatterNames());
        pluginNames.addAll(getPluginStepDefinitionReporterNames());
        pluginNames.addAll(getPluginSummaryPrinterNames());
        return pluginNames;
    }
    @Override
    public List<URI> getGlue() {
        return unmodifiableList(glue);
    }
    @Override
    public boolean isStrict() {
        return strict;
    }
    @Override
    public boolean isDryRun() {
        return dryRun;
    }
    public boolean isWip() {
        return wip;
    }
    @Override
    public List<URI> getFeaturePaths() {
        return unmodifiableList(new ArrayList<>(featurePaths));
    }
    @Override
    public List<Pattern> getNameFilters() {
        return unmodifiableList(nameFilters);
    }
    @Override
    public List<String> getTagFilters() {
        return unmodifiableList(tagFilters);
    }
    void setCount(int count) {
        this.count = count;
    }
    void setFeaturePaths(List<URI> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }
    void setGlue(List<URI> parsedGlue) {
        glue.clear();
        glue.addAll(parsedGlue);
    }
    void setJunitOptions(List<String> junitOptions) {
        this.junitOptions.clear();
        this.junitOptions.addAll(junitOptions);
    }
    void setLineFilters(Map<URI, Set<Integer>> lineFilters) {
        this.lineFilters.clear();
        for (URI path : lineFilters.keySet()) {
            this.lineFilters.put(path, lineFilters.get(path));
        }
    }
    void setNameFilters(List<Pattern> nameFilters) {
        this.nameFilters.clear();
        this.nameFilters.addAll(nameFilters);
    }
    void setPickleOrder(PickleOrder pickleOrder) {
        this.pickleOrder = pickleOrder;
    }
    void setTagFilters(List<String> tagFilters) {
        this.tagFilters.clear();
        this.tagFilters.addAll(tagFilters);
    }
    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        return unmodifiableMap(new HashMap<>(lineFilters));
    }
    @Override
    public int getLimitCount() {
        return getCount();
    }
    @Override
    public boolean isMonochrome() {
        return monochrome;
    }
    @Override
    public SnippetType getSnippetType() {
        return snippetType;
    }
    public List<String> getJunitOptions() {
        return unmodifiableList(junitOptions);
    }
    public int getThreads() {
        return threads;
    }
    public PickleOrder getPickleOrder() {
        return pickleOrder;
    }
    void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
    void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }
    void setSnippetType(SnippetType snippetType) {
        this.snippetType = snippetType;
    }
    void setStrict(boolean strict) {
        this.strict = strict;
    }
    void setThreads(int threads) {
        this.threads = threads;
    }
    void setWip(boolean wip) {
        this.wip = wip;
    }
    @Override
    public List<URI> getFeaturePaths() {
        Set<URI> uris = new HashSet<>();
        for (FeatureWithLines featurePath : featurePaths) {
            URI uri = featurePath.uri();
            uris.add(uri);
        }
        ArrayList<URI> toSort = new ArrayList<>(uris);
        Collections.sort(toSort);
        return unmodifiableList(toSort);
    }
    void setFeaturePaths(List<FeatureWithLines> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }
    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        Map<URI, Set<Integer>> lineFilters = new HashMap<>();
        for (FeatureWithLines featureWithLines : featurePaths) {
            SortedSet<Integer> lines = featureWithLines.lines();
            URI uri = featureWithLines.uri();
            if (lines.isEmpty()) {
                continue;
            }
            if(!lineFilters.containsKey(uri)){
                lineFilters.put(uri, new TreeSet<Integer>());
            }
            lineFilters.get(uri).addAll(lines);
        }
        return unmodifiableMap(lineFilters);
    }
    List<Plugin> getFormatters() {
        return formatters;
    }
    List<Plugin> getSummaryPrinter() {
        return summaryPrinters;
    }
    @Override
    public List<Plugin> plugins() {
        List<Plugin> plugins = new ArrayList<>();
        plugins.addAll(formatters);
        plugins.addAll(summaryPrinters);
        return plugins;
    }
    @Override
    public List<URI> getFeaturePaths() {
        return unmodifiableList(featurePaths.stream()
            .map(FeatureWithLines::uri)
            .sorted()
            .distinct()
            .collect(Collectors.toList()));
    }
    @Override
    public List<String> getTagExpressions() {
        return unmodifiableList(tagExpressions);
    }
    void setTagExpressions(List<String> tagExpressions) {
        this.tagExpressions.clear();
        this.tagExpressions.addAll(tagExpressions);
    }
    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        Map<URI, Set<Integer>> lineFilters = new HashMap<>();
        featurePaths.forEach(featureWithLines -> {
            SortedSet<Integer> lines = featureWithLines.lines();
            URI uri = featureWithLines.uri();
            if (lines.isEmpty()) {
                return;
            }
            lineFilters.putIfAbsent(uri, new TreeSet<>());
            lineFilters.get(uri).addAll(lines);
        });
        return unmodifiableMap(lineFilters);
    }
    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return objectFactoryClass;
    }
    void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.objectFactoryClass = objectFactoryClass;
    }
}

public final class RuntimeOptions implements
    io.cucumber.core.feature.Options,
    io.cucumber.core.runner.Options,
    io.cucumber.core.plugin.Options,
    io.cucumber.core.filter.Options,
    io.cucumber.core.backend.Options{

    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagExpressions = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final List<FeatureWithLines> featurePaths = new ArrayList<>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
    private int count = 0;
    private final List<Plugin> formatters = new ArrayList<>();
    private final List<Plugin> summaryPrinters = new ArrayList<>();
    private Class<? extends ObjectFactory> objectFactoryClass;

    private RuntimeOptions() {

    }
    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }
    public int getCount() {
        return count;
    }
    List<Plugin> getFormatters() {
        return formatters;
    }
    List<Plugin> getSummaryPrinter() {
        return summaryPrinters;
    }
    public boolean isMultiThreaded() {
        return getThreads() > 1;
    }
    @Override
    public List<Plugin> plugins() {
        List<Plugin> plugins = new ArrayList<>();
        plugins.addAll(formatters);
        plugins.addAll(summaryPrinters);
        return plugins;
    }
    @Override
    public List<URI> getGlue() {
        return unmodifiableList(glue);
    }
    @Override
    public boolean isStrict() {
        return strict;
    }
    @Override
    public boolean isDryRun() {
        return dryRun;
    }
    public boolean isWip() {
        return wip;
    }
    @Override
    public List<URI> getFeaturePaths() {
        return unmodifiableList(featurePaths.stream()
            .map(FeatureWithLines::uri)
            .sorted()
            .distinct()
            .collect(Collectors.toList()));
    }
    @Override
    public List<String> getTagExpressions() {
        return unmodifiableList(tagExpressions);
    }
    @Override
    public List<Pattern> getNameFilters() {
        return unmodifiableList(nameFilters);
    }
    void setCount(int count) {
        this.count = count;
    }
    void setFeaturePaths(List<FeatureWithLines> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }
    void setGlue(List<URI> parsedGlue) {
        glue.clear();
        glue.addAll(parsedGlue);
    }
    void setNameFilters(List<Pattern> nameFilters) {
        this.nameFilters.clear();
        this.nameFilters.addAll(nameFilters);
    }
    void setPickleOrder(PickleOrder pickleOrder) {
        this.pickleOrder = pickleOrder;
    }
    void setTagExpressions(List<String> tagExpressions) {
        this.tagExpressions.clear();
        this.tagExpressions.addAll(tagExpressions);
    }
    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        Map<URI, Set<Integer>> lineFilters = new HashMap<>();
        featurePaths.forEach(featureWithLines -> {
            SortedSet<Integer> lines = featureWithLines.lines();
            URI uri = featureWithLines.uri();
            if (lines.isEmpty()) {
                return;
            }
            lineFilters.putIfAbsent(uri, new TreeSet<>());
            lineFilters.get(uri).addAll(lines);
        });
        return unmodifiableMap(lineFilters);
    }
    @Override
    public int getLimitCount() {
        return getCount();
    }
    @Override
    public boolean isMonochrome() {
        return monochrome;
    }
    @Override
    public SnippetType getSnippetType() {
        return snippetType;
    }
    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return objectFactoryClass;
    }
    public int getThreads() {
        return threads;
    }
    public PickleOrder getPickleOrder() {
        return pickleOrder;
    }
    void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
    void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }
    void setSnippetType(SnippetType snippetType) {
        this.snippetType = snippetType;
    }
    void setStrict(boolean strict) {
        this.strict = strict;
    }
    void setThreads(int threads) {
        this.threads = threads;
    }
    void setWip(boolean wip) {
        this.wip = wip;
    }
    void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.objectFactoryClass = objectFactoryClass;
    }
}
