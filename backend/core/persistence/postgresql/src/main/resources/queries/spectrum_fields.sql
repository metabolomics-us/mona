create or replace view spectrum_submitter_statistics as

select a.id as id,
       s.last_name as last_name,
       s.first_name as first_name,
       s.institution as institution,
       s.email_address as email_address,
       coalesce (s2.score,0):: float as score
from
    "spectrum" a
        inner JOIN spectrum_submitters s on a.spectrum_submitter_id = s.id
        left JOIN score s2 on s2.id = a.spectrum_score_id;

CREATE INDEX CONCURRENTLY IF NOT EXISTS compound_classification_index ON metadata (compound_classification_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS compound_metadata_index ON metadata (compound_metadata_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_annotation_index ON metadata (spectrum_annotation_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_metadata_index ON metadata (spectrum_metadata_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS compound_spectrum_index ON compound (spectrum_compound_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS impacts_score_index ON impacts (score_impacts_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS name_compound_index ON name (compound_names_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS tags_compound_tags_id_idx ON tags (compound_tags_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_score_index ON spectrum (spectrum_score_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_library_index ON spectrum (spectrum_library_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_splash_index ON spectrum (spectrum_splash_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS spectrum_submitter_index ON spectrum (spectrum_submitter_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_value_count_index on metadata_value_count (statistics_metadata_id, statistics_metadata_name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS tag_spectrum_index on tags (spectrum_tags_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS tag_text_index on tags (text);
CREATE INDEX CONCURRENTLY IF NOT EXISTS library_tag_index on library (library_tag_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS library_name_index on library (library);
CREATE INDEX CONCURRENTLY IF NOT EXISTS statistics_metadata_name_index on statistics_metadata(name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_name_index ON metadata(name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS metadata_value_index ON metadata(value);
create index concurrently if not exists spectrum_submitter_email_address_index on spectrum_submitters (email_address);