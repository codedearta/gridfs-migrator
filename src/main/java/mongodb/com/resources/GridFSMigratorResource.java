package mongodb.com.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

// Server
@Path("gridfs/{filename}")
@Produces(MediaType.APPLICATION_XML)
public class GridFSMigratorResource {
    @GET
    public Response loadStream(@PathParam("filename") String filename) {
        StreamingOutput stream = os -> {

            Writer writer = new BufferedWriter(new OutputStreamWriter(os));

            // Johns code here ....
            writer.write("<filename>"+ filename + "</filename>");

            writer.flush();
        };
        return Response.ok(stream).build();
    }
}
