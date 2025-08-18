package io.qdrant.example;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.Distance;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QdrantExample {
    public static void main(String[] args) {        
        try {
            ManagedChannel channel =
                Grpc.newChannelBuilder(
                        "localhost:6334", InsecureChannelCredentials.create())
                    .build();
            QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder(channel, true).build();
            QdrantClient client = new QdrantClient(grpcClient);
                        
            
            // Create a simple collection
            String collectionName = "example_collection";
            
            client.createCollectionAsync(
                    collectionName,
                    VectorParams.newBuilder()
                        .setDistance(Distance.Cosine)
                        .setSize(128)
                        .build()
                ).get();
            

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
