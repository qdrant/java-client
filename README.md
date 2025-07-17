<p align="center">
  <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/qdrant/qdrant/raw/master/docs/logo-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/qdrant/qdrant/raw/master/docs/logo-light.svg">
      <img height="100" alt="Qdrant" src="https://github.com/qdrant/qdrant/raw/master/docs/logo.svg">
  </picture>
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
    <a href="https://qdrant.to/roadmap"><img src="https://img.shields.io/badge/Roadmap-2024-bc1439.svg" alt="Roadmap 2024"></a>
</p>

# Qdrant Java Client

Java client library with handy utility methods and overloads for interfacing with [Qdrant](https://qdrant.tech/).

## 📥 Installation

> [!IMPORTANT]  
> Requires Java 8 or above.

To install the library, add the following lines to your build config file.

#### Maven

```xml
<dependency>
  <groupId>io.qdrant</groupId>
  <artifactId>client</artifactId>
  <version>1.14.0</version>
</dependency>
```

#### SBT

```sbt
libraryDependencies += "io.qdrant" % "client" % "1.14.0"
```

#### Gradle

```gradle
implementation 'io.qdrant:client:1.14.0'
```

> [!NOTE]  
> Please make sure to include all necessary dependencies listed [here](https://central.sonatype.com/artifact/io.qdrant/client/dependencies) in your project.

## 📖 Documentation

- Usage examples are available throughout the [Qdrant documentation](https://qdrant.tech/documentation/quick-start/) and [API Reference](https://api.qdrant.tech/).
- [JavaDoc Reference](https://qdrant.github.io/java-client/)

## 🔌 Getting started

### Creating a client

A client can be instantiated with

```java
QdrantClient client = 
  new QdrantClient(QdrantGrpcClient.newBuilder("localhost").build());
```

which creates a client that will connect to Qdrant on <https://localhost:6334>.

Internally, the high-level client uses a low-level gRPC client to interact with
Qdrant. Additional constructor overloads provide more control over how the gRPC
client is configured. The following example configures a client to use TLS,
validating the certificate using the root CA to verify the server's identity
instead of the system's default, and also configures API key authentication:

```java
ManagedChannel channel = Grpc.newChannelBuilder(
  "localhost:6334",
  TlsChannelCredentials.newBuilder()
    .trustManager(new File("ssl/ca.crt"))
    .build())
.build();

QdrantClient client = new QdrantClient(
  QdrantGrpcClient.newBuilder(channel)
    .withApiKey("<apikey>")
    .build());
```

The client implements [`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html),
though a client will typically be created once and used for the lifetime of the
application. When a client is constructed by passing a `ManagedChannel`, the
client does not shut down the channel on close by default. The client can be
configured to shut down the channel on closing with

```java
ManagedChannel channel = Grpc.newChannelBuilder(
  "localhost:6334", 
  TlsChannelCredentials.create())
.build();

QdrantClient client = new QdrantClient(
  QdrantGrpcClient.newBuilder(channel, true)
    .withApiKey("<apikey>")
    .build());
```

All client methods return `ListenableFuture<T>`.

### Working with collections

Once a client has been created, create a new collection

```java
client.createCollectionAsync("{collection_name}",
  VectorParams.newBuilder()
    .setDistance(Distance.Cosine)
    .setSize(4)
    .build())
  .get();
```

Insert vectors into a collection

```java
// import static convenience methods
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

List<PointStruct> points =
    List.of(
        PointStruct.newBuilder()
            .setId(id(1))
            .setVectors(vectors(0.32f, 0.52f, 0.21f, 0.52f))
            .putAllPayload(
                Map.of(
                    "color", value("red"),
                    "rand_number", value(32)))
            .build(),
        PointStruct.newBuilder()
            .setId(id(2))
            .setVectors(vectors(0.42f, 0.52f, 0.67f, 0.632f))
            .putAllPayload(
                Map.of(
                    "color", value("black"),
                    "rand_number", value(53),
                    "extra_field", value(true)))
            .build());

UpdateResult updateResult = client.upsertAsync("{collection_name}", points).get();
```

Search for similar vectors

```java
List<ScoredPoint> points =
    client
        .searchAsync(
            SearchPoints.newBuilder()
                .setCollectionName("{collection_name}")
                .addAllVector(List.of(0.6235f, 0.123f, 0.532f, 0.123f))
                .setLimit(5)
                .build())
        .get();
```

Search for similar vectors with filtering condition

```java
// import static convenience methods
import static io.qdrant.client.ConditionFactory.range;

List<ScoredPoint> points = client.searchAsync(SearchPoints.newBuilder()
  .setCollectionName("{collection_name}")
  .addAllVector(List.of(0.6235f, 0.123f, 0.532f, 0.123f))
  .setFilter(Filter.newBuilder()
    .addMust(range("rand_number", Range.newBuilder().setGte(3).build()))
    .build())
  .setLimit(5)
  .build()
).get();
```

## ⚖️ LICENSE

[Apache 2.0](https://github.com/qdrant/java-client/blob/master/LICENSE)
