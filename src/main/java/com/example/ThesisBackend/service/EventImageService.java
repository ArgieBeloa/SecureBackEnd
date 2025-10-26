package com.example.ThesisBackend.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class EventImageService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    /**
     * ✅ Store image using eventId as same _id in GridFS
     */
    public String storeImageWithEventId(String eventId, MultipartFile file) throws IOException {
        // Delete any existing image for this eventId
        deleteImage(eventId);

        ObjectId objectId = new ObjectId(eventId);

        // Include the _id in metadata
        Document metadata = new Document("contentType", file.getContentType())
                .append("_id", objectId);

        // Store the file
        ObjectId storedId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );

        System.out.println("✅ Stored image for eventId: " + eventId);
        return storedId.toHexString();
    }




    /**
     * ✅ Retrieve image bytes by Event ID
     */
    public byte[] getImageById(String id) {
        try {
            GridFSBucket bucket = GridFSBuckets.create(mongoDatabaseFactory.getMongoDatabase());
            try (InputStream stream = bucket.openDownloadStream(new ObjectId(id))) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            System.out.println("❌ Image not found or error reading ID: " + id + " (" + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * ✅ Get image content type by Event ID
     */
    public String getImageContentType(String id) {
        GridFSFile file = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(id)))
        );

        if (file != null && file.getMetadata() != null) {
            String type = file.getMetadata().getString("contentType");
            if (type != null && !type.isEmpty()) return type;
        }

        return MediaType.IMAGE_JPEG_VALUE;
    }

    /**
     * ✅ Delete existing image by Event ID
     */
    public void deleteImage(String id) {
        try {
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(id))));
            System.out.println("🗑️ Deleted existing image for ID: " + id);
        } catch (Exception e) {
            System.out.println("⚠️ No existing image to delete for ID: " + id);
        }
    }
}
