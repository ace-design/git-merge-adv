package io.cucumber.testng;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.backend.ObjectFactoryServiceLoader;
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
import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import java.time.Clock;
import java.util.List;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.testng.SkipException;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;


/**
 * Glue code for running Cucumber via TestNG.
 * <p>
 * Options can be provided in order of precedence by:
 * <ol>
 * <li>Setting {@value Constants#CUCUMBER_OPTIONS_PROPERTY_NAME} property in {@link System#getProperties()} ()}</li>
 * <li>Setting {@value Constants#CUCUMBER_OPTIONS_PROPERTY_NAME} property in {@link System#getenv()}</li>
 * <li>Annotating the runner class with {@link CucumberOptions}</li>
 * <li>Setting {@value Constants#CUCUMBER_OPTIONS_PROPERTY_NAME} property in {@value Constants#CUCUMBER_PROPERTIES_FILE_NAME}</li>
 * </ol>
 */
@API(status = API.Status.STABLE)
public final class TestNGCucumberRunner {
    private final EventBus bus;

    private final Predicate<CucumberPickle> filters;

    private final ThreadLocalRunnerSupplier runnerSupplier;

    private final RuntimeOptions runtimeOptions;

    private final Plugins plugins;

    private final FeaturePathFeatureSupplier featureSupplier;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the {@link CucumberOptions}
     *              and {@link org.testng.annotations.Test} annotations
     */
    public TestNGCucumberRunner(Class clazz) {

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser(resourceLoader)
            .withOptionsProvider(new TestNGCucumberOptionsProvider())
            .parse(clazz)
            .build(propertiesFileOptions);

        RuntimeOptions environmentOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromEnvironment())
            .build(annotationOptions);

<<<<<<< LEFT
        runtimeOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromSystemProperties())
            .build(environmentOptions);

=======
        runtimeOptions.addUndefinedStepsPrinterIfSummaryNotDefined();

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
>>>>>>> RIGHT
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

        this.bus = new TimeServiceEventBus(Clock.systemUTC());
        this.plugins = new Plugins(new PluginFactory(), runtimeOptions);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(resourceLoader, objectFactorySupplier);
        this.filters = new Filters(runtimeOptions);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classFinder, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
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
            SkipException skipException = ((SkipException) (error));
            if (skipException.isSkip()) {
                System.out.println(format("Skipped scenario: '%s'. %s", cucumberPickle.getName(), skipException.getMessage()));
            }
        }
        // null pointer is covered by isPassed
        // noinspection ConstantConditions
        throw error;
    }

    public void finish() {
        bus.send(new TestRunFinished(bus.getInstant()));
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            return getFeatures().stream().flatMap(( feature) -> feature.getPickles().stream().filter(filters).map(( cucumberPickle) -> new Object[]{ new PickleWrapperImpl(new Pickle(cucumberPickle)), new FeatureWrapperImpl(feature) })).collect(toList()).toArray(new Object[0][0]);
        } catch (CucumberException e) {
            return new Object[][]{ new Object[]{ new CucumberExceptionWrapper(e), null } };
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
