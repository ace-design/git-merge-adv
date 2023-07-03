package io.cucumber.junit;
import io.cucumber.core.backend.ObjectFactoryServiceLoader;
import io.cucumber.core.event.TestRunFinished;
import io.cucumber.core.event.TestRunStarted;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.FeatureLoader;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import org.apiguardian.api.API;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import java.time.Clock;
import java.util.List;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;

@API(status = API.Status.STABLE)
public final class Cucumber extends ParentRunner<FeatureRunner>{

    private final List<FeatureRunner> children;
    private final EventBus bus;
    private final List<CucumberFeature> features;
    private final Plugins plugins;
    private boolean multiThreadingAssumed = false;

    public Cucumber(Class clazz) throws InitializationError {
        super(clazz);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser(resourceLoader)
            .withOptionsProvider(new JUnitCucumberOptionsProvider())
            .parse(clazz)
            .build(propertiesFileOptions);

        RuntimeOptions environmentOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromEnvironment())
            .build(annotationOptions);

        RuntimeOptions runtimeOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromSystemProperties())
            .build(environmentOptions);

        // Next parse the junit options
        JUnitOptions junitPropertiesFileOptions = new JUnitOptionsParser()
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        JUnitOptions junitAnnotationOptions = new JUnitOptionsParser()
            .parse(clazz)
            .build(junitPropertiesFileOptions);

        JUnitOptions junitEnvironmentOptions = new JUnitOptionsParser()
            .parse(CucumberProperties.fromEnvironment())
            .build(junitAnnotationOptions);

        JUnitOptions junitOptions = new JUnitOptionsParser()
            .parse(CucumberProperties.fromSystemProperties())
            .setStrict(runtimeOptions.isStrict())
            .build(junitEnvironmentOptions);

        // Parse the features early. Don't proceed when there are lexer errors
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        this.features = featureSupplier.get();

        // Create plugins after feature parsing to avoid the creation of empty files on lexer errors.
        this.plugins = new Plugins(new PluginFactory(), runtimeOptions);
        this.bus = new TimeServiceEventBus(Clock.systemUTC());

        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendSupplier backendSupplier = new BackendServiceLoader(resourceLoader, objectFactorySupplier);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classFinder, runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
        Predicate<CucumberPickle> filters = new Filters(runtimeOptions);
        this.children = features.stream()
                .map(feature -> FeatureRunner.create(feature, filters, runnerSupplier, junitOptions))
                .filter(runner -> !runner.isEmpty())
                .collect(toList());
    }
    @Override
    protected List<FeatureRunner> getChildren() {
        return children;
    }
    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }
    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }
    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        Statement runFeatures = super.childrenInvoker(notifier);
        return new RunCucumber(runFeatures);
    }
    @Override
    public void setScheduler(RunnerScheduler scheduler) {
        super.setScheduler(scheduler);
        multiThreadingAssumed = true;
    }
}
