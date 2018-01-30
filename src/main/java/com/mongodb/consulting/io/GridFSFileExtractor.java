package com.mongodb.consulting.io;

import com.google.common.base.Optional;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GridFSFileExtractor {
    private static final Logger logger = LoggerFactory.getLogger(GridFSFileExtractor.class);

    private String mongoPath;
    private MongoDatabase mongoDatabase;

    public GridFSFileExtractor(MongoDatabase mongoDatabase, String path) {
        this.mongoDatabase = mongoDatabase;
        this.mongoPath = path;
    }

    public Optional<Document> getFileMetadataById(String pfx, ObjectId id) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(pfx + ".files", Document.class);
        Document query = new Document("_id", id);
        return Optional.fromNullable(collection.find(query).first());
    }

    public void getFile(String dbName, String bucketName, Document fileMeta, OutputStream outputStream) throws IOException
    {
        try {
            ObjectId fileId = fileMeta.getObjectId( "_id" );

            for (Document chunkMetadataDocument : getChunkMetadata( bucketName, fileId )) {
                BsonBinary bsonBinary = getBsonBinary( chunkMetadataDocument, dbName );
                outputStream.write( bsonBinary.getData() );
            }
        } catch (RuntimeException ioex) {
            RuntimeException newEx = new RuntimeException( String.format("Possible file corruption in getFile(): dbName:%s, bucketName:%s, fileId:%s", dbName, bucketName, fileMeta.getObjectId( "_id" )), ioex);
            logger.error(newEx.getMessage(), newEx);
            throw newEx;
        }
    }

    private FindIterable<Document> getChunkMetadata(String pfx, ObjectId id) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(pfx + ".chunks", Document.class);
        Document query = new Document("$query",new Document("files_id",id)).append("$showDiskLoc", 1);
        Document projection = new Document("_id",0).append("n", 1);
        return collection.find(query).projection(projection);
    }

    private BsonBinary getBsonBinary(Document chunkMetadataDocument, String dbName) throws IOException {
        Document diskLoc = chunkMetadataDocument.get( "$diskLoc", Document.class );
        int fileno = diskLoc.getInteger( "file", -1 );
        int offset = diskLoc.getInteger( "offset", -1 );
        RawBsonDocument chunk = readBsonDocFromFile( dbName, fileno, offset );
        return chunk.getBinary( "data" );
    }

    private RawBsonDocument readBsonDocFromFile(String dbname, Integer fileno, Integer offset) throws IOException {
        String filePath = String.format("%s/%s.%s", mongoPath, dbname, fileno.toString());

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
            randomAccessFile.seek(offset + 16);//There is a 16 byte record header here
            //This is the length of the BSON
            byte[] littleEndian = new byte[4];
            randomAccessFile.readFully(littleEndian);
            int bigEndian = little2big(littleEndian); //Swap Endian
            byte[] bytes = new byte[bigEndian+4];
            randomAccessFile.seek(offset + 16); //Read the length bytes again
            randomAccessFile.readFully(bytes);
            return new RawBsonDocument(bytes);
        }
    }

    private int little2big(byte[] b) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(b);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt();
    }
}