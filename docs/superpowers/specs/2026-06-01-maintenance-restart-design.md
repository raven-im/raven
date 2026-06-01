# Raven Maintenance Restart Design

## Context

Raven is a multi-module Maven project for an instant messaging system. The
default branch has not received a maintenance commit since 2020-03-15. This
work restarts maintenance with a small, reviewable baseline rather than
claiming that the project remained continuously active.

The initial audit reproduced the following issues:

- The README links to a deleted pressure-test report and advertises an old
  public demo IP address with test account credentials.
- The Flyway helper commits a local MySQL password in both configuration and
  shell invocation.
- Travis CI explicitly skips tests.
- A clean Maven build on JDK 26 does not run the inherited Lombok 1.18.6
  annotation processor.
- `GroupServiceImpl` returns `ResultCode` for two methods whose interface and
  controller contract require `Result`.
- Four service POM files include an accidental `n` after the Docker image tag
  configuration.

## Goal

Restore a credible maintenance baseline that is useful to contributors and
maintainers:

1. Describe the current project status honestly.
2. Remove unsafe or stale public setup information.
3. Make a clean build reproducible on a modern JDK.
4. Fix the known admin service result-contract regression.
5. Replace the placeholder CI configuration with real Maven verification.

## Non-Goals

This baseline does not upgrade Spring Boot, Spring Cloud, Netty, Kafka, Redis,
MySQL, or the full dependency graph. Those upgrades need separate design and
compatibility work because they can change runtime behavior across services.

This baseline also does not claim production readiness. End-to-end deployment
still depends on external infrastructure such as MySQL, Redis, Kafka, Nacos,
ZooKeeper, and FastDFS.

## Change Set

### 1. Document The Maintenance Restart

Update the README to:

- State that the project is resuming maintenance after a dormant period.
- Remove the stale demo endpoint and public test account block.
- Remove the link to the deleted pressure-test report.
- Add a module overview, local verification command, and links to maintenance
  and contribution guidance.

Add:

- `MAINTENANCE.md` with the staged modernization roadmap and support policy.
- `CONTRIBUTING.md` with a focused contribution workflow and verification
  expectations.

### 2. Remove Committed Local Database Credentials

Update the Flyway helper to read local database settings from environment
variables with development-friendly defaults that do not include a password.
Keep the existing migration history intact so existing installations do not
receive a destructive schema rewrite.

Document that the bootstrap app secret committed in the historical seed
migration must be rotated for any deployed environment.

### 3. Restore Clean Modern-JDK Compilation

Update Lombok to `1.18.46`, which adds JDK 26 support. Configure Maven
explicitly so Lombok runs as an annotation processor on JDK 23 and newer.

Fix the accidental Docker image tag suffix in the route, gateway, admin, and
file-service POM files.

### 4. Repair Group Membership Result Contracts

Change `GroupServiceImpl.joinGroup` and `GroupServiceImpl.quitGroup` to return
`Result`, matching the interface and controller boundary.

Return `Result.failure(...)` for invalid member lists and use the
`MemberNotInValidator` error code when a quit request includes a user who is
not in the group.

Add focused unit tests for these branches without requiring external
infrastructure.

### 5. Enable Real Continuous Integration

Replace the Travis placeholder with a GitHub Actions Maven workflow. Run clean
verification against Java 11 and Java 26 so the project retains its historical
runtime baseline while also validating current maintainer tooling.

## Commit Plan

Keep each concern independently reviewable:

1. `docs: define maintenance restart scope`
2. `security: remove committed local database password`
3. `build: restore clean compilation on modern JDKs`
4. `fix(admin): align group membership service results`
5. `ci: run Maven verification with GitHub Actions`

## Verification

Before completion:

- Run `mvn clean verify` locally on JDK 26.
- Confirm the new admin unit tests pass.
- Confirm no tracked Flyway helper contains a local database password.
- Inspect the final commit list and working tree.

## Follow-Up Work

After the baseline is green, track dependency modernization as separate work:

- Upgrade Spring Boot and Spring Cloud in compatible stages.
- Audit bundled Flyway command-line JARs and database drivers.
- Review committed seed credentials and define a replacement bootstrap flow.
- Add integration tests for MySQL, Redis, Kafka, Nacos, ZooKeeper, and FastDFS
  boundaries.
- Review release, security-reporting, and issue-triage processes.
