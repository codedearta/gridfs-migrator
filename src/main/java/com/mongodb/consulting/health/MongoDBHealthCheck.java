package com.mongodb.consulting.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.MongoClient;

public class MongoDBHealthCheck extends HealthCheck {
    private final MongoClient mongoClient;

    public MongoDBHealthCheck(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    protected Result check() throws Exception {
        if (mongoClient.getDatabase( "admin" ).getName().equals("admin")) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot connect to MongoDB");
        }
    }
}
