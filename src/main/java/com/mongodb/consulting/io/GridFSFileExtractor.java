package com.mongodb.consulting.io;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class GridFSFileExtractor {
    private MongoClient mongoClient;
    private String mongoPath;
    private static Logger logger;

    public GridFSFileExtractor(MongoClient mongoClient, String path) {
        logger = LoggerFactory.getLogger(GridFSFileExtractor.class);
        this.mongoClient = mongoClient;
        this.mongoPath = path;
    }

    public OutputStream getFile(String dbname, String id, String pfx, OutputStream os) throws IOException
    {
        final OutputStream baos = os;

        //This doesn't need to be a query by ID

        Document fileMeta = getFileMetadataById(dbname, pfx, id);
        if(fileMeta == null){
            logger.info("No file found");
            return null;
        }
        logger.info(fileMeta.toJson());
        String md5 = fileMeta.getString("md5");
        ObjectId fileId = fileMeta.getObjectId("_id");
        ArrayList<Document>  chunkMeta = getChunkMetadata(dbname,fileId,pfx);

        for(Document d : chunkMeta) {
            logger.info(d.toJson());
            Document diskLoc = d.get("$diskLoc",Document.class);
            int fileno = diskLoc.getInteger("file", -1);
            int offset = diskLoc.getInteger("offset",-1);
            RawBsonDocument chunk = readBsonDocFromFile(dbname,fileno,offset);
            BsonBinary b = chunk.getBinary("data");

            baos.write(b.getData());
        }
//        logger.info("File Size= " + baos.size());
//        byte[] bytearray = baos.toByteArray();
        return baos;
//        return compareChecksum(bytearray, md5, filename) ? baos : null;
    }

    public boolean compareChecksum(byte[] bytes, String md5, String filename){
        //Calculate the MD5 to verify this was correct
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] digest = messageDigest.digest(bytes);
            String digesttxt = bytesToHex(digest);
            logger.info(digesttxt);
            logger.info(md5);
            if(digesttxt.equals(md5)) {
                logger.info("MD5 digest match OK " + filename);
                return true;
            } else {
                logger.error("MD5 digest didn't match for " + filename);
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private  String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private int little2big(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    private RawBsonDocument readBsonDocFromFile( String dbname,
                                                 Integer fileno, Integer offset) {
        RawBsonDocument rval = null;
        String filepath = mongoPath + "/"+dbname+"."+fileno.toString();
        RandomAccessFile ff=null;
        try {
            ff = new RandomAccessFile(filepath, "r");
            ff.seek(offset + 16);//There is a 16 byte record header here
            //This is the length of the BSON
            byte[] l = new byte[4];
            ff.readFully(l);
            int i = little2big(l); //Swap Endien
            byte[] b = new byte[i+4];
            ff.seek(offset + 16); //Read the length bytes again
            ff.readFully(b);
            logger.info("BSON Length is " + i);

            //logger.info(bytesToHex(b));
            //Construct a RawBSONDoc
            rval = new RawBsonDocument(b);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        finally {
            if(ff != null) {

                try {
                    ff.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    return null;
                }
            }
        }

        return rval;
    }

    private ArrayList<Document>  getChunkMetadata(String dbname, ObjectId id, String pfx) {
        MongoDatabase db = mongoClient.getDatabase(dbname);

        MongoCollection<Document> collection = db.getCollection(pfx+".chunks", Document.class);

        Document query = new Document("$query",new Document("files_id",id)).append("$showDiskLoc", 1);

        Document projection = new Document("_id",0).append("n", 1);
        logger.info("Searching for" + query.toJson());
        FindIterable<Document> cursor = collection.find(query).projection(projection);
        ArrayList<Document> rval = new ArrayList<>();
        for (Document d : cursor) {
            rval.add(d);
        }

        return rval;
    }

//    private Document getFileMetadataByName(String dbname, String pfx, String filename) {
//        return getFileMetadata(dbname, pfx, new Document("filename",filename));
//    }

    private Document getFileMetadataById(String dbname, String pfx, String id) {
        return getFileMetadata(dbname, pfx, new Document("_id", new ObjectId(id)));
    }

    private Document getFileMetadata(String dbname, String pfx, Document query) {
        MongoDatabase db = mongoClient.getDatabase(dbname);
        MongoCollection<Document> collection = db.getCollection(pfx+".files", Document.class);
        return collection.find(query).first();
    }
}