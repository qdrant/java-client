package io.qdrant.client;

import io.qdrant.client.grpc.Points.DenseVector;
import io.qdrant.client.grpc.Points.MultiDenseVector;
import io.qdrant.client.grpc.Points.SparseIndices;
import io.qdrant.client.grpc.Points.SparseVector;
import io.qdrant.client.grpc.Points.VectorOutput;
import java.util.ArrayList;
import java.util.List;

/** Helper methods for extracting vector data from {@link VectorOutput}. */
public final class VectorOutputHelper {
  private VectorOutputHelper() {}

  /**
   * Returns the DenseVector from the VectorOutput.
   *
   * @param vectorOutput the VectorOutput to extract from
   * @return the DenseVector if available, null otherwise
   */
  public static DenseVector getDenseVector(VectorOutput vectorOutput) {
    if (vectorOutput == null) {
      return null;
    }

    List<Float> data = vectorOutput.getDataList();
    if (!data.isEmpty()) {
      return DenseVector.newBuilder().addAllData(data).build();
    }

    if (vectorOutput.hasDense()) {
      return vectorOutput.getDense();
    }

    return null;
  }

  /**
   * Returns the SparseVector from the VectorOutput.
   *
   * @param vectorOutput the VectorOutput to extract from
   * @return the SparseVector if available, null otherwise
   */
  public static SparseVector getSparseVector(VectorOutput vectorOutput) {
    if (vectorOutput == null) {
      return null;
    }

    List<Float> data = vectorOutput.getDataList();
    if (!data.isEmpty()) {
      if (vectorOutput.hasIndices()) {
        SparseIndices indices = vectorOutput.getIndices();
        return SparseVector.newBuilder()
            .addAllValues(data)
            .addAllIndices(indices.getDataList())
            .build();
      }
    }

    if (vectorOutput.hasSparse()) {
      return vectorOutput.getSparse();
    }

    return null;
  }

  /**
   * Returns the MultiDenseVector from the VectorOutput.
   *
   * @param vectorOutput the VectorOutput to extract from
   * @return the MultiDenseVector if available, null otherwise
   */
  public static MultiDenseVector getMultiVector(VectorOutput vectorOutput) {
    if (vectorOutput == null) {
      return null;
    }

    List<Float> data = vectorOutput.getDataList();
    if (!data.isEmpty()) {
      int vectorsCount = vectorOutput.getVectorsCount();
      if (vectorsCount > 0) {
        int vectorSize = data.size() / vectorsCount;
        List<DenseVector> vectors = new ArrayList<>(vectorsCount);

        for (int i = 0; i < vectorsCount; i++) {
          int start = i * vectorSize;
          int end = start + vectorSize;
          List<Float> vectorData = data.subList(start, end);

          vectors.add(DenseVector.newBuilder().addAllData(vectorData).build());
        }

        return MultiDenseVector.newBuilder().addAllVectors(vectors).build();
      }
    }

    if (vectorOutput.hasMultiDense()) {
      return vectorOutput.getMultiDense();
    }

    return null;
  }
}
