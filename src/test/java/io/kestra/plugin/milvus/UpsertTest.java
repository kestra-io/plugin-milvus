package io.kestra.plugin.milvus;

import io.kestra.core.models.property.Property;
import io.kestra.core.serializers.FileSerde;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.exception.MilvusClientException;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpsertTest extends MilvusTest {

    @BeforeEach
    void setUpCollection() {
        var client = milvusClient();
        try {
            client.createCollection(CreateCollectionReq.builder()
                .collectionName(collectionName)
                .dimension(DIMENSION)
                .metricType(IndexParam.MetricType.COSINE.name())
                .primaryFieldName("id")
                .idType(DataType.Int64)
                .autoID(false)
                .vectorFieldName("vector")
                .build());
        } finally {
            client.close();
        }
    }

    @Test
    void run() throws Exception {
        var runContext = runContextFactory.of();

        List<Map<String, Object>> rows = List.of(
            Map.of("id", 1, "vector", List.of(0.05, 0.61, 0.76, 0.74)),
            Map.of("id", 2, "vector", List.of(0.19, 0.81, 0.75, 0.11))
        );

        var task = Upsert.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .rows(rows)
            .batchSize(Property.ofValue(10))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getUpsertedCount(), is(2L));
    }

    @Test
    void runUpsertFromInternalStorage() throws Exception {
        var runContext = runContextFactory.of();

        var tempFile = runContext.workingDir().createTempFile(".ion").toFile();
        try (var output = new FileOutputStream(tempFile)) {
            FileSerde.write(output, Map.of("id", 10, "vector", List.of(0.05, 0.61, 0.76, 0.74)));
            FileSerde.write(output, Map.of("id", 11, "vector", List.of(0.19, 0.81, 0.75, 0.11)));
        }

        var storageUri = runContext.storage().putFile(tempFile);

        var task = Upsert.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .rows(storageUri.toString())
            .batchSize(Property.ofValue(1))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getUpsertedCount(), is(2L));
    }

    @Test
    void runUpsertFromInternalStorageWithTemporalField() throws Exception {
        var runContext = runContextFactory.of();

        var tempFile = runContext.workingDir().createTempFile(".ion").toFile();
        try (var output = new FileOutputStream(tempFile)) {
            FileSerde.write(output, Map.of(
                "id", 40,
                "vector", List.of(0.05, 0.61, 0.76, 0.74),
                "createdAt", Instant.parse("2024-01-01T00:00:00Z")
            ));
        }

        var storageUri = runContext.storage().putFile(tempFile);

        var task = Upsert.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .rows(storageUri.toString())
            .batchSize(Property.ofValue(1))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getUpsertedCount(), is(1L));
    }

    @Test
    void runUpsertWithMismatchedVectorDimensionThrows() {
        var runContext = runContextFactory.of();

        var task = Upsert.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .rows(List.of(Map.of("id", 50, "vector", List.of(0.05, 0.61, 0.76))))
            .batchSize(Property.ofValue(10))
            .build();

        var exception = assertThrows(MilvusClientException.class, () -> task.run(runContext));
        assertThat(exception.getMessage(), containsString("dim"));
    }

    @Test
    void runRenderedExpressions() throws Exception {
        var runContext = runContextFactory.of(Map.of(
            "inputs", Map.of(
                "milvusUri", uri(),
                "collection", collectionName
            )
        ));

        var task = Upsert.builder()
            .uri(Property.ofExpression("{{ inputs.milvusUri }}"))
            .collectionName(Property.ofExpression("{{ inputs.collection }}"))
            .rows(List.of(Map.of("id", 20, "vector", List.of(0.05, 0.61, 0.76, 0.74))))
            .batchSize(Property.ofValue(10))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getUpsertedCount(), is(1L));
    }

    @AfterEach
    void tearDown() {
        var client = milvusClient();
        try {
            client.dropCollection(DropCollectionReq.builder().collectionName(collectionName).build());
        } catch (Exception ignored) {
        } finally {
            client.close();
        }
    }
}
