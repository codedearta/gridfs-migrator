package mongodb.com;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mongodb.com.resources.GridFSMigratorResource;
import mongodb.com.resources.HelloWorldResource;

public class AsmlGridfsMigratorApplication extends Application<AsmlGridfsMigratorConfiguration> {

    public static void main(final String[] args) throws Exception {
        new AsmlGridfsMigratorApplication().run(args);
    }

    @Override
    public String getName() {
        return "asml-gridfs-migrator";
    }

    @Override
    public void initialize(final Bootstrap<AsmlGridfsMigratorConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final AsmlGridfsMigratorConfiguration configuration,
                    final Environment environment) {
        final GridFSMigratorResource resource = new GridFSMigratorResource();
        environment.jersey().register(resource);
        final HelloWorldResource hello = new HelloWorldResource();
        environment.jersey().register(hello);
    }

}
