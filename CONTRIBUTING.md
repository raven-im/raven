# Contributing

Raven is restarting maintenance after a dormant period. Small, reviewable
changes with clear verification are the most useful contributions during this
phase.

## Before Starting

Open an issue for changes that affect service boundaries, dependency versions,
database migrations, or deployment behavior. Describe the problem, expected
behavior, and affected modules.

Do not include passwords, tokens, private endpoints, or exploit details in
public issues. For a potential vulnerability, open an issue requesting a
private reporting channel without disclosing the sensitive details.

## Development Workflow

1. Create a focused branch.
2. Keep each commit limited to one maintenance concern.
3. Add or update tests when behavior changes.
4. Run the Maven verification build:

```bash
mvn clean verify
```

5. Include the verification result and any remaining limitations in the pull
   request description.

## Pull Requests

Pull requests should explain:

- What changed
- Why the change is needed
- Which modules are affected
- How the change was verified
- Whether runtime configuration or deployment steps changed

Large dependency upgrades should be split into compatible stages and discussed
in an issue before implementation.
