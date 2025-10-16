package com.example.ThesisBackend.db;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.bson.Document;

@Component
public class MongoConnectionChecker implements CommandLineRunner {

    private final MongoClient mongoClient;

    public MongoConnectionChecker(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void run(String... args) {
        try {
            MongoDatabase database = mongoClient.getDatabase("thesisBackEnd");
            database.runCommand(new Document("ping", 1));
            System.out.println("✅ Connected to MongoDB: " + database.getName());
        } catch (Exception e) {
            System.out.println("❌ MongoDB connection failed: " + e.getMessage());
        }
    }
}
