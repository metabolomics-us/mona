create or replace view tags as

select
        monaId as mona_id,
        ((tag ->> 'text'):: text) as text,
        ((tag -> 'ruleBased'):: bool) as rule_based
from (
    select
        a.monaId,
        jsonb_array_elements(tags) as tag
      from (
        select
            content ->> 'id' as monaId,
            content -> 'tags' as tags
            from "spectrum_result" sr
            ) a
      ) b;


create or replace view score as

select
        monaId as mona_id,
        ((score -> 'score'):: float) as score,
        score -> 'impacts' as impacts
from (
    select
        content ->> 'id'    as monaId,
        content -> 'score' as score
    from "spectrum_result" sr) a;


create or replace view splash as

select
    monaId as mona_id,
    splash ->> 'block1' as block1,
    splash ->> 'block2' as block2,
    splash ->> 'block3' as block3,
    splash ->> 'splash' as splash
from (
    select
        content ->> 'id' as monaId,
        content -> 'splash' as splash
    from "spectrum_result" sr
    ) a;


create or replace view compound as

select
    monaId as mona_id,
    compound ->> 'kind' as kind,
    compound -> 'tags' as tags,
    compound ->> 'inchi' as inchi,
    compound -> 'names' as names,
    compound ->> 'molFile' as mol_file,
    (compound ->> 'computed'):: bool as computed,
    compound ->> 'inchiKey' as inchikey,
    compound -> 'metaData' as metadata,
    compound -> 'classification' as classification
from (select
            content ->> 'id'                             as monaId,
            jsonb_array_elements(content -> 'compound') as compound
      from "spectrum_result" sr
      ) a;

create or replace view compound_submitter as

select monaId as mona_id,
       submitter ->> 'id' as submitter_id,
       submitter ->> 'lastName' as last_name,
       submitter ->> 'firstName' as first_name,
       submitter ->> 'institution' as institution,
       submitter ->> 'emailAddress' as email_address
from
    (select content ->> 'id' as monaId,
           content -> 'submitter' as submitter
    from "spectrum_result" sr) a;


create or replace view library as

select monaId as mona_id,
       library ->> 'description' as description,
       library ->> 'link' as link,
       library ->> 'library' as library,
       library -> 'tag' as tag
from
(select content ->> 'id' as monaId,
       content -> 'library' as library
from "spectrum_result" sr) a;


create or replace view metadata as

select
    monaId as mona_id,
    ((jsonb_array_elements(metadata) ->> 'name')) as name,
    ((jsonb_array_elements(metadata) ->> 'unit')) as unit,
    ((jsonb_array_elements(metadata) ->> 'value')) as value,
    ((jsonb_array_elements(metadata) ->> 'hidden'):: bool) as hidden,
    ((jsonb_array_elements(metadata) ->> 'category')) as category,
    ((jsonb_array_elements(metadata) ->> 'computed'):: bool) as computed
from
(select content ->> 'id' as monaId,
       content -> 'metaData' as metadata
from "spectrum_result" sr) a
UNION
select
    monaId as mona_id,
    ((jsonb_array_elements(compound_metadata) ->> 'name')) as name,
    ((jsonb_array_elements(compound_metadata) ->> 'unit')) as unit,
    ((jsonb_array_elements(compound_metadata) ->> 'value')) as value,
    ((jsonb_array_elements(compound_metadata) ->> 'hidden'):: bool) as hidden,
    ((jsonb_array_elements(compound_metadata) ->> 'category')) as category,
    ((jsonb_array_elements(compound_metadata) ->> 'computed'):: bool) as computed
from
    (select content ->> 'id' as monaId,
            jsonb_array_elements(content -> 'compound') -> 'metaData' as compound_metadata
     from "spectrum_result" sr) b
UNION
select
    monaId as mona_id,
    ((jsonb_array_elements(compound_classification) ->> 'name')) as name,
    null as unit,
    ((jsonb_array_elements(compound_classification) ->> 'value')) as value,
    ((jsonb_array_elements(compound_classification) ->> 'hidden'):: bool) as hidden,
    ((jsonb_array_elements(compound_classification) ->> 'category')) as category,
    ((jsonb_array_elements(compound_classification) ->> 'computed'):: bool) as computed
from
    (select content ->> 'id' as monaId,
            jsonb_array_elements(content -> 'compound') -> 'classification' as compound_classification
     from "spectrum_result" sr) c
UNION
select
    monaId as mona_id,
    'compound_name' as name,
    null as unit,
    ((jsonb_array_elements(names) ->> 'name')) as value,
    false as hidden,
    null as category,
    ((jsonb_array_elements(names) ->> 'computed'):: bool) as name
from
    (select content ->> 'id' as monaId,
            jsonb_array_elements(content -> 'compound') -> 'names' as names
     from "spectrum_result" sr) d
UNION
select
    monaId as mona_id,
    ((annotation ->> 'name')) as name,
    null as unit,
    ((annotation ->> 'value')) as value,
    ((annotation ->> 'hidden'):: bool) as hidden,
    ((annotation ->> 'category')) as category,
    ((annotation ->> 'computed'):: bool) as computed
from
    (select content ->> 'id' as monaId,
            jsonb_array_elements(content -> 'annotations') as annotation
     from "spectrum_result" sr) e;

create or replace view search_table as

select sr.mona_id,
       sr.content,
       m.name as metadata_name,
       m.unit as metadata_unit,
       m.value as metadata_value,
       m.hidden as metadata_hidden,
       m.category as metadata_category,
       m.computed as metadata_computed,
       c.kind as compound_kind,
       c.mol_file as compound_mol_file,
       c.computed as compound_computed,
       c.inchi as inchi,
       c.inchikey as inchikey,
       cs.submitter_id,
       cs.last_name,
       cs.first_name,
       cs.institution,
       cs.email_address,
       s.block1,
       s.block2,
       s.block3,
       s.splash,
       t.text,
       t.rule_based
from spectrum_result sr
         INNER JOIN metadata m on sr.mona_id = m.mona_id
         INNER JOIN compound c on sr.mona_id = c.mona_id
         INNER JOIN compound_submitter cs on sr.mona_id = cs.mona_id
         INNER JOIN splash s on sr.mona_id = s.mona_id
         INNER JOIN tags t on sr.mona_id = t.mona_id;

create materialized view if not exists search_table_mat as
select
    *
from
    search_table;



