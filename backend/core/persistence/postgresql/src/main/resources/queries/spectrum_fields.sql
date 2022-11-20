create or replace view spectrum_submitter_statistics as

select a.id as id,
       s.last_name as last_name,
       s.first_name as first_name,
       s.institution as institution,
       s.email_address as email_address,
       s2.score:: float as score
from
    "spectrum" a
INNER JOIN spectrum_submitters s on a.submitter_id = s.id
INNER JOIN score s2 on s2.id = a.score_id;

CREATE INDEX IF NOT EXISTS compound_classification_index ON metadata (compound_classification_id);
CREATE INDEX IF NOT EXISTS compound_metadata_index ON metadata (compound_metadata_id);
CREATE INDEX IF NOT EXISTS spectrum_annotation_index ON metadata (spectrum_annotation_id);
CREATE INDEX IF NOT EXISTS spectrum_metadata_index ON metadata (spectrum_metadata_id);
CREATE INDEX IF NOT EXISTS compound_spectrum_index ON compound (spectrum_id);
CREATE INDEX IF NOT EXISTS impacts_score_index ON impacts (score_id);
CREATE INDEX IF NOT EXISTS name_compound_index ON name (compound_id);
CREATE INDEX IF NOT EXISTS metadata_value_index ON metadata_value_count (statistics_metadata_id, statistics_metadata_name);


