package com.mongodb.consulting;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;

public class AsmlGridfsMigratorConfiguration extends Configuration {
    @NotEmpty
    private String mongodbFilePath = "/data/db";

    @JsonProperty
    public String getMongodbFilePath() {
        return mongodbFilePath;
    }

    @JsonProperty
    public void setMongodbFilePath(String mongodbFilePath) {
        this.mongodbFilePath = mongodbFilePath;
    }
}
