-- Trigram indexes for substring search.
-- Terms shorter than 3 characters cannot use a trigram index, so the API rejects them

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Session only setting that makes the builds below considerably faster.
-- It disappears when the psql session ends, so there is nothing to undo
SET maintenance_work_mem = '2GB';

-- Both of these back the quick search box (GET /rest/spectra/keyword), which looks for the
-- term in metadata values and compound names.
-- metadata is by far the largest table, expect minutes to tens of minutes on production
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_value_trgm_idx
    ON metadata USING gin (value gin_trgm_ops);

CREATE INDEX CONCURRENTLY IF NOT EXISTS name_name_trgm_idx
    ON name USING gin (name gin_trgm_ops);

-- Partial SPLASH searches do not use the quick search endpoint. They go through the normal
-- search API as splash.splash~'*term*', which is a single table predicate, so the planner can
-- still use this index
CREATE INDEX CONCURRENTLY IF NOT EXISTS splash_splash_trgm_idx
    ON splash USING gin (splash gin_trgm_ops);

-- Refresh the planner's statistics so it starts using the new indexes right away
ANALYZE metadata;
ANALYZE name;
ANALYZE splash;
