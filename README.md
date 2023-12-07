<p align="center">
  <img height="120" src="https://github.com/qdrant/qdrant/raw/master/docs/logo.svg" alt="Qdrant"> 
  &nbsp;
  <img height="150" width="100" src="./resources/java-logo-small.svg" alt="Java">

</p>

<p align="center">
    <b>Java library for the <a href="https://github.com/qdrant/qdrant">Qdrant</a> vector search engine.</b>
</p>

<p align="center">
    <a href="https://qdrant.github.io/java-client"><img src="https://img.shields.io/badge/Docs-Javadoc%203.6.2-success" alt="Javadoc"></a>
    <a href="https://github.com/qdrant/java-client/actions/workflows/cd.yml"><img src="https://github.com/qdrant/java-client/actions/workflows/cd.yml/badge.svg?branch=master" alt="Tests"></a>
    <a href="https://github.com/qdrant/java-client/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-success" alt="Apache 2.0 License"></a>
    <a href="https://qdrant.to/discord"><img src="https://img.shields.io/badge/Discord-Qdrant-5865F2.svg?logo=discord" alt="Discord"></a>
    <a href="https://qdrant.to/roadmap"><img src="https://img.shields.io/badge/Roadmap-2023-bc1439.svg" alt="Roadmap 2023"></a>
</p>

# Qdrant Java Client

Java client library with handy utility methods and overloads for interfacing with [Qdrant](https://qdrant.tech/).

## 📥 Installation

> Not yet published.

> [!IMPORTANT]  
> Requires Java 8 or above.

To install the library, add the following lines to your build config file.

#### Maven
```xml
<dependency>
  <groupId>io.qdrant</groupId>
  <artifactId>client</artifactId>
  <version>1.7.0</version>
</dependency>
```

#### Scala SBT
```sbt
libraryDependencies += "io.qdrant" % "client" % "1.7.0"
```

#### Gradle
```gradle
implementation 'io.qdrant:client:1.7.0'
```

## 📖 Documentation
- [`QdrantClient` Reference](https://qdrant.github.io/java-client/io/qdrant/client/QdrantClient.html#constructor-detail)
- [Utility Methods Reference](https://qdrant.github.io/java-client/io/qdrant/client/utils/package-summary.html)

## 🔌 Connecting to Qdrant

> [!NOTE]  
> The library uses Qdrant's GRPC interface. The default port being `6334`.
> 
> Uses `TLS` if the URL protocol is `https`, plaintext otherwise.

#### Connecting to a local Qdrant instance
```java
import io.qdrant.client.QdrantClient;

QdrantClient client = new QdrantClient("http://localhost:6334");
```

#### Connecting to Qdrant cloud
```java
import io.qdrant.client.QdrantClient;

QdrantClient client = new QdrantClient("https://xyz-eg.eu-central.aws.cloud.qdrant.io:6334", "<your-api-key>");
```

## 🧪 Example Usage
<details>
<summary>Click to expand example</summary>


#### You can connect to Qdrant by instantiating a [QdrantClient](https://qdrant.github.io/java-client/io/qdrant/client/QdrantClient.html) instance.
```java
import io.qdrant.client.QdrantClient;

QdrantClient client = new QdrantClient("http://localhost:6334");

System.out.println(client.listCollections());
```
*Output*:
```
collections {
name: "Documents"
}
collections {
name: "some_collection"
}
time: 7.04541E-4
```

#### We can now perform operations on the DB. Like creating a collection, adding a point.
The library offers handy utility methods for constructing GRPC structures.

```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.qdrant.client.utils.*;

String collectionName = "Documents";

client.recreateCollection(collectionName, 6, Distance.Cosine);

Map<String, Object> map = new HashMap<>();
map.put("name", "John Doe");
map.put("age", 42);
map.put("married", true);

PointStruct point =
  PointUtil.point(
    0,
    VectorUtil.toVector(0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f),
    PayloadUtil.toPayload(map));
List<PointStruct> points = Arrays.asList;
client.upsertPoints(collectionName, points, null);
```

#### Performing a search on the vectors with filtering
```java
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.SearchResponse;

import io.qdrant.client.utils.*;

Filter filter = FilterUtil.must(FilterUtil.fieldCondition("age", FilterUtil.match(42)));
      
SearchPoints request = SearchPoints.newBuilder()
        .setCollectionName(collectionName)
        .addAllVector(Arrays.asList(0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f))
        .setFilter(filter)
        .setWithPayload(SelectorUtil.withPayload())
        .setLimit(10)
        .build();
SearchResponse result = client.searchPoints(request);

ScoredPoint result = results.getResult(0);
            
System.out.println("Similarity: " + result.getScore());
System.out.println("Payload: " + PayloadUtil.toMap(result.getPayload()));
```
*Output*:
```
Similarity: 0.9999999
Payload: {name=John Doe, married=true, age=42}
```

</details>

## ⚖️ LICENSE

Apache 2.0 © [2023](https://github.com/qdrant/java-client/blob/master/LICENSE)
