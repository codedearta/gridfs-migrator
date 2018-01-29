package com.mongodb.consulting.db;

import com.mongodb.MongoClient;
import io.dropwizard.lifecycle.Managed;

public class MongoDBClientManager implements Managed {


    private MongoClient mongoClient;

    public MongoDBClientManager(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        this.mongoClient.close();
    }
}
