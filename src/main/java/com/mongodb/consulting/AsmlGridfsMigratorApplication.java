package com.mongodb.consulting;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.consulting.db.MongoDBClientManager;
import com.mongodb.consulting.health.MongoDBHealthCheck;
import com.mongodb.consulting.io.GridFSFileExtractor;
import com.mongodb.consulting.resources.GridFSMigratorResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Arrays;

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

        MongoClient mongoClient;
        if(Strings.isNullOrEmpty(configuration.getMongodbUser())) {
            mongoClient = new MongoClient(new ServerAddress(configuration.getMongodbHostname(), configuration.getMongodbPort()));
        } else {
            MongoCredential credential = MongoCredential.createCredential(
                    configuration.getMongodbUser(),
                    configuration.getMongodbAuthenticationDb(),
                    configuration.getMongodbPassword().toCharArray() );

            mongoClient = new MongoClient(new ServerAddress(configuration.getMongodbHostname(), configuration.getMongodbPort()),
                    Arrays.asList(credential));
        }

        MongoDBClientManager mongoDBClientManager = new MongoDBClientManager( mongoClient );
        environment.lifecycle().manage(mongoDBClientManager);
        environment.healthChecks().register("MongoDB", new MongoDBHealthCheck(mongoClient));

        String mongodbDatabaseName = configuration.getMongodbDatabaseName();
        MongoDatabase database = mongoClient.getDatabase( mongodbDatabaseName );
        GridFSFileExtractor extractor = new GridFSFileExtractor(database, configuration.getMongodbFilePath());
        final GridFSMigratorResource resource = new GridFSMigratorResource(extractor, mongodbDatabaseName);
        environment.jersey().register(resource);
    }

}
