# Raven Maintenance Restart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore an honest, secure, reproducible maintenance baseline for the dormant Raven repository and publish it as a reviewable pull request.

**Architecture:** Keep the existing Spring Boot and Spring Cloud application architecture intact. Make narrowly scoped changes at the repository boundary: contributor documentation, local Flyway tooling, parent Maven build configuration, one admin-service contract fix with regression tests, and GitHub Actions verification.

**Tech Stack:** Java 8 source compatibility, Maven, Spring Boot 2.1, Lombok, JUnit 4, Mockito, Flyway command-line tooling, GitHub Actions

---

### Task 1: Publish The Maintenance Restart Scope

**Files:**
- Modify: `README.md`
- Create: `MAINTENANCE.md`
- Create: `CONTRIBUTING.md`
- Create: `docs/superpowers/plans/2026-06-01-maintenance-restart.md`

- [ ] **Step 1: Rewrite the README status and contributor entry points**

State that maintenance is resuming after a dormant period. Remove the stale
demo endpoint, public test accounts, and deleted pressure-test link. Add module
overview, infrastructure prerequisites, `mvn clean verify`, and links to
`MAINTENANCE.md` and `CONTRIBUTING.md`.

- [ ] **Step 2: Add maintenance and contribution guidance**

Document the baseline scope, known infrastructure requirements, staged
dependency modernization, historical seed-secret rotation requirement, issue
triage expectations, and pull-request verification command.

- [ ] **Step 3: Verify documentation cleanup**

Run:

```bash
rg -n '114\.67\.79\.183|13800222222|13800333333|2019\.6\.2-record' README.md MAINTENANCE.md CONTRIBUTING.md
```

Expected: no matches.

- [ ] **Step 4: Commit**

```bash
git add README.md MAINTENANCE.md CONTRIBUTING.md docs/superpowers/plans/2026-06-01-maintenance-restart.md
git commit -m "docs: define maintenance restart scope"
```

### Task 2: Remove Committed Local Database Passwords

**Files:**
- Modify: `flyway/conf/flyway.conf`
- Modify: `flyway/migrate.sh`
- Modify: `flyway/flyway`

- [ ] **Step 1: Make local migration settings environment-driven**

Remove `flyway.password = 123456`. Update `migrate.sh` to read:

```bash
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-imdb}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
```

Build MySQL and Flyway argument arrays so a password argument is passed only
when `MYSQL_PASSWORD` is non-empty. Resolve the Flyway wrapper relative to the
script path so the helper works outside the `flyway/` directory.

- [ ] **Step 2: Remove the invalid wrapper character**

Delete the standalone non-ASCII character between Flyway wrapper Java
selection and classpath setup.

- [ ] **Step 3: Verify shell syntax and password removal**

Run:

```bash
bash -n flyway/migrate.sh flyway/flyway
rg -n '123456|-p123456|flyway\.password\s*=' flyway/conf/flyway.conf flyway/migrate.sh
```

Expected: shell syntax passes and the search returns no matches.

- [ ] **Step 4: Commit**

```bash
git add flyway/conf/flyway.conf flyway/migrate.sh flyway/flyway
git commit -m "security: remove committed local database password"
```

### Task 3: Restore Clean Compilation On Modern JDKs

**Files:**
- Modify: `pom.xml`
- Modify: `raven-admin/pom.xml`
- Modify: `raven-file/pom.xml`
- Modify: `raven-gateway/pom.xml`
- Modify: `raven-route/pom.xml`

- [ ] **Step 1: Configure modern Lombok annotation processing**

Add:

```xml
<lombok.version>1.18.46</lombok.version>
```

Configure `maven-compiler-plugin` in the parent build:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
</annotationProcessorPaths>
```

- [ ] **Step 2: Fix Docker image tag configuration**

Replace each accidental `<tag>latest</tag>n` with `<tag>latest</tag>` in the
route, gateway, admin, and file-service POM files.

- [ ] **Step 3: Verify common-module clean compilation**

Run:

```bash
mvn clean test -pl raven-common -am
```

Expected: four existing common-module tests pass on JDK 26.

- [ ] **Step 4: Confirm the remaining admin contract failure**

Run:

```bash
mvn clean test
```

Expected: compilation reaches `raven-admin` and fails because
`GroupServiceImpl.joinGroup` and `GroupServiceImpl.quitGroup` return
`ResultCode` while the interface requires `Result`.

- [ ] **Step 5: Commit**

```bash
git add pom.xml raven-admin/pom.xml raven-file/pom.xml raven-gateway/pom.xml raven-route/pom.xml
git commit -m "build: restore clean compilation on modern JDKs"
```

### Task 4: Repair Group Membership Result Contracts

**Files:**
- Modify: `raven-admin/src/main/java/com/raven/admin/group/service/impl/GroupServiceImpl.java`
- Create: `raven-admin/src/test/java/com/raven/admin/group/service/impl/GroupServiceImplTest.java`

- [ ] **Step 1: Write the failing empty-member-list regression test**

Create:

```java
@Test
public void joinGroupRejectsEmptyMemberList() {
    GroupReqParam param = new GroupReqParam();
    param.setMembers(Collections.emptyList());

    Result result = service.joinGroup(param);

    assertEquals(ResultCode.COMMON_INVALID_PARAMETER.getCode(), result.getCode().intValue());
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
mvn -pl raven-admin -am test
```

Expected: compilation fails because the implementation still returns
`ResultCode`.

- [ ] **Step 3: Implement the minimal result-contract fix**

Change both method signatures to `Result` and replace the empty-list returns
with:

```java
return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
```

- [ ] **Step 4: Write the failing missing-member error-code regression test**

Add:

```java
@Test
public void quitGroupReportsMissingMember() {
    GroupReqParam param = new GroupReqParam();
    param.setGroupId("group-id");
    param.setMembers(Collections.singletonList("missing-member"));
    when(groupValidator.isValid("group-id")).thenReturn(true);
    when(memberNotValidator.isValid("group-id", param.getMembers())).thenReturn(false);

    Result result = service.quitGroup(param);

    assertEquals(ResultCode.GROUP_ERROR_MEMBER_NOT_IN.getCode(), result.getCode().intValue());
}
```

- [ ] **Step 5: Run the test to verify the error-code assertion fails**

Run:

```bash
mvn -pl raven-admin -am test
```

Expected: `quitGroupReportsMissingMember` fails because the implementation
returns the group validator error code.

- [ ] **Step 6: Use the member validator error code**

Replace:

```java
return Result.failure(groupValidator.errorCode());
```

inside the failed `memberNotValidator` branch with:

```java
return Result.failure(memberNotValidator.errorCode());
```

- [ ] **Step 7: Verify admin and reactor tests**

Run:

```bash
mvn clean test
```

Expected: all reactor modules compile and all tests pass.

- [ ] **Step 8: Commit**

```bash
git add raven-admin/src/main/java/com/raven/admin/group/service/impl/GroupServiceImpl.java raven-admin/src/test/java/com/raven/admin/group/service/impl/GroupServiceImplTest.java
git commit -m "fix(admin): align group membership service results"
```

### Task 5: Enable GitHub Actions Verification

**Files:**
- Delete: `.travis.yml`
- Create: `.github/workflows/maven.yml`

- [ ] **Step 1: Replace placeholder Travis configuration**

Create a GitHub Actions workflow using `actions/checkout@v6` and
`actions/setup-java@v5`. Grant `contents: read`, enable Maven caching, and run
`mvn --batch-mode clean verify` for Java 11 and Java 26.

- [ ] **Step 2: Verify local Maven build**

Run:

```bash
mvn clean verify
```

Expected: all reactor modules compile, package, and pass tests on local JDK 26.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/maven.yml .travis.yml
git commit -m "ci: run Maven verification with GitHub Actions"
```

### Task 6: Publish The Pull Request

**Files:**
- Inspect: all files changed since `origin/master`

- [ ] **Step 1: Inspect the final branch**

Run:

```bash
git diff --check origin/master...HEAD
git status --short --branch
git log --oneline origin/master..HEAD
```

Expected: no whitespace errors, clean working tree, and reviewable commits.

- [ ] **Step 2: Push the maintenance branch**

Run:

```bash
git push -u origin codex/maintenance-restart
```

- [ ] **Step 3: Open a draft pull request into master**

Create a draft PR titled:

```text
[codex] restore Raven maintenance baseline
```

The PR body must summarize the dormant-project restart, stale documentation
cleanup, Flyway password removal, JDK 26 compilation work, admin regression
fix, GitHub Actions workflow, and local verification results.
