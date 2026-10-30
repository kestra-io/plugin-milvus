package io.kestra.plugin.milvus;

import io.kestra.core.models.property.Property;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CreateCollectionTest extends MilvusTest {

    @Test
    void run() throws Exception {
        var runContext = runContextFactory.of();

        var task = CreateCollection.builder()
            .uri(Property.ofValue(uri()))
            .collectionName(Property.ofValue(collectionName))
            .dimension(Property.ofValue(DIMENSION))
            .metricType(Property.ofValue(MetricType.COSINE))
            .build();

        var output = task.run(runContext);

        assertThat(output, is((Object) null));

        var client = milvusClient();
        try {
            boolean exists = client.hasCollection(HasCollectionReq.builder().collectionName(collectionName).build());
            assertThat(exists, is(true));
        } finally {
            client.close();
        }
    }

    @Test
    void runRenderedExpressions() throws Exception {
        var runContext = runContextFactory.of(Map.of(
            "inputs", Map.of(
                "milvusUri", uri(),
                "collection", collectionName
            )
        ));

        var task = CreateCollection.builder()
            .uri(Property.ofExpression("{{ inputs.milvusUri }}"))
            .collectionName(Property.ofExpression("{{ inputs.collection }}"))
            .dimension(Property.ofValue(DIMENSION))
            .metricType(Property.ofValue(MetricType.COSINE))
            .build();

        var output = task.run(runContext);

        assertThat(output, is((Object) null));

        var client = milvusClient();
        try {
            boolean exists = client.hasCollection(HasCollectionReq.builder().collectionName(collectionName).build());
            assertThat(exists, is(true));
        } finally {
            client.close();
        }
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
