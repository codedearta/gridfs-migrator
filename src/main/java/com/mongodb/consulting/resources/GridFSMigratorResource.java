package com.mongodb.consulting.resources;

import com.mongodb.MongoClient;
import com.mongodb.consulting.io.GridFSFileExtractor;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.*;

@Path("gridfs/{bucket}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class GridFSMigratorResource {

    private String mongodbBasePath;

    public GridFSMigratorResource(String mongodbBasePath) {
        this.mongodbBasePath = mongodbBasePath;
    }

    @GET
    @Path("{id}")
    public Response loadStreamById(@PathParam("bucket") final String bucketName, @PathParam("id") String id) {
        final String innnerId = id;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {

                //Writer writer = new BufferedWriter(new OutputStreamWriter(os));

                GridFSFileExtractor extractor = new GridFSFileExtractor(new MongoClient(), mongodbBasePath);
                ByteArrayOutputStream file = (ByteArrayOutputStream) extractor.getFile("test", innnerId, bucketName, os);
                os.flush();

//                writer.write("<id>"+ innnerId + "</id>");
//
//                writer.flush();
            }
        };
        return Response.ok(stream).build();
    }
}
