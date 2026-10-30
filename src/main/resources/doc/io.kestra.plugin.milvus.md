# How to use the Milvus plugin

Use the Milvus plugin to manage vector collections, upsert embeddings, run similarity search, and delete rows in [Milvus](https://milvus.io/), an open-source vector database, or [Zilliz Cloud](https://zilliz.com/).

## Authentication

All tasks inherit common connection properties from `uri`, `token`, `username`, `password` and `databaseName`.

- `token` is the API token for authentication, typically used with Zilliz Cloud or a Milvus instance with token-based auth enabled.
- `username` / `password` are credentials for a self-hosted Milvus instance with username/password authentication enabled.

`token` and `password` are alternative authentication methods, not to be combined — set one or the other depending on how your Milvus deployment is configured.

`token` and `password` are secrets, so they should never be hardcoded in a flow. Store them using Kestra's [secrets](https://kestra.io/docs/concepts/secret) and reference them with `{{ secret('MILVUS_TOKEN') }}`, or set them once for every Milvus task in a namespace via [plugin defaults](https://kestra.io/docs/workflow-components/plugin-defaults).

- `databaseName`: The Milvus database to operate against. Defaults to the server's default database when not set.

## Tasks

- `CreateCollection`: Creates a collection with a single vector field and a primary key field, using Milvus's quick-setup schema (`dimension`, `metricType`, `primaryFieldName`, `idType`, `autoId`, `vectorFieldName`). For a custom multi-field schema, create the collection with the Milvus SDK or console first and use the other tasks against it directly.
- `Upsert`: Inserts or updates rows in configurable batch sizes (`batchSize`). Each row is a flat map with the collection's vector field, primary key field (unless auto-generated), and any scalar fields. Reads from an inline list or a Kestra internal storage URI.
- `Search`: Runs an approximate nearest-neighbor vector similarity search (`vector`, `limit`), optionally narrowed with a scalar filter expression (`filter`). Outputs results inline (`FETCH`), as a single record (`FETCH_ONE`), or to Kestra internal storage (`STORE`). Searching right after an `Upsert` or `Delete` may not reflect that write under the collection's default consistency level; set `consistencyLevel: STRONG` to always read the latest writes.
- `Delete`: Deletes rows by a list of primary key IDs (`ids`) or by a scalar filter expression (`filter`). Exactly one of the two must be set.
- `DropCollection`: Permanently deletes a collection and all of its rows. This cannot be undone.
