package com.mongodb.consulting.resources;


import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class GridFSMigratorResourceTest {

    @Test
    public void loginHandlerRedirectsAfterPost() {
        URL url = null;
        try {
            url = new URL( "http://localhost:8080/gridfs/fs/5a6d85dad4a8c57c979af026" );
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int n;

            OutputStream output = new FileOutputStream( "myfile.xml" );
            while ((n = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, n);
            }
            output.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}