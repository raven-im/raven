# Maintenance

## Current Status

Raven resumed maintenance in June 2026 after a dormant period. The first
maintenance milestone is a reviewable baseline: accurate public documentation,
safer local tooling, a reproducible Maven build, focused regression tests, and
real continuous integration.

The project is not currently presented as production-ready. Existing
deployments should review configuration and rotate credentials before running
the services.

## Runtime Dependencies

The full service topology depends on:

- MySQL for application and group records
- Redis for routing and conversation state
- Kafka for message delivery
- Nacos for configuration
- ZooKeeper for service discovery
- FastDFS for file uploads

Unit and build verification should remain independent of those external
services. Integration coverage for these boundaries is follow-up work.

## Security Notes

The historical migration `flyway/sql/V5__insert_original_app_config.sql`
contains a bootstrap app secret. It is public repository history, not a
production credential. Rotate it in every deployed environment.

The local Flyway helper reads database settings from environment variables:

| Variable | Default |
| --- | --- |
| `MYSQL_HOST` | `127.0.0.1` |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DATABASE` | `imdb` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | empty |

Do not commit deployment credentials, local passwords, or generated secrets.

## Modernization Roadmap

### Baseline

- Remove stale public demo information.
- Remove committed local database passwords.
- Restore clean compilation on modern JDKs.
- Repair known result-contract regressions.
- Run Maven verification in GitHub Actions.

### Next

- Upgrade Spring Boot and Spring Cloud in compatible stages.
- Audit bundled Flyway command-line JARs and JDBC drivers.
- Replace the historical bootstrap-secret flow.
- Add integration tests for external service boundaries.
- Define release, security-reporting, and issue-triage processes.

## Support Policy

Until a compatibility matrix is published, changes should preserve existing
runtime behavior unless a pull request clearly documents a migration. Open an
issue before starting broad dependency or architecture changes.
