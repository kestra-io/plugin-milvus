package io.kestra.plugin.milvus;

// Consistency levels valid for a search.
public enum ConsistencyLevel {
    STRONG,
    SESSION,
    BOUNDED,
    EVENTUALLY;

    io.milvus.v2.common.ConsistencyLevel toSdk() {
        return switch (this) {
            case STRONG -> io.milvus.v2.common.ConsistencyLevel.STRONG;
            case SESSION -> io.milvus.v2.common.ConsistencyLevel.SESSION;
            case BOUNDED -> io.milvus.v2.common.ConsistencyLevel.BOUNDED;
            case EVENTUALLY -> io.milvus.v2.common.ConsistencyLevel.EVENTUALLY;
        };
    }
}
