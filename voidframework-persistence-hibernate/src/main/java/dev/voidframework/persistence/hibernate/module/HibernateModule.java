package dev.voidframework.persistence.hibernate.module;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import dev.voidframework.datasource.exception.DataSourceException;
import dev.voidframework.datasource.utils.DataSourceUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.Set;

/**
 * Hibernate module.
 */
public class HibernateModule extends AbstractModule {

    private final Config configuration;

    /**
     * Build a new instance.
     *
     * @param configuration The application configuration
     */
    public HibernateModule(final Config configuration) {

        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        if (!this.configuration.hasPathOrNull("voidframework.datasource")) {
            throw new DataSourceException.NotConfigured();
        }

        final Set<String> dbConfigurationNameSet = DataSourceUtils.getAllDataSourceNames(this.configuration);
        if (dbConfigurationNameSet.isEmpty()) {
            throw new DataSourceException.NotConfigured();
        }

        String modelsJarUrlPattern = this.configuration.getString("voidframework.persistence.modelsJarUrlPattern");
        if (modelsJarUrlPattern != null && modelsJarUrlPattern.equalsIgnoreCase("auto")) {
            modelsJarUrlPattern = "(.*)";
        }

        for (final String dbConfigurationName : dbConfigurationNameSet) {
            final EntityManagerProvider entityManagerProvider = new EntityManagerProvider(dbConfigurationName, modelsJarUrlPattern);
            requestInjection(entityManagerProvider);
            bind(EntityManager.class).annotatedWith(Names.named(dbConfigurationName)).toProvider(entityManagerProvider);

            if (dbConfigurationName.equals("default")) {
                bind(EntityManager.class).toProvider(entityManagerProvider);
                bind(EntityManagerProvider.class).toInstance(entityManagerProvider);
            }
        }

        final MethodInterceptor methodInterceptor = new TransactionalInterceptor();
        requestInjection(methodInterceptor);
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), methodInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), methodInterceptor);
    }
}
