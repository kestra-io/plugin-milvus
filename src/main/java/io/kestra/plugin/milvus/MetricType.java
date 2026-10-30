package io.kestra.plugin.milvus;

import io.milvus.v2.common.IndexParam;

/**
 * Plugin-owned subset of the SDK's {@link IndexParam.MetricType}, restricted to the metrics
 * valid for float vectors (the only vector kind this plugin's quick-setup schema supports).
 * Keeping this separate from the SDK enum avoids exposing binary-vector-only metrics
 * (HAMMING, JACCARD, BM25, ...) in the UI, and insulates flows from a future SDK rename.
 */
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
