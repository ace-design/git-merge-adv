package io.cucumber.testng;
import io.cucumber.core.event.TestRunFinished;
import io.cucumber.core.event.TestRunStarted;
import io.cucumber.core.event.TestSourceRead;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.FeatureLoader;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import org.apiguardian.api.API;
import org.testng.SkipException;
import java.util.List;
import io.cucumber.core.runner.Runner;
import java.util.function.Predicate;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import cucumber.runner.TimeService;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.Env;
import gherkin.events.PickleEvent;
import io.cucumber.core.options.EnvironmentOptionsParser;
import cucumber.runner.*;
import cucumber.runtime.*;

@API(status = API.Status.STABLE)
public final class TestNGCucumberRunner {

    private final EventBus bus;
    private final Filters filters;
    private final FeaturePathFeatureSupplier featureSupplier;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final RuntimeOptions runtimeOptions;
    private final Plugins plugins;
    private final Predicate<CucumberPickle> filters;

    public TestNGCucumberRunner(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser(resourceLoader)
            .withOptionsProvider(new TestNGCucumberOptionsProvider())
            .parse(clazz)
            .build();
        runtimeOptions = new EnvironmentOptionsParser(resourceLoader)
            .parse(Env.INSTANCE)
            .build(annotationOptions);

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        filters = new Filters(runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
    }
    public void runScenario(PickleEvent pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        runner.runPickle(pickle);
        testCaseResultListener.finishExecutionUnit();

        if (!testCaseResultListener.isPassed()) {
            throw testCaseResultListener.getError();
        }
    }
    public void runScenario(Pickle pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        CucumberPickle cucumberPickle = pickle.getCucumberPickle();
        runner.runPickle(cucumberPickle);
        testCaseResultListener.finishExecutionUnit();

        if (testCaseResultListener.isPassed()) {
            return;
        }

        // Log the reason we skipped the test. TestNG doesn't provide it by
        // default
        Throwable error = testCaseResultListener.getError();
        if (error instanceof SkipException) {
            SkipException skipException = (SkipException) error;
            if (skipException.isSkip()) {
                System.out.println(format("Skipped scenario: '%s'. %s",
                    cucumberPickle.getName(),
                    skipException.getMessage()
                ));
            }
        }

        // null pointer is covered by isPassed
        // noinspection ConstantConditions
        throw error;
    }
    public void finish() {
        bus.send(new TestRunFinished(bus.getInstant()));
    }
    public Object[][] provideScenarios() {
        try {
            return getFeatures().stream()
                .flatMap(feature -> feature.getPickles().stream()
                    .filter(filters)
                    .map(cucumberPickle -> new Object[]{
                        new PickleWrapperImpl(new Pickle(cucumberPickle)),
                        new FeatureWrapperImpl(feature)}))
                .collect(toList())
                .toArray(new Object[0][0]);
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e), null}};
        }
    }
    private List<CucumberFeature> getFeatures() {
        plugins.setSerialEventBusOnEventListenerPlugins(bus);

        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getInstant()));
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getInstant(), feature.getUri().toString(), feature.getSource()));
        }
        return features;
    }
}
