package com.example.finstatest;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Singleton helper to manage a MongoClient connected to your Atlas cluster.
 */
public class MongoUtil {

    // Replace this with your Atlas connection URI
    private static final String ATLAS_URI =
            "mongodb+srv://mckinn53:eoqj6lk5bzlu9q4q@finstacluster1.it3foso.mongodb.net/?retryWrites=true&w=majority";

    // The name of the database to use
    private static final String DATABASE_NAME = "finsta_app_db";

    private static MongoClient mongoClientInstance;

    private MongoUtil() { }

    /** Returns a singleton MongoClient. */
    public static synchronized MongoClient getMongoClient() {
        if (mongoClientInstance == null) {
            ConnectionString connString = new ConnectionString(ATLAS_URI);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .build();
            mongoClientInstance = MongoClients.create(settings);
        }
        return mongoClientInstance;
    }

    /** Returns the “finsta_app_db” database. */
    public static MongoDatabase getAppDatabase() {
        return getMongoClient().getDatabase(DATABASE_NAME);
    }
}
