# raven-storage Refactor Design

Tracking issue: [#90 raven-storage重构](https://github.com/raven-im/raven/issues/90)
(label: `help wanted`).

## Context

`raven-storage` is currently a Maven **library** (`packaging: jar`), not a
service. It contains two Spring-managed helpers:

- `com.raven.storage.route.RouteManager` — user/gateway routing state in Redis.
- `com.raven.storage.conver.ConverManager` — conversation metadata, the
  per-conversation message timeline, read pointers, and unacked-message
  bookkeeping in Redis.

The jar is compiled into every service that needs persistence:
`raven-gateway`, `raven-admin`, and `raven-route` each declare
`com.github.raven:raven-storage` as a dependency and call the managers
in-process. Messages themselves are stored in Redis sorted sets keyed by
`msg_<converId>`, with the snowflake message id used as the score (see
`ConverManager.saveMsg2Conver` / `getHistoryMsg`).

The issue asks for two related changes:

1. **Deploy `raven-storage` as a standalone service** instead of shipping it as
   a jar that each service embeds.
2. **Store messages in a full-text search engine** so message content can be
   searched, not only paged by id. The issue explicitly marks this as
   *needs research* (代调研).

## Problems With The Current Design

- **Tight coupling.** Storage logic, the Redis schema, and serialization are
  duplicated into three deployables. A schema or key-format change forces a
  lock-step redeploy of every service.
- **No independent scaling or ownership.** Storage cannot be scaled, rate
  limited, cached, or operated separately from the gateway/route services.
- **Shared Redis blast radius.** Every service holds Redis connection pools and
  credentials and can write any key, so a bug in one service can corrupt shared
  state.
- **No content search.** Redis sorted sets only support range-by-score
  (id/time) lookups. There is no way to search message text, which the issue
  wants to enable.
- **Redis as the message system of record.** Redis is an in-memory store; using
  it as the durable message archive couples retention to memory cost and makes
  large history ranges expensive. The score is a `double`, but snowflake ids
  exceed the 53-bit mantissa, so very large ids cannot be represented exactly —
  a latent correctness risk for an unbounded archive.

## Goals

1. Turn `raven-storage` into an independently deployable service with a stable
   API, so other services talk to it over the network instead of linking it.
2. Choose a message store that supports full-text search of message content in
   addition to ordered history retrieval, and document the rationale.
3. Preserve current runtime behavior during migration. Per `MAINTENANCE.md`,
   broad architecture changes must be staged and must not silently change
   behavior.

## Non-Goals

- Rewriting `raven-route` delivery semantics or the Kafka topology.
- Changing the client wire protocol beyond what history search requires.
- A big-bang cutover. This document defines staged work, not a single PR.
- Upgrading the broader dependency stack (Spring Boot, Spring Cloud, Netty,
  Kafka clients). Those remain separate efforts.

## Part 1 — Standalone `raven-storage` Service

### Target shape

Promote `raven-storage` from a `jar` library to a Spring Boot service
(`raven-storage` joins `raven-gateway`/`raven-admin`/`raven-route` as a runnable
application with its own `main`, config, Dockerfile, and discovery
registration). It owns all persistence and exposes an API; no other service
talks to Redis or the message store directly.

### API surface

The current in-process calls map directly to remote operations. Two transport
options were considered:

| Transport | Pros | Cons |
| --- | --- | --- |
| **gRPC** | The project already uses Protocol Buffers (`message.proto`); strong typing, streaming for history pages, low overhead | New runtime dependency and infra (proto service defs, server) |
| REST/JSON | Trivial to call and debug; no new stack | Hand-written DTOs, weaker contracts, more boilerplate |

**Recommendation: gRPC.** Raven already depends on `protobuf-java`, so the
team owns proto tooling, and history paging maps naturally onto a streamed or
paged RPC. A thin REST facade can be added later for admin/debug use.

Proposed service operations (derived from `ConverManager`/`RouteManager`):

- Routing: `getServerByUid`, `bindUserRoute`, `removeUserRoute`.
- Conversations: `newSingleConver`, `newGroupConver`, member add/remove,
  dismiss, `getConversation`, `getConverListByUid`.
- Messages: `saveMessage`, `getHistory(converId, beginId, count, direction)`
  (aligned with the pagination added for #84), `getUnreadCount`,
  read-pointer updates, and the new `searchMessages` (Part 2).

### Client integration

Replace the compile-time dependency on `raven-storage` in
`raven-gateway`/`raven-admin`/`raven-route` with a generated gRPC client (a
small `raven-storage-client` module). The `ConverManager`/`RouteManager` call
sites change from local method calls to client calls; method signatures can be
kept nearly identical to minimize churn.

### Migration strategy (no behavior change)

1. **Extract interfaces.** Introduce `ConverStore`/`RouteStore` interfaces in a
   shared module; current Redis logic becomes the default implementation. No
   behavior change, fully unit-testable.
2. **Stand up the service.** Add the `raven-storage` Boot application that hosts
   those implementations behind gRPC, registered in discovery (ZooKeeper/Nacos,
   matching existing services).
3. **Introduce the client.** Add `raven-storage-client` and switch one consumer
   (`raven-route`) to it behind a feature flag, keeping the embedded path as a
   fallback for one release.
4. **Cut over remaining consumers** (`raven-gateway`, `raven-admin`), then
   remove the embedded dependency and the flag.

Each step is an independently reviewable PR with its own verification.

## Part 2 — Full-Text Search Engine For Messages (Research)

### Requirements

- Full-text search of message `content` scoped by `converId` and, for groups,
  by membership/permission.
- Ordered history retrieval by message id/time with paging in both directions
  (consistent with #84) — the engine must serve this as well, or coexist with
  the ordered store.
- Durable, horizontally scalable archive independent of Redis memory.
- Operable with the project's existing infra footprint and licensing
  constraints (Raven is GPL-3.0).

### Candidates evaluated

| Engine | Full-text | Ordered paging | Scale / ops | License | Notes |
| --- | --- | --- | --- | --- | --- |
| **Elasticsearch** | Excellent (Lucene) | Strong (sort + `search_after`) | Mature, heavy ops | SSPL/Elastic (not OSI) | Powerful but license is a concern for a GPL project |
| **OpenSearch** | Excellent (Lucene) | Strong (`search_after`) | Mature, heavy ops | Apache-2.0 | Apache-licensed Elasticsearch fork; license-clean |
| **Meilisearch** | Very good, simple | Good (sortable attrs) | Lightweight, easy ops | MIT | Great DX; fewer knobs at very large scale |
| Postgres + GIN/`tsvector` | Good (built-in FTS) | Excellent (b-tree) | Familiar; FTS less rich than Lucene | PostgreSQL | One store for ordered + search; weaker CJK tokenization |
| Redis only (today) | None | Range-by-score only | In-memory cost | — | Current state; cannot search content |

### CJK consideration

Raven's UI and issues are Chinese, so message content is largely CJK.
Tokenization matters: Lucene-based engines (OpenSearch/Elasticsearch) support
analyzers such as `ik`/`smartcn`; Meilisearch handles CJK reasonably out of the
box; Postgres FTS needs an extension (e.g. `zhparser`/`pg_jieba`) for good
Chinese segmentation. This should be validated with representative data before
committing.

### Recommendation

Adopt a **two-store model** rather than replacing the ordered store with a
search engine:

- **System of record + ordered history:** a durable primary store. If a single
  store is preferred for operational simplicity, **PostgreSQL** can serve both
  ordered paging and adequate FTS via `tsvector` + a Chinese segmentation
  extension. Redis stays as a hot cache for recent messages and read pointers.
- **Search index:** **OpenSearch** for rich full-text search. It is
  Apache-2.0 (no license friction for a GPL project), Lucene-backed, supports
  CJK analyzers, and provides `search_after` for deep, ordered pagination.

Messages are written to the system of record first, then indexed into
OpenSearch asynchronously (e.g. by a consumer on the existing Kafka message
topics, which keeps indexing off the hot send path and reuses current infra).

If the team prefers the lightest footprint and the dataset is moderate,
**Meilisearch** is a strong simpler alternative to OpenSearch for the search
index.

### Proof-of-concept before adoption

Because the issue marks this as *needs research*, the next step is a small spike
(not yet a production change):

1. Index a representative message corpus (CJK-heavy) into OpenSearch and into
   Postgres FTS.
2. Compare relevance, CJK tokenization quality, deep-pagination behavior,
   write/index latency, storage size, and operational overhead.
3. Record results and pick the search backend in a follow-up design update.

## Data Model Sketch

A message document/row keyed by message id with: `converId`, `fromUid`,
`type`, `content`, `time`, and `groupId` (nullable). Indexes/mappings:

- Ordered retrieval: composite key/index on `(converId, id)`.
- Search: analyzed `content` field plus `converId` filter; group results
  additionally filtered by the requester's membership.

Read pointers, unacked-message tracking, and routing remain in Redis (hot,
volatile state), now owned by the storage service rather than each caller.

## Risks And Mitigations

- **Latency / availability:** moving from in-process to RPC adds a hop and a new
  failure domain. Mitigate with local caching of hot reads, timeouts, and
  retries; keep the embedded fallback during the staged rollout.
- **Dual-write consistency:** record-store vs. search-index drift. Mitigate by
  indexing from the Kafka message stream (at-least-once) with idempotent upserts
  keyed by message id.
- **Migration of existing Redis data:** provide a one-off backfill job that
  reads existing `msg_<converId>` sorted sets into the new store.
- **CJK search quality:** validate analyzers in the PoC before committing.
- **Scope creep:** keep Part 1 (service extraction) and Part 2 (search) as
  separate tracks; Part 1 delivers value even if the search backend is still
  under evaluation.

## Staged Delivery Plan

1. `docs`: this design (current PR).
2. PoC report comparing OpenSearch vs. Postgres FTS (vs. Meilisearch) on CJK
   data; finalize the search backend choice.
3. Extract `ConverStore`/`RouteStore` interfaces over the existing Redis logic.
4. Add the standalone `raven-storage` Boot service exposing gRPC.
5. Add `raven-storage-client`; migrate consumers one at a time behind a flag.
6. Introduce the message system-of-record and async indexer; add
   `searchMessages`.
7. Backfill existing Redis history; remove the embedded dependency and flag.

## Verification Expectations

- Each implementation PR keeps `mvn clean verify` green and adds focused unit
  tests (storage logic is mockable without external infrastructure, as the
  existing `ConverManager`/`GatewayService` tests show).
- Integration tests against Redis, the chosen record store, and the search
  engine are tracked as part of the external-boundary testing follow-up in
  `MAINTENANCE.md`.

## Open Questions

- Single store (Postgres for both ordered + FTS) vs. two stores (record store +
  OpenSearch index)? Decide from the PoC.
- Synchronous indexing vs. Kafka-driven async indexing for search freshness.
- Retention policy for the message archive and the search index.
