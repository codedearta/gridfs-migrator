package com.mongodb.consulting;

import com.mongodb.consulting.resources.GridFSMigratorResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsmlGridfsMigratorApplicationTest {
    private final Environment environment = mock(Environment.class);
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
    private final AsmlGridfsMigratorApplication application = new AsmlGridfsMigratorApplication();
    private final AsmlGridfsMigratorConfiguration config = new AsmlGridfsMigratorConfiguration();

    @Before
    public void setup() throws Exception {
        config.setMongodbFilePath("/data/db");
        when(environment.jersey()).thenReturn(jersey);
    }

    @Test
    public void registerAGridFSMigratorResource() throws Exception {
        application.run(config, environment);
        verify(jersey).register(isA(GridFSMigratorResource.class));
    }
}
