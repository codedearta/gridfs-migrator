package com.mongodb.consulting.resources;

import com.google.common.base.Optional;
import com.mongodb.consulting.io.GridFSFileExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;


@Path("{bucketName}/{objectId}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class GridFSMigratorResource {
    private static final Logger logger = LoggerFactory.getLogger(GridFSMigratorResource.class);
    private static final String METADATA_MD5_FIELDNAME = "md5";
    static final String MD5_HEADER_NAME = "md5-checksum";

    private GridFSFileExtractor extractor;
    private String dbName;


    public GridFSMigratorResource(GridFSFileExtractor extractor, String dbName) {
        this.extractor = extractor;
        this.dbName = dbName;
    }

    @GET
    public Response loadStreamById(@PathParam("bucketName") final String bucketName, @PathParam("objectId") final String objectIdString) {
        try {
            ObjectId objectId = new ObjectId(objectIdString);
            final Optional<Document> fileMetadata = extractor.getFileMetadataById(bucketName ,objectId);
            if(fileMetadata.isPresent()) {
                final Document unwrappedDoc = fileMetadata.get();
                final String md5Checksum = unwrappedDoc.getString(METADATA_MD5_FIELDNAME);
                logger.debug(String.format("md5 checksum of file is: %s", md5Checksum));

                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                        extractor.getFile(dbName, bucketName, unwrappedDoc, outputStream );
                        outputStream.flush();
                    }
                };

                logger.debug(String.format("Requested file found in mongodb gridfs. dbName: %s, bucketName: %s, objectId: %s", dbName, bucketName, objectId));
                return Response.ok(stream).header(MD5_HEADER_NAME, md5Checksum ).build();
            } else {
                logger.error(String.format("Requested file wasn't present in mongodb gridfs. dbName: %s, bucketName: %s, objectId: %s", dbName, bucketName, objectId));
                return Response.status( HttpStatus.NOT_FOUND_404).build();
            }
        } catch (IllegalArgumentException ex) {
            logger.error(String.format("Invalid objectId: %s", objectIdString));
            return Response.status( HttpStatus.BAD_REQUEST_400).build();
        }
    }
}