package io.kestra.plugin.milvus;

import io.milvus.v2.common.DataType;

// Primary key types Milvus accepts.
public enum IdType {
    INT64,
    VARCHAR;

    DataType toSdk() {
        return switch (this) {
            case INT64 -> DataType.Int64;
            case VARCHAR -> DataType.VarChar;
        };
    }
}
