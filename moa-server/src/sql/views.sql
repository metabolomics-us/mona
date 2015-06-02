-- overview of the score of a spectra by submitter--
create or replace view spectra_scores_by_submitter as (
    select count(*),avg(relative_score) as relative_score,avg(scaled_score) as scaled_score,avg(score) as score,submitter_id from spectrum a, score b where a.score_id = b.id group by submitter_id order by relative_score desc
)