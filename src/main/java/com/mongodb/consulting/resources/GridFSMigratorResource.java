package com.mongodb.consulting.resources;

import com.google.common.base.Optional;
import com.mongodb.consulting.io.GridFSFileExtractor;
import org.bson.Document;
import org.bson.types.ObjectId;
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
    private final GridFSFileExtractor extractor;

    public static final String MD5_HEADER_NAME = "md5-checksum";

    public GridFSMigratorResource(GridFSFileExtractor extractor) {
        this.extractor = extractor;
    }

    @GET
    public Response loadStreamFor(@PathParam("bucketName") final String bucketName, @PathParam("objectId") final String objectIdString) {
        try {
            ObjectId objectId = new ObjectId(objectIdString);
            final Optional<Document> fileMetadata = extractor.getFileMetadataById(bucketName ,objectId);
            if(fileMetadata.isPresent()) {
                final Document unwrappedFileMetadata = fileMetadata.get();
                final String md5Checksum = unwrappedFileMetadata.getString(METADATA_MD5_FIELDNAME);
                logger.debug(String.format("md5 checksum of file is: %s", md5Checksum));

                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                        try {
                            extractor.getFile( bucketName, unwrappedFileMetadata, outputStream );
                        } catch (IOException ioex) {
                            IOException newEx = new IOException( String.format("Possible file corruption in file: bucketName:%s, fileId:%s", bucketName, unwrappedFileMetadata.getObjectId( "_id" )), ioex);
                            logger.error(newEx.getMessage(), newEx);
                            throw newEx;
                        }
                        finally {
                            outputStream.flush();
                        }
                    }
                };

                logger.debug(String.format("Requested file found in mongodb gridfs. bucketName: %s, objectId: %s", bucketName, objectId));
                return Response.ok(stream).header(MD5_HEADER_NAME, md5Checksum ).build();
            } else {
                logger.error(String.format("Requested file wasn't present in mongodb gridfs. bucketName: %s, objectId: %s", bucketName, objectId));
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("Requested file wasn't present in mongodb gridfs. bucketName: %s, objectId: %s", bucketName, objectId)).build();
            }
        } catch (IllegalArgumentException ex) {
            logger.error(String.format("Invalid objectId: %s", objectIdString));
            return Response.status(Response.Status.BAD_REQUEST).entity(String.format("Invalid objectId: %s", objectIdString)).build();
        }
    }
}