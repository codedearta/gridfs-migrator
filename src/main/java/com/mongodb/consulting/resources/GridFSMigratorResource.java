package com.mongodb.consulting.resources;

import com.mongodb.consulting.io.GridFSFileExtractor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

@Path("gridfs/{filename}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class GridFSMigratorResource {

    @GET
    public Response loadStream(@PathParam("filename") String filename) {
        final String innnerFilename = filename;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {

                Writer writer = new BufferedWriter(new OutputStreamWriter(os));

                GridFSFileExtractor extractor = new GridFSFileExtractor(null,"bls");
                ByteArrayOutputStream file = (ByteArrayOutputStream) extractor.getFile("lis", "some.xml", "methrology", os);
                writer.write("<filename>"+ innnerFilename + "</filename>");

                writer.flush();
            }
        };
        return Response.ok(stream).build();
    }
}