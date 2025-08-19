package io.qdrant.example;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.Distance;
import java.util.List;

public class QdrantExample {
    public static void main(String[] args) {        
        try {
            QdrantClient client = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false)
            .build());
                        
                        
            // Create a simple collection
            String collectionName = "example_collection";

            if (!client.collectionExistsAsync(collectionName).get()) {
                client.createCollectionAsync(
                        collectionName,
                        VectorParams.newBuilder()
                            .setDistance(Distance.Cosine)
                            .setSize(128)
                            .build()
                    ).get();
            }

            List<String> collections = client.listCollectionsAsync().get();
            
            for (String collection : collections) {
                System.out.println("- " + collection);
            }
        
            client.close();
            
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
