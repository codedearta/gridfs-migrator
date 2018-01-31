package com.mongodb.consulting;

import com.mongodb.consulting.resources.GridFSMigratorResource;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;


import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Category(IntegrationTest.class)
public class GridFSMigratorResourceTest {

    @Test
    public void downloadStream() throws URISyntaxException, IOException, NoSuchAlgorithmException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String bucketName = "fs";
            String objectId = "5a6d85dad4a8c57c979af026";
            String path = String.format( "%s/%s", bucketName, objectId );

            URI uri = new URIBuilder().setScheme( "http" ).setPort( 8080 ).setHost( "localhost" ).setPath(path).build();
            HttpGet httpGet = new HttpGet(uri);

            try(CloseableHttpResponse response = client.execute(httpGet)) {
                StatusLine status = response.getStatusLine();
                if(status.getStatusCode() == HttpStatus.SC_OK) {


                    HttpEntity entity = response.getEntity();
                    try (InputStream input = entity.getContent()) {
                        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                            IOUtils.copy( input, output );

                            byte[] bytes = output.toByteArray();
                            String md5 = response.getFirstHeader( GridFSMigratorResource.MD5_HEADER_NAME ).getValue();
                            Assert.assertTrue( "md5 hashes don't match", checkMd5( bytes, md5 ) );
                        }
                    }
                } else {
                    Assert.fail(status.getReasonPhrase() + " - " + status.getStatusCode());
                }
            }
        }
    }

    public void loadFile(String id) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String bucketName = "fs";
            String objectId = id;
            String path = String.format( "%s/%s", bucketName, objectId );

            URI uri = new URIBuilder().setScheme( "http" ).setPort( 8080 ).setHost( "localhost" ).setPath(path).build();
            HttpGet httpGet = new HttpGet(uri);

            try(CloseableHttpResponse response = client.execute(httpGet)) {
                StatusLine status = response.getStatusLine();
                if(status.getStatusCode() == HttpStatus.SC_OK) {


                    HttpEntity entity = response.getEntity();
                    try (InputStream input = entity.getContent()) {
                        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                            IOUtils.copy( input, output );

                            byte[] bytes = output.toByteArray();
                            String md5 = response.getFirstHeader( GridFSMigratorResource.MD5_HEADER_NAME ).getValue();
                            Assert.assertTrue( "md5 hashes don't match", checkMd5( bytes, md5 ) );
                        }
                    }
                } else {
                    Assert.fail(status.getReasonPhrase());
                }
            }
        }
    }

    @Test
    public void radAllData() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("fs.files.json").getFile());
        String contents = new String(Files.readAllBytes( file.toPath() ));
        JSONArray jsonarray = new JSONArray(contents);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            JSONObject id = jsonobject.getJSONObject( "_id" );
            String oid = id.getString( "$oid" );
            loadFile( oid );
        }
    }

    private boolean checkMd5(byte[] byteArray, String expectedMd5) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance( "MD5" );
        byte[] digest = messageDigest.digest(byteArray);
        String digestTxt = DatatypeConverter.printHexBinary(digest);
        return digestTxt.equals(expectedMd5.toUpperCase());
    }
}