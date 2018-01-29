package com.mongodb.consulting;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class AsmlGridfsMigratorConfiguration extends Configuration {

    @NotEmpty private String mongodbFilePath = "/data/db";
    private String mongodbUser;
    private String mongodbAuthenticationDb;
    private String mongodbPassword;


    @NotEmpty private String mongodbDatabaseName;

    @Min(1)
    @Max(65535) private int mongodbPort = 27017;

    @NotEmpty private String mongodbHostname = "localhost";

    @JsonProperty
    public String getMongodbFilePath() {
        return mongodbFilePath;
    }


    @JsonProperty
    public String getMongodbHostname() {
        return this.mongodbHostname;
    }

    @JsonProperty
    public String getMongodbUser() {
        return mongodbUser;
    }

    @JsonProperty
    public String getMongodbAuthenticationDb() {
        return mongodbAuthenticationDb;
    }

    @JsonProperty
    public String getMongodbPassword() {
        return mongodbPassword;
    }

    @JsonProperty
    public int getMongodbPort() {
        return mongodbPort;
    }

    @JsonProperty
    public String getMongodbDatabaseName() {
        return mongodbDatabaseName;
    }






    @JsonProperty
    public void setMongodbFilePath(String mongodbFilePath) {
        this.mongodbFilePath = mongodbFilePath;
    }

    @JsonProperty
    public void setMongodbHostname(String mongodbHostname) {
        this.mongodbHostname = mongodbHostname;
    }

    @JsonProperty
    public void setMongodbUser(String mongodbUser) {
        this.mongodbUser = mongodbUser;
    }

    @JsonProperty
    public void setMongodbAuthenticationDb(String mongodbAuthenticationDb) {
        this.mongodbAuthenticationDb = mongodbAuthenticationDb;
    }

    @JsonProperty
    public void setMongodbPassword(String mongodbPassword) {
        this.mongodbPassword = mongodbPassword;
    }

    @JsonProperty
    public void setMongodbPort(int mongodbPort) {
        this.mongodbPort = mongodbPort;
    }

    @JsonProperty
    public void setMongodbDatabaseName(String mongodbDatabaseName) {
        this.mongodbDatabaseName = mongodbDatabaseName;
    }
}
