package io.kestra.plugin.milvus;

import io.milvus.v2.common.DataType;

/**
 * Plugin-owned subset of the SDK's {@link DataType}, restricted to the two types Milvus
 * actually accepts as a primary key field (confirmed from the SDK's own CreateCollectionReq
 * quick-setup schema). Keeping this separate from the SDK enum avoids exposing the roughly
 * 20 other DataType values (FloatVector, JSON, Array, ...) that are never valid primary keys.
 */
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
