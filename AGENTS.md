# Kestra Milvus Plugin

## What

- Provides plugin components under `io.kestra.plugin.milvus`.
- Includes classes `CreateCollection`, `Upsert`, `Search`, `Delete`, `DropCollection`.

## Why

- What user problem does this solve? Teams building RAG pipelines or semantic search need to manage Milvus vector collections and vector data as part of their Kestra workflows, without hand-rolling SDK calls.
- Why would a team adopt this plugin in a workflow? It lets a Kestra flow create/drop collections, batch-upsert embeddings, run approximate nearest-neighbor search, and delete rows against a Milvus server or Zilliz Cloud endpoint declaratively.
- What operational/business outcome does it enable? It allows vector-database maintenance and ingestion to be orchestrated, scheduled, and observed alongside the rest of a data or AI pipeline instead of living in separate scripts.

## How

### Architecture

Single-module plugin. Source packages under `io.kestra.plugin`:

- `milvus`

Infrastructure dependencies (integration tests use Testcontainers):

- `milvusdb/milvus` (via `org.testcontainers:milvus`)

### Key Plugin Classes

- `io.kestra.plugin.milvus.AbstractMilvus`: Base class holding shared connection properties (`uri`, `token`, `username`, `password`, `databaseName`) and building the Milvus SDK client.
- `io.kestra.plugin.milvus.CreateCollection`: Creates a collection using Milvus's quick-setup schema.
- `io.kestra.plugin.milvus.Upsert`: Inserts or updates rows in batches.
- `io.kestra.plugin.milvus.Search`: Runs an approximate nearest-neighbor vector similarity search.
- `io.kestra.plugin.milvus.Delete`: Deletes rows by primary key IDs or a scalar filter expression.
- `io.kestra.plugin.milvus.DropCollection`: Permanently deletes a collection.

### Project Structure

```
plugin-milvus/
├── src/main/java/io/kestra/plugin/milvus/
├── src/test/java/io/kestra/plugin/milvus/
├── build.gradle
└── README.md
```

## Local rules

- Base the wording on the implemented packages and classes, not on template README text.

## References

- https://kestra.io/docs/plugin-developer-guide
- https://kestra.io/docs/plugin-developer-guide/contribution-guidelines
