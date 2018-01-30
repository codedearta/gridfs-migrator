package com.mongodb.consulting.resources;

import com.google.common.base.Optional;
import com.mongodb.consulting.io.GridFSFileExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GridFSMigratorResourceTest {


    @Test
    public void loadStreamByIdShouldReturnStatus200(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        ObjectId objectId = new ObjectId();
        when(gfsExtractor.getFileMetadataById( "fs", objectId )).thenReturn( Optional.of(new Document()));

        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor, "test");

        Response response = resource.loadStreamFor( "fs", objectId.toString() );
        assertEquals(response.getStatus(), 200);
    }


    @Test
    public void loadStreamForWithInvalidIdShouldReturnStatus400(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor, "test");
        String objectIdString = "123235Blahsdf123";

        Response response = resource.loadStreamFor( "fs", objectIdString );

        assertEquals(response.getStatus(), 400);
        assertEquals(response.getEntity().toString(), "Invalid objectId: " + objectIdString);
    }

    @Test
    public void loadStreamForWithInvalidBucketNameShouldReturnStatus404(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        String dbName = "test";
        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor, dbName );
        ObjectId objectId = new ObjectId();
        String objectIdString = objectId.toString();
        String bucketName = "fantasy";
        when(gfsExtractor.getFileMetadataById( bucketName, objectId )).thenReturn( Optional.<Document>absent() );

        Response response = resource.loadStreamFor( bucketName, objectIdString );


        assertEquals(response.getStatus(), 404);
        assertEquals(response.getEntity().toString(), String.format("Requested file wasn't present in mongodb gridfs. dbName: %s, bucketName: %s, objectId: %s", dbName, bucketName, objectIdString));
    }


}
