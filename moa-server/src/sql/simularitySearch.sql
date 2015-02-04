-- our view to do the binning --
create or replace VIEW binned_ions as (select spectrum_id,round(mass) as mass, sum(intensity) as intensity from ion  where intensity > 0.05 group by mass, spectrum_id );


-- returns all the similar spectra to the provided id -- DOESNT WORK YET
create or replace function findSimularSpectra(unknown int8, minSimilarity float8 = 700, identicalMassCount int8 = 5,resultCount int8 =10) returns table(similarity float8, id int8) AS $$

   
     BEGIN

        return query  select calculateSimilarity(a.id,unknown) as similarity, a.id from spectrum a, binned_ions b where a.id = b.spectrum_id and a.id in
    (
        select spectrum_id from binned_ions
            where mass in (
                select mass from binned_ions where spectrum_id = unknown order by intensity desc limit identicalMassCount
            )
            group by spectrum_id
            having count(spectrum_id) = identicalMassCount
    )
    group by a.id

    having count(b.spectrum_id) < 2 * (select count(*)  from binned_ions where spectrum_id = unknown) and
     calculateSimilarity(a.id,unknown) > minSimilarity order by similarity DESC limit resultCount; 

end
$$ LANGUAGE plpgsql;

-- calculates the similarity between 2 spectra ids--
create or replace function calculateSimilarity( unknown int8,  library int8) returns float8 AS $$

DECLARE

    unknownMasses float8[];
unknownIntensities float8[];
knownMasses float8[];
knownIntensities float8[];
    result float8;

    
BEGIN

    raise notice 'calculating % vs %', unknown, library;

    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO unknownMasses, unknownIntensities from binned_ions where spectrum_id = unknown;
    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library;

    return calculateSimilarity(unknownMasses, unknownIntensities,knownMasses,knownIntensities);

END;
$$ LANGUAGE plpgsql;

--calculates the similarity between 2 massspectra--
create or replace function calculateSimilarity(unknownMasses float8[], unknownIntensities float8[], knownMasses float8[], knownIntensities float8[]) returns float8 AS $$

DECLARE
    result float8;
    sameIons float8[];
    sameSpectraRelativeValuesunknown float8[];

    sameSpectraRelativeValueslibrary float8[];

    unknownSpectra float8[];
    librarySpectra float8[];

    unknownSpectraLength int :=0;

    f1 float8 := 0;
    f2 float8 := 0;

    lib float8 := 0;
    unk float8 := 0;

    sqrt1 float8 := 0;
    summ float8 := 0;
    summ4 float8 := 0;
    summ2 float8 := 0;
    summ3 float8 := 0;

    array_len int;
    sameIon int;

BEGIN

    --max array length--
    array_len = 1000;

    sameIon = 0;

    -- generate the unknown spectra matrix
   FOR i IN array_lower(unknownMasses, 1) .. array_upper(unknownMasses, 1)
   LOOP
        unknownSpectra[unknownMasses[i]] = unknownIntensities[i];
     --   RAISE NOTICE 'ion %, intensity %',unknownMasses[i], unknownSpectra[unknownMasses[i]];
   END LOOP;
    

    -- generate the known spectra matrix
   FOR i IN array_lower(knownMasses, 1) .. array_upper(knownMasses, 1)
   LOOP
        librarySpectra[knownMasses[i]] = knownIntensities[i];
      --  RAISE NOTICE 'ion %, intensity %',knownMasses[i], librarySpectra[knownMasses[i]];
   END LOOP;
    

    -- find the identical ions --

 for i in 1 .. array_len
    LOOP
        -- this will contain all the identical ions --
        IF unknownSpectra[i] is not null and librarySpectra[i] is not null
        then
            sameIons[sameIon] = i;
            sameSpectraRelativeValuesunknown[sameIon] = unknownSpectra[i];
            sameSpectraRelativeValueslibrary[sameIon] = librarySpectra[i];
            sameIon = sameIon + 1;
        END IF;
       
    END LOOP;


    -- calculate f1 --
    for i in 1 .. sameIon
    LOOP
        -- this will contain all the identical ions --
        IF sameIons[i] is not null 
        then
            sqrt1 = sqrt(sameSpectraRelativeValueslibrary[i] * sameSpectraRelativeValuesunknown[i]);
            summ4 = summ4 + (sqrt1 * sameIons[i]);

            IF i > 0 
            THEN
                unk = sameSpectraRelativeValuesunknown[i]/sameSpectraRelativeValuesunknown[i-1];
                lib = sameSpectraRelativeValueslibrary[i]/sameSpectraRelativeValueslibrary[i-1];

                if unk <= lib
                then
                    summ = summ + (unk/lib);
                else
                    summ = summ + (lib/unk);
                end if;
            END IF;
        END IF;
    END LOOP;

    unknownSpectraLength = 0;

    for i in 1 .. array_len
    LOOP
        IF librarySpectra[i] is not null and librarySpectra[i] > 0
        then
            summ2 = summ2 + (librarySpectra[i] * i);
        END IF;
       
        IF unknownSpectra[i] is not null and unknownSpectra[i] > 0
        then
            unknownSpectraLength = unknownSpectraLength + 1;
            summ3 = summ3 + (unknownSpectra[i] * i);
        END IF;
    END LOOP;

    f1 = summ4 / sqrt(summ2 * summ3);
    f2 = 1.0/sameIon * summ;

    result = (1000.0/(unknownSpectraLength + sameIon))*((unknownSpectraLength * f1) + (sameIon * f2));


    RETURN result;

exception 
    when division_by_zero then
        return 0.0;
END;
$$ LANGUAGE plpgsql;




   explain analyze
    select calculateSimilarity(a.id,947916) as similarity, a.id from spectrum a, binned_ions b where a.id = b.spectrum_id and a.id in
    (
        select spectrum_id from binned_ions
            where mass in (
                select mass from binned_ions where spectrum_id = 947916 order by intensity desc limit 5
            )
            group by spectrum_id
            having count(spectrum_id) = 5
    )
    group by a.id

    having count(b.spectrum_id) < 2 * (select count(*)  from binned_ions where spectrum_id = 947916) and
     calculateSimilarity(a.id,947916) > 500

    order by similarity DESC limit 5




select calculateSimilarity(a.id,9) as similarity from spectrum a order by similarity DESC 

 select findSimularSpectra(9,500,10)