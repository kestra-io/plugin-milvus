package io.kestra.plugin.milvus;

import io.milvus.v2.common.IndexParam;

// Similarity metrics valid for float vectors.
public enum MetricType {
    COSINE,
    L2,
    IP;

    IndexParam.MetricType toSdk() {
        return switch (this) {
            case COSINE -> IndexParam.MetricType.COSINE;
            case L2 -> IndexParam.MetricType.L2;
            case IP -> IndexParam.MetricType.IP;
        };
    }
}
