package com.mongodb.consulting;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.mongodb.consulting.resources.GridFSMigratorResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GridfsMigratorApplicationTest {
    private final Environment environment = mock(Environment.class);
    private final LifecycleEnvironment lifecycle = mock( LifecycleEnvironment.class );
    private final HealthCheckRegistry healthCheckRegistry = mock( HealthCheckRegistry.class );
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
    private final GridfsMigratorApplication application = new GridfsMigratorApplication();
    private final GridfsMigratorConfiguration config = new GridfsMigratorConfiguration();

    @Before
    public void setup() throws Exception {
        config.setMongodbFilePath("/data/db");
        config.setMongodbDatabaseName("test");
        when(environment.jersey()).thenReturn(jersey);
        when(environment.lifecycle()).thenReturn(lifecycle);
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
    }

    @Test
    public void registerAGridFSMigratorResource() throws Exception {
        application.run(config, environment);
        verify(jersey).register(isA(GridFSMigratorResource.class));
    }
}
