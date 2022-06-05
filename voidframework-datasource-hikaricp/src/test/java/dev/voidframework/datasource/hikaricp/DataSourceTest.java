package dev.voidframework.datasource.hikaricp;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariDataSource;
import dev.voidframework.datasource.DataSourceManager;
import dev.voidframework.datasource.hikaricp.module.HikariCpDataSourceModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public final class DataSourceTest {

    private final Injector injector;

    public DataSourceTest() {
        final Config configuration = ConfigFactory.parseString("""
            voidframework.core.runInDevMode = true
            voidframework.datasource.default.driver = "org.h2.Driver"
            voidframework.datasource.default.url = "jdbc:h2:mem:unit_tests;MODE=PostgreSQL;DATABASE_TO_UPPER=TRUE;"
            voidframework.datasource.default.username = "sa"
            voidframework.datasource.default.password = "sa"
            voidframework.datasource.default.cachePrepStmts = true
            voidframework.datasource.default.prepStmtCacheSize = 250
            voidframework.datasource.default.prepStmtCacheSqlLimit = 2048
            voidframework.datasource.default.autoCommit = false
            voidframework.datasource.default.connectionInitSql = "SELECT 1 FROM DUAL"
            voidframework.datasource.default.connectionTestQuery = "SELECT 1 FROM DUAL"
            voidframework.datasource.default.connectionTimeout = 10000
            voidframework.datasource.default.idleTimeout = 30000
            voidframework.datasource.default.keepaliveTime = 0
            voidframework.datasource.default.minimumIdle = 1
            voidframework.datasource.default.maximumPoolSize = 5
            voidframework.datasource.second.driver = "org.h2.Driver"
            voidframework.datasource.second.url = "jdbc:h2:mem:unit_tests_2;MODE=PostgreSQL;DATABASE_TO_UPPER=TRUE;"
            voidframework.datasource.second.username = "sa"
            voidframework.datasource.second.password = "sa"
            """);
        this.injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                install(new HikariCpDataSourceModule());
                bind(Config.class).toInstance(configuration);
            }
        });
    }

    @Test
    public void defaultConnection() throws SQLException {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final Connection connection = dataSourceManager.getConnection();
        Assertions.assertNotNull(connection);
        connection.close();
    }

    @Test
    public void specificConnection() throws SQLException {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final Connection connection = dataSourceManager.getConnection("second");
        Assertions.assertNotNull(connection);
        connection.close();
    }

    @Test
    public void unknownConnection() throws SQLException {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final Connection connection = dataSourceManager.getConnection("unknown");
        Assertions.assertNull(connection);
    }

    @Test
    public void defaultDataSource() throws SQLException {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final DataSource dataSource = dataSourceManager.getDataSource();
        Assertions.assertTrue(dataSource instanceof HikariDataSource);

        final Connection connection = dataSource.getConnection();
        Assertions.assertNotNull(connection);
        connection.close();
    }

    @Test
    public void specificDataSource() throws SQLException {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final DataSource dataSource = dataSourceManager.getDataSource("second");
        Assertions.assertTrue(dataSource instanceof HikariDataSource);

        final Connection connection = dataSource.getConnection();
        Assertions.assertNotNull(connection);
        connection.close();
    }

    @Test
    public void unknownDataSource() {
        final DataSourceManager dataSourceManager = this.injector.getInstance(DataSourceManager.class);
        Assertions.assertNotNull(dataSourceManager);

        final DataSource dataSource = dataSourceManager.getDataSource("unknown");
        Assertions.assertNull(dataSource);
    }
}