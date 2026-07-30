-- Teaches Postgres that metadata.name and metadata.value are correlated.

CREATE STATISTICS IF NOT EXISTS metadata_name_value_stats (dependencies, ndistinct)
    ON name, value FROM metadata;

-- Populate the new statistics now rather than waiting for the next autovacuum
ANALYZE metadata;
