-- our view to do the binning, should be replaced with a materialized view once we switch to postgres 9.4 --
create or replace VIEW binned_ions as (select spectrum_id,round(mass) as mass, sum(intensity) as intensity from ion  where intensity > 0.05 group by mass, spectrum_id );

--calculates the similarity between 2 mass spectra--
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

    --max array length, the higher the slower the calculation is --
    array_len =  0;

    sameIon = 0;

    -- generate the unknown spectra matrix
   FOR i IN array_lower(unknownMasses, 1) .. array_upper(unknownMasses, 1)
   LOOP
        unknownSpectra[unknownMasses[i]] = unknownIntensities[i];
       --RAISE NOTICE 'ion %, intensity %',unknownMasses[i], unknownSpectra[unknownMasses[i]];

       if unknownMasses[i] > array_len
       then
            array_len = unknownMasses[i];
        end if;
   END LOOP;


    -- generate the known spectra matrix
   FOR i IN array_lower(knownMasses, 1) .. array_upper(knownMasses, 1)
   LOOP
        librarySpectra[knownMasses[i]] = knownIntensities[i];
        --RAISE NOTICE 'ion %, intensity %',knownMasses[i], librarySpectra[knownMasses[i]];


       if knownMasses[i] > array_len
       then
            array_len = knownMasses[i];
       end if;
   END LOOP;

    --add 1 to make sure its big enoug --
    array_len = array_len + 1;


    --raise notice 'unknown %', unknownSpectra ;
    --raise notice 'known %',librarySpectra;

    --raise notice 'max array len %', array_len;

    -- find the identical ions --

    for i in 0 .. array_len
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
    for i in 0 .. sameIon
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

    for i in 0 .. array_len
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
    f2 = 1.0/(sameIon-1) * summ;

    result = (1000.0/(unknownSpectraLength + sameIon))*((unknownSpectraLength * f1) + (sameIon * f2));

    --RAISE NOTICE 'similarity is %', result;

    RETURN result;

exception
    when division_by_zero then
        return 0.0;
END;
$$ LANGUAGE plpgsql;

-- calculates the similarity between 2 spectra ids--
create or replace function calculateSimilarity( unknownMasses float[], unknownIntensities float[],  library int8) returns float8 AS $$

DECLARE

    knownMasses float8[];
    knownIntensities float8[];
    result float8;

BEGIN

    --raise notice 'calculating % vs %', unknown, library;
    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library order by masses;

    raise notice 'library: %/%', knownMasses, knownIntensities;
    raise notice 'unknown: %/%', unknownMasses, unknownIntensities;

    return calculateSimilarity(unknownMasses, unknownIntensities,knownMasses,knownIntensities);

END;
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

    --raise notice 'calculating % vs %', unknown, library;

    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO unknownMasses, unknownIntensities from binned_ions where spectrum_id = unknown ;
    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library;

    return calculateSimilarity(unknownMasses, unknownIntensities,knownMasses,knownIntensities);

END;
$$ LANGUAGE plpgsql;


-- returns all the similar spectra to the provided id and can be adjusted based on requirements --
create or replace function findSimularSpectra(unknownSpectra int8, minSimilarity float8 = 700, identicalMassCount int8 = 5,resultCount int8 =10)

  returns table(similarity float8, id int8) AS

  $$

  DECLARE
    sql text := 'select calculateSimilarity(a.id,$1) as similarity, a.id as id from spectrum a, binned_ions b ' ||
                'where a.id = b.spectrum_id and a.id in ' ||
                '(select spectrum_id from binned_ions where spectrum_id != $1 and mass in (select mass from binned_ions ' ||
                'where spectrum_id = $1 order by intensity desc limit $2) group by spectrum_id having count(spectrum_id) = $2) group by a.id ' ||
                'having count(b.spectrum_id) < 2 * (select count(*)  from binned_ions where spectrum_id = $1) and calculateSimilarity(a.id,$1) > $3 order by similarity DESC limit $4';

   sourceMasses int8 := 0;
  BEGIN

    select count(spectrum_id) as masses into sourceMasses from binned_ions where spectrum_id = unknownSpectra;

    if(identicalMassCount > sourceMasses)
    then
      raise notice 'identical mass count reduced from % to %, since spectra has not enough ions!', identicalMassCount, sourceMasses;
      identicalMassCount = sourceMasses;
    end if;

    RETURN QUERY EXECUTE sql USING unknownSpectra,identicalMassCount,minSimilarity,resultCount;
  end

  $$ LANGUAGE plpgsql;




-- simple type used to define a spectra and so reduce excess arguments for returns --
create  type spectra as (ions float8[],intensities float8[]);

-- converts a spectra string to a float array for further use, including binning --
create or replace function convertSpectra(spectra text, bin boolean = true, topPeaks int = 5) returns spectra AS $$

DECLARE
    result spectra;
    masses float8[];
    intensities float8[];

    spectraSplit text[];
    ionSplit text[];

    maxValue float8 = 0;

BEGIN


    -- build the spectra object --
    spectraSplit = regexp_split_to_array(spectra,' ');

    for i in 1 .. array_upper(spectraSplit,1)
    LOOP
            ionSplit = regexp_split_to_array(spectraSplit[i],':');

           masses[i] := ionSplit[1];
           intensities[i] := ionSplit[2];

           if intensities[i] > maxValue
           then
            maxValue = intensities[i];
           end if;
    END LOOP;

    -- calculate relative values--
    for i in 1 .. array_upper(intensities,1)
    loop
        intensities[i] = intensities[i]/maxValue;
    end loop;

    -- bin the actual spectra, if requested--
    if bin then
        --query to sort and bin the arrays--
        select array_agg(spectra_masses), array_agg(spectra_intensities)into result.ions, result.intensities from(
            select round(mass) as spectra_masses, sum(intensy) as spectra_intensities from (
                select unnest(masses) as mass, unnest(intensities) as intensy
            ) a
            group by mass order by mass asc
        ) b;

    else
        result.ions = masses;
        result.intensities = intensities;
    end if;

    --sort our ions--
    RETURN result;

END;
$$ LANGUAGE plpgsql;



-- returns all the similar spectra to the provided id and can be adjusted based on requirements --
create or replace function findSimularSpectra(unknownSpectra text, minSimilarity float8 = 700, identicalMassCount int8 = 5,resultCount int8 =10)

  returns table(similarity float8, id int8) AS

  $$

  DECLARE

    sql text := 'select calculateSimilarity($1,$2,a.id) as similarity, a.id as id from spectrum a, binned_ions b ' ||
                'where a.id = b.spectrum_id and a.id in (' ||
                'select spectrum_id from binned_ions where mass in ( '||
                'select unnest($6) ) ' ||
                'group by spectrum_id having count(spectrum_id) = $5 ) group by a.id ' ||
                'having count(b.spectrum_id) < 2 * array_upper($1,1) ' ||
                'and calculateSimilarity($1,$2,a.id) > $3 order by similarity DESC limit $4';

   sourceMasses int8 := 0;

   --our converted spectra--
   parsedSpectra spectra;

    unknownMasses float8[];

    unknownIntensities float8[];

   topIons float8[];

  BEGIN


    parsedSpectra = convertSpectra(unknownSpectra,true);
    unknownMasses = parsedSpectra.ions;
    unknownIntensities = parsedSpectra.intensities;

    --generate our top ions--
    select array_agg(spectra_masses) into topIons from(
        select round(mass) as spectra_masses, sum(intensy) as spectra_intensities from (
            select unnest(unknownMasses) as mass, unnest(unknownIntensities) as intensy
        ) a
        group by mass order by spectra_intensities desc limit identicalMassCount
    ) b;

    raise notice 'selected top ions: %', topIons;

    if(identicalMassCount > array_upper(parsedSpectra.ions,1))
    then
      raise notice 'identical mass count reduced from % to %, since spectra has not enough ions!', identicalMassCount, array_upper(parsedSpectra.ions,1);
      identicalMassCount = array_upper(parsedSpectra.ions,1);
    end if;

    raise notice '%',sql;

    RETURN QUERY EXECUTE sql USING unknownMasses,unknownIntensities,minSimilarity,resultCount,identicalMassCount,topIons;
  end

  $$ LANGUAGE plpgsql;

--explain analyze select * from findSimularSpectra(947916,500) a, meta_data_value b, ion c where a.id = b.owner_id and a.id = c.spectrum_id


select * from findSimularSpectra('53.0:0.049 98.0:0.17902 113.0:0.0019 117.0:7.000000000000001E-4 112.0:0.01 94.0:0.006999999999999999 66.0:9.0E-4 105.0:0.013000000000000001 127.0:0.003 82.0:0.0059 111.0:0.039 69.0:0.34403 92.0:0.019 77.0:0.01 68.0:0.034 104.0:0.025 106.0:0.001 95.0:0.0159 114.0:0.052009999999999994 39.0:7.000000000000001E-4 52.0:0.005 54.0:0.004699999999999999 175.0:0.044000000000000004 85.0:0.05701 51.0:0.006999999999999999 89.0:0.018000000000000002 97.0:1.0 110.0:0.34904 130.0:1.0E-4 70.0:0.0125 159.0:0.0029 99.0:0.0079 84.0:0.12900999999999999 118.0:0.006 65.0:0.01 43.0:0.02141 74.0:0.0023 29.0:0.0159 40.0:0.019 42.0:0.049 109.0:0.0014000000000000002 80.0:0.003 100.0:0.024 132.0:0.039 76.0:0.016 96.0:0.028999999999999998 119.0:0.005 102.0:0.009000000000000001 93.0:0.006 44.0:0.24902999999999997 56.0:0.11501 131.0:0.003 58.0:0.0038 32.0:0.009000000000000001 129.0:0.019 120.0:0.001 27.0:0.07701 26.0:0.009000000000000001 63.0:0.009000000000000001 158.0:0.30902999999999997 126.0:1.0E-4 91.0:7.000000000000001E-4 176.0:0.009000000000000001 61.0:0.008 60.0:0.008 134.0:7.000000000000001E-4 128.0:0.15902 46.0:0.017 55.0:0.62406 45.0:0.06401 135.0:0.011000000000000001 59.0:0.07001 57.0:0.24502 116.0:0.050010000000000006 136.0:0.009000000000000001 101.0:0.01 78.0:1.0E-4 28.0:0.07400999999999999 71.0:0.42103999999999997 72.0:0.64006 83.0:0.08900999999999999 47.0:6.0E-4 67.0:0.06101 73.0:0.14901999999999999 115.0:0.20402 81.0:0.08401 64.0:0.004 103.0:1.0E-4 62.0:0.0409 160.0:0.004 88.0:0.013000000000000001 75.0:0.16902 87.0:0.004 79.0:0.018000000000000002 86.0:0.0013 30.0:0.013000000000000001 133.0:0.009000000000000001 31.0:0.07901 41.0:0.29403'::text,500) a;

