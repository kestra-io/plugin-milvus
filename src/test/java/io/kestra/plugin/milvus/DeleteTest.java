package io.kestra.plugin.milvus;

import io.kestra.core.models.property.Property;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeleteTest extends MilvusTest {

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

            client.upsert(UpsertReq.builder()
                .collectionName(collectionName)
                .data(List.of(
                    toJsonObject(Map.of("id", 1, "vector", List.of(0.05f, 0.61f, 0.76f, 0.74f))),
                    toJsonObject(Map.of("id", 2, "vector", List.of(0.19f, 0.81f, 0.75f, 0.11f)))
                ))
                .build());
        } finally {
            client.close();
        }
    }

    @Test
    void runDeleteByIds() throws Exception {
        var runContext = runContextFactory.of();

        var task = Delete.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .ids(Property.ofValue(List.of(1)))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getDeletedCount(), is(1L));

        var client = milvusClient();
        try {
            var remaining = client.query(QueryReq.builder()
                .collectionName(collectionName)
                .filter("id >= 0")
                .outputFields(List.of("id"))
                .build());
            assertThat(remaining.getQueryResults().size(), is(1));
        } finally {
            client.close();
        }
    }

    @Test
    void runDeleteByFilter() throws Exception {
        var runContext = runContextFactory.of();

        var task = Delete.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .filter(Property.ofValue("id == 2"))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getDeletedCount(), is(1L));

        var client = milvusClient();
        try {
            var remaining = client.query(QueryReq.builder()
                .collectionName(collectionName)
                .filter("id >= 0")
                .outputFields(List.of("id"))
                .build());
            assertThat(remaining.getQueryResults().size(), is(1));
        } finally {
            client.close();
        }
    }

    @Test
    void runRequiresExactlyOneOfIdsOrFilter() throws Exception {
        var runContext = runContextFactory.of();

        var task = Delete.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .build();

        assertThrows(IllegalArgumentException.class, () -> task.run(runContext));
    }

    @Test
    void runRenderedExpressions() throws Exception {
        var runContext = runContextFactory.of(Map.of(
            "inputs", Map.of(
                "milvusUri", uri(),
                "collection", collectionName,
                "filter", "id == 2"
            )
        ));

        var task = Delete.builder()
            .uri(Property.ofExpression("{{ inputs.milvusUri }}"))
            .collectionName(Property.ofExpression("{{ inputs.collection }}"))
            .filter(Property.ofExpression("{{ inputs.filter }}"))
            .build();

        var output = task.run(runContext);

        assertThat(output, notNullValue());
        assertThat(output.getDeletedCount(), is(1L));
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
