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

    private String mongoDbPath;
    private String dbName;
    private MongoDatabase mongoDatabase;

    public GridFSFileExtractor(MongoDatabase mongoDatabase, String mongoDbPath) {
        this.mongoDatabase = mongoDatabase;
        this.mongoDbPath = mongoDbPath;
        this.dbName = mongoDatabase.getName();
    }

    public Optional<Document> getFileMetadataById(String bucketName, ObjectId id) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(bucketName + ".files", Document.class);
        Document query = new Document("_id", id);
        return Optional.fromNullable(collection.find(query).first());
    }

    public void getFile(String bucketName, Document fileMeta, OutputStream outputStream) throws IOException
    {
        try {
            ObjectId fileId = fileMeta.getObjectId( "_id" );
            FindIterable<Document> chunkMetadata = getChunkMetadata( bucketName, fileId, Integer.MAX_VALUE);
            for (Document chunkMetadataDocument : chunkMetadata) {
                BsonBinary bsonBinary = getBsonBinary( chunkMetadataDocument);
                outputStream.write( bsonBinary.getData() );
            }
        } catch (RuntimeException ioex) {
            RuntimeException newEx = new RuntimeException( String.format("Possible file corruption in getFile(): dbName:%s, bucketName:%s, fileId:%s", this.dbName, bucketName, fileMeta.getObjectId( "_id" )), ioex);
            logger.error(newEx.getMessage(), newEx);
            throw newEx;
        }
    }

    private FindIterable<Document> getChunkMetadata(String bucketName, ObjectId id, int batchSize) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(bucketName + ".chunks", Document.class);
        Document query = new Document("$query",new Document("files_id",id)).append("$showDiskLoc", 1);
        Document projection = new Document("_id",0).append("n", 1);
        return collection.find(query).projection(projection).batchSize(batchSize);
    }

    private BsonBinary getBsonBinary(Document chunkMetadataDocument) throws IOException {
        Document diskLoc = chunkMetadataDocument.get( "$diskLoc", Document.class );
        int fileno = diskLoc.getInteger( "file", -1 );
        int offset = diskLoc.getInteger( "offset", -1 );
        RawBsonDocument chunk = readBsonDocFromFile( fileno, offset );
        return chunk.getBinary( "data" );
    }

    private RawBsonDocument readBsonDocFromFile(Integer fileno, Integer offset) throws IOException {
        String filePath = String.format("%s/%s.%s", mongoDbPath, dbName, fileno.toString());

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