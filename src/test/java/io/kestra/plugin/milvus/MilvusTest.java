package io.kestra.plugin.milvus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContextFactory;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import jakarta.inject.Inject;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.milvus.MilvusContainer;

import java.util.Map;

@KestraTest
@Testcontainers
public abstract class MilvusTest {

    protected static final int DIMENSION = 4;

    @Container
    protected static MilvusContainer milvusContainer = new MilvusContainer("milvusdb/milvus:v2.5.27");

    @Inject
    protected RunContextFactory runContextFactory;

    protected final String collectionName = "kestra_" + getClass().getSimpleName().toLowerCase() + "_" + Long.toHexString(System.nanoTime());

    protected static String uri() {
        return milvusContainer.getEndpoint();
    }

    protected MilvusClientV2 milvusClient() {
        return new MilvusClientV2(ConnectConfig.builder().uri(uri()).build());
    }

    protected static JsonObject toJsonObject(Map<String, Object> map) {
        return new Gson().toJsonTree(map).getAsJsonObject();
    }
}
