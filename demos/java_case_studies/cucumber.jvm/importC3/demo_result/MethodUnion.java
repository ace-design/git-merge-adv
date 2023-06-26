package io.cucumber.core.options;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
public final class RuntimeOptionsBuilder{
    private boolean hasFeaturesWithLineFilters() {
        if (parsedRerunPaths != null) {
            return true;
        }
        for (FeatureWithLines parsedFeaturePath : parsedFeaturePaths) {
            if (!parsedFeaturePath.lines().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    public RuntimeOptionsBuilder addRerun(Collection<FeatureWithLines> featureWithLines) {
        if (parsedRerunPaths == null) {
            parsedRerunPaths = new ArrayList<>();
        }
        parsedRerunPaths.addAll(featureWithLines);
        return this;
    }
    public RuntimeOptionsBuilder addPluginName(String name, boolean isAddPlugin) {
        this.parsedPluginData.addPluginName(name, isAddPlugin);
        return this;
    }
    public RuntimeOptionsBuilder addJunitOption(String junitOption) {
        this.parsedJunitOptions.add(junitOption);
        return this;
    }
    public RuntimeOptionsBuilder setWip(boolean wip) {
        this.parsedWip = wip;
        return this;
    }
    public RuntimeOptionsBuilder addNameFilter(Pattern pattern) {
        this.parsedNameFilters.add(pattern);
        return this;
    }
    public RuntimeOptionsBuilder setSnippetType(SnippetType snippetType) {
        this.parsedSnippetType = snippetType;
        return this;
    }
    private RuntimeOptionsBuilder addLineFilters(FeatureWithLines featureWithLines) {
        URI key = featureWithLines.uri();
        Set<Integer> lines = featureWithLines.lines();
        if (lines.isEmpty()) {
            return null;
        }
        if (this.parsedLineFilters.containsKey(key)) {
            this.parsedLineFilters.get(key).addAll(lines);
        } else {
            this.parsedLineFilters.put(key, new TreeSet<>(lines));
        }
        return this;
    }
    public RuntimeOptionsBuilder setStrict() {
        return setStrict(true);
    }
    public RuntimeOptionsBuilder setMonochrome() {
        return setMonochrome(true);
    }
    public RuntimeOptionsBuilder setThreads(int threads) {
        this.parsedThreads = threads;
        return this;
    }
    public RuntimeOptionsBuilder addDefaultSummaryPrinterIfNotPresent() {
        parsedPluginData.addDefaultSummaryPrinterIfNotPresent();
        return this;
    }
    public void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.parsedObjectFactoryClass = objectFactoryClass;
    }
    public RuntimeOptionsBuilder addTagFilter(String tagExpression) {
        this.parsedTagFilters.add(tagExpression);
        return this;
    }
    public RuntimeOptions build(RuntimeOptions runtimeOptions) {
        if (this.parsedThreads != null) {
            runtimeOptions.setThreads(this.parsedThreads);
        }

        if (this.parsedDryRun != null) {
            runtimeOptions.setDryRun(this.parsedDryRun);
        }

        if (this.parsedStrict != null) {
            runtimeOptions.setStrict(this.parsedStrict);
        }

        if (this.parsedMonochrome != null) {
            runtimeOptions.setMonochrome(this.parsedMonochrome);
        }

        if (this.parsedSnippetType != null) {
            runtimeOptions.setSnippetType(this.parsedSnippetType);
        }

        if (this.parsedWip != null) {
            runtimeOptions.setWip(this.parsedWip);
        }

        if (this.parsedPickleOrder != null) {
            runtimeOptions.setPickleOrder(this.parsedPickleOrder);
        }

        if (this.parsedCount != null) {
            runtimeOptions.setCount(this.parsedCount);
        }

        if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || hasFeaturesWithLineFilters()) {
            runtimeOptions.setTagExpressions(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty() || this.parsedRerunPaths != null) {
            List<FeatureWithLines> features = new ArrayList<>(this.parsedFeaturePaths);
            if (parsedRerunPaths != null) {
                features.addAll(this.parsedRerunPaths);
            }
            runtimeOptions.setFeaturePaths(features);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }

        this.parsedPluginData.updateFormatters(runtimeOptions.getFormatters());
        this.parsedPluginData.updateSummaryPrinters(runtimeOptions.getSummaryPrinter());

        if (parsedObjectFactoryClass != null) {
            runtimeOptions.setObjectFactoryClass(parsedObjectFactoryClass);
        }

        return runtimeOptions;
    }
    public RuntimeOptionsBuilder setStrict(boolean strict) {
        this.parsedStrict = strict;
        return this;
    }
    public RuntimeOptionsBuilder setCount(int count) {
        this.parsedCount = count;
        return this;
    }
    public RuntimeOptions build(RuntimeOptions runtimeOptions) {
        if (this.parsedThreads != null) {
            runtimeOptions.setThreads(this.parsedThreads);
        }

        if (this.parsedDryRun != null) {
            runtimeOptions.setDryRun(this.parsedDryRun);
        }

        if (this.parsedStrict != null) {
            runtimeOptions.setStrict(this.parsedStrict);
        }

        if (this.parsedMonochrome != null) {
            runtimeOptions.setMonochrome(this.parsedMonochrome);
        }

        if (this.parsedSnippetType != null) {
            runtimeOptions.setSnippetType(this.parsedSnippetType);
        }

        if (this.parsedWip != null) {
            runtimeOptions.setWip(this.parsedWip);
        }

        if (this.parsedPickleOrder != null) {
            runtimeOptions.setPickleOrder(this.parsedPickleOrder);
        }

        if (this.parsedCount != null) {
            runtimeOptions.setCount(this.parsedCount);
        }

        if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || hasFeaturesWithLineFilters()) {
            runtimeOptions.setTagFilters(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty() || this.parsedRerunPaths != null) {
            List<FeatureWithLines> features = new ArrayList<>(this.parsedFeaturePaths);
            if (parsedRerunPaths != null) {
                features.addAll(this.parsedRerunPaths);
            }
            runtimeOptions.setFeaturePaths(features);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }
        if (!this.parsedJunitOptions.isEmpty()) {
            runtimeOptions.setJunitOptions(this.parsedJunitOptions);
        }

        this.parsedPluginData.updatePluginFormatterNames(runtimeOptions.getPluginFormatterNames());
        this.parsedPluginData.updatePluginStepDefinitionReporterNames(runtimeOptions.getPluginStepDefinitionReporterNames());
        this.parsedPluginData.updatePluginSummaryPrinterNames(runtimeOptions.getPluginSummaryPrinterNames());

        return runtimeOptions;
    }
    public RuntimeOptionsBuilder addFeature(FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines);
        return this;
    }
    public RuntimeOptionsBuilder setDryRun(boolean dryRun) {
        this.parsedDryRun = dryRun;
        return this;
    }
    public RuntimeOptionsBuilder addFeature(FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines.uri());
        addLineFilters(featureWithLines);
        return this;
    }
    private boolean hasFeaturesWithLineFilters() {
        return parsedRerunPaths != null || !parsedFeaturePaths.stream()
            .map(FeatureWithLines::lines)
            .allMatch(Set::isEmpty);
    }
    public RuntimeOptionsBuilder addDefaultFormatterIfNotPresent() {
        parsedPluginData.addDefaultFormatterIfNotPresent();
        return this;
    }
    public RuntimeOptionsBuilder addGlue(URI glue) {
        parsedGlue.add(glue);
        return this;
    }
    public RuntimeOptions build(RuntimeOptions runtimeOptions) {
        if (this.parsedThreads != null) {
            runtimeOptions.setThreads(this.parsedThreads);
        }

        if (this.parsedDryRun != null) {
            runtimeOptions.setDryRun(this.parsedDryRun);
        }

        if (this.parsedStrict != null) {
            runtimeOptions.setStrict(this.parsedStrict);
        }

        if (this.parsedMonochrome != null) {
            runtimeOptions.setMonochrome(this.parsedMonochrome);
        }

        if (this.parsedSnippetType != null) {
            runtimeOptions.setSnippetType(this.parsedSnippetType);
        }

        if (this.parsedWip != null) {
            runtimeOptions.setWip(this.parsedWip);
        }

        if (this.parsedPickleOrder != null) {
            runtimeOptions.setPickleOrder(this.parsedPickleOrder);
        }

        if (this.parsedCount != null) {
            runtimeOptions.setCount(this.parsedCount);
        }

        if (this.parsedIsRerun || !this.parsedFeaturePaths.isEmpty()) {
            runtimeOptions.setFeaturePaths(Collections.<URI>emptyList());
            runtimeOptions.setLineFilters(Collections.<URI, Set<Integer>>emptyMap());
        }
        if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || !this.parsedLineFilters.isEmpty()) {
            runtimeOptions.setTagFilters(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
            runtimeOptions.setLineFilters(this.parsedLineFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty()) {
            runtimeOptions.setFeaturePaths(this.parsedFeaturePaths);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }
        if (!this.parsedJunitOptions.isEmpty()) {
            runtimeOptions.setJunitOptions(this.parsedJunitOptions);
        }

        this.parsedPluginData.updatePluginFormatterNames(runtimeOptions.getPluginFormatterNames());
        this.parsedPluginData.updatePluginStepDefinitionReporterNames(runtimeOptions.getPluginStepDefinitionReporterNames());
        this.parsedPluginData.updatePluginSummaryPrinterNames(runtimeOptions.getPluginSummaryPrinterNames());

        return runtimeOptions;
    }
    public RuntimeOptionsBuilder setDryRun() {
        return setDryRun(true);
    }
    public RuntimeOptionsBuilder setMonochrome(boolean monochrome) {
        this.parsedMonochrome = monochrome;
        return this;
    }
    public void setIsRerun(boolean isRerun) {
        this.parsedIsRerun = isRerun;
    }
    public RuntimeOptionsBuilder setPickleOrder(PickleOrder pickleOrder) {
        this.parsedPickleOrder = pickleOrder;
        return this;
    }
    public RuntimeOptions build() {
        return build(RuntimeOptions.defaultOptions());
    }
    private static class ParsedPluginData{
        void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }
        void updateFormatters(List<Options.Plugin> formatter) {
            this.formatters.updateNameList(formatter);
        }
        void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }
        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinterNames.names.isEmpty()) {
                summaryPrinterNames.addName("default_summary", false);
            }
        }
        void addPluginName(String name, boolean isAddPlugin) {
            if (PluginFactory.isStepDefinitionReporterName(name)) {
                stepDefinitionReporterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }
        void addDefaultFormatterIfNotPresent() {
            if (formatters.names.isEmpty()) {
                addPluginName("progress", false);
            }
        }
        void updatePluginStepDefinitionReporterNames(List<String> pluginStepDefinitionReporterNames) {
            stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
        }
        void updateSummaryPrinters(List<Options.Plugin> pluginSummaryPrinterNames) {
            summaryPrinters.updateNameList(pluginSummaryPrinterNames);
        }
        void addDefaultFormatterIfNotPresent() {
            if (formatterNames.names.isEmpty()) {
                formatterNames.addName("progress", false);
            }
        }
        void addPluginName(String name, boolean isAddPlugin) {
            PluginOption pluginOption = PluginOption.parse(name);
            if (pluginOption.isSummaryPrinter()) {
                summaryPrinters.addName(pluginOption, isAddPlugin);
            } else if (pluginOption.isFormatter()) {
                formatters.addName(pluginOption, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }
        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinters.names.isEmpty()) {
                addPluginName("summary", false);
            }
        }
    }
    private static class ParsedOptionNames{
        void addName(String name, boolean isAddOption) {
            names.add(name);
            if (!isAddOption) {
                clobber = true;
            }
        }
        void updateNameList(List<String> nameList) {
            if (!names.isEmpty()) {
                if (clobber) {
                    nameList.clear();
                }
                nameList.addAll(names);
            }
        }
    }
    static final class ParsedPluginData{
        void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }
        void updateFormatters(List<Options.Plugin> formatter) {
            this.formatters.updateNameList(formatter);
        }
        void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }
        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinterNames.names.isEmpty()) {
                summaryPrinterNames.addName("default_summary", false);
            }
        }
        void addPluginName(String name, boolean isAddPlugin) {
            if (PluginFactory.isStepDefinitionReporterName(name)) {
                stepDefinitionReporterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }
        void addDefaultFormatterIfNotPresent() {
            if (formatters.names.isEmpty()) {
                addPluginName("progress", false);
            }
        }
        void updatePluginStepDefinitionReporterNames(List<String> pluginStepDefinitionReporterNames) {
            stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
        }
        void updateSummaryPrinters(List<Options.Plugin> pluginSummaryPrinterNames) {
            summaryPrinters.updateNameList(pluginSummaryPrinterNames);
        }
        void addDefaultFormatterIfNotPresent() {
            if (formatterNames.names.isEmpty()) {
                formatterNames.addName("progress", false);
            }
        }
        void addPluginName(String name, boolean isAddPlugin) {
            PluginOption pluginOption = PluginOption.parse(name);
            if (pluginOption.isSummaryPrinter()) {
                summaryPrinters.addName(pluginOption, isAddPlugin);
            } else if (pluginOption.isFormatter()) {
                formatters.addName(pluginOption, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }
        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinters.names.isEmpty()) {
                addPluginName("summary", false);
            }
        }
    }
}
