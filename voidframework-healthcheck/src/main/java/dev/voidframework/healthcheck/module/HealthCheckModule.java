package dev.voidframework.healthcheck.module;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.typesafe.config.Config;
import dev.voidframework.healthcheck.HealthCheckManager;
import dev.voidframework.healthcheck.checker.JavaVirtualMachineHealthChecker;

/**
 * Health check module.
 */
public class HealthCheckModule extends AbstractModule {

    private final Config configuration;

    /**
     * Build a new instance.
     *
     * @param configuration The application configuration
     */
    public HealthCheckModule(final Config configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        final HealthCheckManager healthCheckManager = new HealthCheckManager(this.configuration);
        requestInjection(healthCheckManager);

        bind(HealthCheckManager.class).toInstance(healthCheckManager);
        bindListener(Matchers.any(), new HealthCheckAnnotationListener(healthCheckManager));

        bind(JavaVirtualMachineHealthChecker.class).asEagerSingleton();
    }
}
