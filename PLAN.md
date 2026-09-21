# Read-Later Service — Java Relearning Plan

A personal read-later / link archive service, built as a vehicle for rebuilding
backend Java skills.

**Target pace:** 3–5 hrs/week over ~3 months
**Starting point:** Java 11–17 era knowledge, rusty but not gone
**Started:** 2026-09-21

---

## What we're building

POST a URL. The service fetches the page, extracts title/author/text, stores it,
and lets you tag it, search it, and mark it read. A weekly digest emails you what
you didn't get to.

Chosen over a generic CRUD app because it forces real outbound I/O, background
work, and a genuine data-modeling decision around search — none of which a
todo-list API would teach.

## Ground rules

1. **No tutorials.** Every session produces code in this repo.
2. **Commit every session**, even if incomplete. The git log is the progress log.
3. **Alex writes the code.** Claude reviews, explains, and unblocks after ~20
   minutes of being stuck. Claude does not hand over finished files.
4. **Tests from milestone 5 onward are not optional.** Retrofitting them is worse.
5. **Watch for Spring-fluency masquerading as Java-fluency.** Milestones 4 and 6
   are deliberately framework-light as a check on this.
6. **Make every new test fail on purpose before trusting it.** A green build only
   proves nothing failed — a test that never ran cannot fail. Check `Tests run: N`
   against the number of tests you expect.

## Target stack

| Concern | Choice | Why |
|---|---|---|
| Language | Java 21 LTS | Current LTS; records, pattern matching, virtual threads |
| Build | Maven | More ubiquitous in backend jobs than Gradle |
| Framework | Spring Boot 4.1.1 | Dominant in the Java backend market |
| Database | PostgreSQL (Docker) | Full-text search built in; no extra service needed |
| Migrations | Flyway | Versioned schema from day one |
| HTTP client | `java.net.http.HttpClient` | JDK-native, async, no dependency |
| HTML parsing | jsoup | De facto standard |
| Testing | JUnit 5 + AssertJ + Testcontainers + WireMock | Real DB, faked network |
| CI | GitHub Actions | Free, standard |

---

## Milestones

Each is roughly 2–3 sessions. Check items off as they land.

### 1. Toolchain + walking skeleton
Prove the whole loop works before writing any real logic.

- [x] Install Java 21 LTS (Temurin tarball to `~/Library/Java/JavaVirtualMachines`)
- [x] Install Maven (3.9.16, tarball to `~/.local`)
- [x] `git init`, `.gitignore` for Java/Maven/IDE
- [x] Spring Boot project via start.spring.io (web, validation)
- [x] One endpoint (`GET /health`) returning a hardcoded response
- [x] One test asserting it returns 200 (`@WebMvcTest` slice, not `@SpringBootTest`)
- [x] `mvn test` green, committed

**Relearning focus:** Maven's project layout and lifecycle; how a Boot app boots.

### 2. Domain model + persistence
- [ ] Docker Compose with Postgres
- [ ] Flyway migration creating `article` and `tag` tables
- [ ] JPA entities, repositories via Spring Data
- [ ] Save and load one article from a test

**Relearning focus — the big Java 11→21 delta.** Deliberately use:
- `record` for DTOs and value types
- `var` for local inference
- Text blocks (`"""`) for any SQL or JSON literals
- Pattern-matching `switch` and `instanceof`
- `sealed` interfaces where a closed set of types exists

### 3. CRUD API, validation, error handling
- [ ] `POST /articles`, `GET /articles/{id}`, `GET /articles`, `DELETE`
- [ ] Bean Validation on request bodies
- [ ] `@RestControllerAdvice` global exception handler
- [ ] Correct status codes: 201 with Location, 404, 400, 409
- [ ] No stack traces in responses

**Relearning focus:** Layer separation — entity vs. DTO, and why leaking JPA
entities out of the controller causes pain later.

### 4. The fetcher (framework-light)
- [ ] Fetch a URL with `HttpClient` asynchronously
- [ ] Extract title, author, main text with jsoup
- [ ] Connect and read timeouts
- [ ] Retry with backoff on transient failure
- [ ] Store partial results when extraction partly fails
- [ ] Article gets a status: PENDING / FETCHED / FAILED

**Relearning focus:** `CompletableFuture` composition, checked vs. unchecked
exception design, and modeling failure as data rather than as exceptions.

### 5. Testing for real
This milestone pays for itself across every one after it.

- [ ] JUnit 5 + AssertJ conventions established
- [ ] Testcontainers spinning a real Postgres for repository tests
- [ ] WireMock faking remote pages — including timeouts and 500s
- [ ] `@SpringBootTest` slice tests vs. full context; know when to use which
- [ ] Backfill tests for milestones 2–4

**Relearning focus:** What's worth testing. Aim for confidence, not coverage.

### 6. Search + pagination (framework-light)
- [ ] Postgres `tsvector` column, populated on write
- [ ] GIN index; verify with `EXPLAIN ANALYZE`
- [ ] `GET /articles?q=...` with ranked results
- [ ] `Pageable` with sane defaults and a max page size
- [ ] Filter by tag and read/unread

**Relearning focus:** Native queries when JPA gets in the way; knowing when to
drop to SQL.

### 7. Authentication
The step people skip. Don't.

- [ ] Spring Security on the dependency path
- [ ] Single-user API token, or OAuth2 if feeling ambitious
- [ ] All endpoints secured except `/health`
- [ ] Secrets from environment, never committed
- [ ] Tests covering both authorized and unauthorized paths

**Relearning focus:** The filter chain — where requests actually get intercepted.

### 8. Scheduled digest
- [ ] `@Scheduled` weekly job collecting unread articles
- [ ] Render an email (start by logging it; wire real SMTP after)
- [ ] Idempotency: a restart or double-fire must not double-send
- [ ] Test the job without waiting a week — inject a clock

**Relearning focus:** `java.time` done properly, and why `Clock` should be a
dependency rather than a static call.

### 9. Ship it
- [ ] Multi-stage Dockerfile
- [ ] GitHub Actions running `mvn verify` on push
- [ ] Deployed somewhere cheap (Fly.io, Railway, a small VPS)
- [ ] README explaining what it is and how to run it

**Relearning focus:** JVM in a container — heap sizing, and why the defaults used
to be wrong.

---

## Stretch goals

Only after milestone 9, and only if it's still fun:

- **Virtual threads** (Java 21's headline feature) — swap the fetcher's async
  composition for blocking calls on virtual threads and compare. Directly
  relevant to a fetch-heavy service.
- **Observability** — Micrometer metrics, structured logging, a Grafana dashboard.
- **A front end** — HTMX for minimum friction, React if the goal includes it.
- **Caching** — Caffeine or Redis, with a real eviction policy.

---

## Toolchain notes

- **SDKMAN was not used.** Its installer requires bash 4+; macOS ships bash 3.2.
  Homebrew also failed — it needs standalone Xcode Command Line Tools, which
  aren't installed (Xcode.app alone doesn't satisfy it). Both tools were
  installed from verified tarballs instead. If you want `brew` working later,
  run `xcode-select --install` — it's an interactive GUI dialog.
- **Switching JDKs:** `jdk 21` / `jdk 15` (shell function added to `~/.zshrc`,
  which also exports `JAVA_HOME` and puts Maven on `PATH`). Backup at
  `~/.zshrc.bak.20260921`.
- **Spring Boot 4, not 3.** Initializr no longer offers 3.x. Note the renamed
  starters: `spring-boot-starter-webmvc` (not `-web`), and test starters split
  into `-webmvc-test` / `-validation-test`. Most tutorials you find will be
  written against Boot 3 — expect small deltas.
- Initializr reports versions as `4.1.1.RELEASE`; Maven Central publishes plain
  `4.1.1`. The generated pom needed that corrected by hand.
- **Boot 4 moved the test annotations.** `@WebMvcTest` and `@AutoConfigureMockMvc`
  are now in `org.springframework.boot.webmvc.test.autoconfigure`, not
  `org.springframework.boot.test.autoconfigure.web.servlet` as every Boot 3
  tutorial will tell you. When an import won't resolve, search the jar before
  assuming the code is wrong.

## Known risks

- **Spring's magic hides Java.** Mitigated by the framework-light milestones; if
  you can't explain what an annotation does, stop and find out.
- **Milestone 5 is skippable-feeling and shouldn't be.** Everything after it is
  harder without it.
- **Scope creep toward features over fundamentals.** New feature ideas go in
  IDEAS.md, not into the current milestone.
- **Gaps between sessions.** Each session ends with a commit and a one-line note
  in the log below saying what's next.

---

## Session log

| Date | Milestone | What happened | Next |
|---|---|---|---|
| 2026-09-21 | — | Plan written | Start milestone 1: install Java 21 + Maven |
| 2026-09-21 | 1 | Toolchain installed (Java 21 Temurin, Maven 3.9.16), Boot 4.1.1 scaffold generated, `mvn test` green, repo initialised | Alex writes `GET /health` + its test |
| 2026-09-21 | 1 ✅ | `HealthController` + `HealthControllerTests` done. Reworked from `@SpringBootTest` + manual `MockMvcBuilders` to a `@WebMvcTest` slice; caught a `private` `@Test` that was silently skipped while the build stayed green | Milestone 2: Postgres in Docker, Flyway, JPA entities |
