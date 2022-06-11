create or replace view parsed_tags as

select
        monaId,
        ((tag ->> 'text'):: text) as text,
        ((tag -> 'ruleBased'):: bool) as ruleBased
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


create or replace view parsed_scores as

select
        monaId,
        ((score -> 'score'):: float) as score,
        score -> 'impacts' as impacts
from (
    select
        content ->> 'id'    as monaId,
        content -> 'score' as score
    from "spectrum_result" sr) a;


create or replace view parsed_splash as

select
    monaId,
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


create or replace view parsed_compound as

select
    monaId,
    compound ->> 'kind' as kind,
    compound -> 'tags' as tags,
    compound ->> 'inchi' as inchi,
    compound -> 'names' as names,
    compound ->> 'molFile' as molFile,
    (compound ->> 'computed'):: bool as computed,
    compound ->> 'inchikey' as inchikey,
    compound -> 'metaData' as metaData,
    compound -> 'classification' as classification
from (select
            content ->> 'id'                             as monaId,
            jsonb_array_elements(content -> 'compound') as compound
      from "spectrum_result" sr
      ) a;

create or replace view parsed_submitter as

select monaId,
       submitter ->> 'id' as submitterId,
       submitter ->> 'lastName' as lastName,
       submitter ->> 'firstName' as firstName,
       submitter ->> 'institution' as institution,
       submitter ->> 'emailAddress' as emailAddress
from
    (select content ->> 'id' as monaId,
           content -> 'submitter' as submitter
    from "spectrum_result" sr) a;


create or replace view parsed_library as

select monaId,
       library ->> 'description' as description,
       library ->> 'link' as link,
       library ->> 'library' as library,
       library -> 'tag' as tag
from
(select content ->> 'id' as monaId,
       content -> 'library' as library
from "spectrum_result" sr) a;


create or replace view parsed_metadata as

select
    monaId,
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
    monaId,
    ((jsonb_array_elements(compound_metadata) ->> 'name')) as name,
    ((jsonb_array_elements(compound_metadata) ->> 'unit')) as unit,
    ((jsonb_array_elements(compound_metadata) ->> 'value')) as value,
    ((jsonb_array_elements(compound_metadata) ->> 'hidden'):: bool) as hidden,
    ((jsonb_array_elements(compound_metadata) ->> 'category')) as category,
    ((jsonb_array_elements(compound_metadata) ->> 'computed'):: bool) as computed
from
    (select content ->> 'id' as monaId,
            jsonb_array_elements(content -> 'compound') -> 'metaData' as compound_metadata
     from "spectrum_result" sr) b;



