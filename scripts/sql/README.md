# Database Index Scripts

Hibernate (`ddl-auto: update`) manages tables, columns and foreign key constraints, but never
indexes, and Postgres does not index foreign key columns on its own. Without indexes every join
and cascade delete degrades to a sequential scan.

MoNA DB Indexes come from two places:

- **Automatic:** `persistence-server` runs `core/persistence/postgresql/.../queries/spectrum_fields.sql`
  on every startup (profile `mona.persistence.init`, `spring.sql.init.mode: always`). It creates the
  foreign key indexes, `metadata_name_idx` and a few small lookup indexes. Nothing to do.
- **Manual:** the scripts here. Not wired into the app, so each database (local, dev, prod) needs
  them run against it by hand. This directory is the source of truth for that set.

| Script | Contents |
|---|---|
| `000_metadata_btree_indexes.sql` | Partial b-trees on `metadata`: `(name, value)` for combined name/value filters, `(value)` for classification lookups |
| `001_trgm_search_indexes.sql` | `pg_trgm` plus GIN trigram indexes for the quick search box (`/rest/spectra/keyword`) and partial SPLASH searches |
| `002_extended_statistics.sql` | Multi column statistics on `metadata(name, value)`, fixing bad nested loop plans |

## Applying

Every statement is idempotent (`IF NOT EXISTS`), so re-running is always safe. Order does not
matter. psql runs statements in autocommit, which is what `CONCURRENTLY` requires, so do **not**
wrap these in `BEGIN`/`COMMIT`.

```bash
docker exec -i <postgres-container> psql -U $MONA_USER -d mona -v ON_ERROR_STOP=1 < scripts/sql/001_trgm_search_indexes.sql
```

On production, copy to gose and run it inside the postgres container under tmux or
`docker exec -d` so a dropped SSH connection cannot kill the build. Watch progress from another
session with `SELECT pid, phase, blocks_done, blocks_total FROM pg_stat_progress_create_index;`.

Only `001` is slow; the GIN index on `metadata.value` takes minutes to tens of minutes.
`CONCURRENTLY` does *not* block reads or writes. `002` should finish in seconds at any table size.

## Verifying

```sql
SELECT indexname FROM pg_indexes WHERE indexname IN ('metadata_name_value_idx',
  'metadata_classification_value_idx', 'metadata_value_trgm_idx', 'name_name_trgm_idx',
  'splash_splash_trgm_idx');                              -- expect all 5
SELECT stxname FROM pg_statistic_ext;                     -- expect metadata_name_value_stats
SELECT indexrelid::regclass FROM pg_index WHERE NOT indisvalid;   -- expect nothing
```

The last query matters most: a failed `CONCURRENTLY` build leaves an INVALID index that is never
used for queries but still slows every write, and since `IF NOT EXISTS` matches by *name only*, a
re-run silently skips it. `DROP INDEX` anything reported, then re-run.
