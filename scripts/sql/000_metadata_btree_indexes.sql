-- Speeds up the very common "name AND value" metadata filter, e.g. a search for
-- metaData.name:'ion mode' and metaData.value:'positive'.
-- InChI rows are skipped because those values are long enough to bloat the index
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_name_value_idx
    ON metadata (name, value)
    WHERE name != 'InChI';

-- Speeds up compound classification searches, which look for a value among the relatively
-- few metadata rows that belong to a classification
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_classification_value_idx
    ON metadata (value)
    WHERE compound_classification_id IS NOT NULL;

-- Refresh the planner's statistics so it starts using the new indexes right away
ANALYZE metadata;
