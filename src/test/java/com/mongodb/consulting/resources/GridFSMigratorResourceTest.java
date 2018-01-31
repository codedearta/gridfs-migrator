package com.mongodb.consulting.resources;

import com.google.common.base.Optional;
import com.mongodb.consulting.io.GridFSFileExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GridFSMigratorResourceTest {

    @Test
    public void loadStreamByIdShouldReturnStatus200(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        ObjectId objectId = new ObjectId();
        when(gfsExtractor.getFileMetadataById( "fs", objectId )).thenReturn( Optional.of(new Document()));

        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor);

        Response response = resource.loadStreamFor( "fs", objectId.toString() );
        assertEquals(200, response.getStatus());
    }

    @Test
    public void loadStreamForWithInvalidIdShouldReturnStatus400(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor);
        String objectIdString = "123235Blahsdf123";

        Response response = resource.loadStreamFor( "fs", objectIdString );

        assertEquals(400, response.getStatus());
        assertEquals("Invalid objectId: " + objectIdString, response.getEntity().toString());
    }

    @Test
    public void loadStreamForWithInvalidBucketNameShouldReturnStatus404(){
        GridFSFileExtractor gfsExtractor = mock( GridFSFileExtractor.class );
        GridFSMigratorResource resource = new GridFSMigratorResource(gfsExtractor);
        ObjectId objectId = new ObjectId();
        String objectIdString = objectId.toString();
        String bucketName = "fantasy";
        when(gfsExtractor.getFileMetadataById( bucketName, objectId )).thenReturn( Optional.<Document>absent() );

        Response response = resource.loadStreamFor( bucketName, objectIdString );


        assertEquals(404, response.getStatus());
        assertEquals(response.getEntity().toString(), String.format("Requested file wasn't present in mongodb gridfs. bucketName: %s, objectId: %s", bucketName, objectIdString));
    }
}
