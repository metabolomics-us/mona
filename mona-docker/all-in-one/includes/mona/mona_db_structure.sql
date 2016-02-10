--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: tablefunc; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS tablefunc WITH SCHEMA public;


--
-- Name: EXTENSION tablefunc; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION tablefunc IS 'functions that manipulate whole tables, including crosstab';


SET search_path = public, pg_catalog;

--
-- Name: spectra; Type: TYPE; Schema: public; Owner: mona
--

CREATE TYPE spectra AS (
	ions double precision[],
	intensities double precision[]
);


ALTER TYPE public.spectra OWNER TO mona;

--
-- Name: calculatesimilarity(spectra, bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION calculatesimilarity(unknown spectra, library bigint) RETURNS double precision
    LANGUAGE plpgsql
    AS $$

DECLARE

  knownMasses float8[];
  knownIntensities float8[];
  result float8;

BEGIN

  
  SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library order by masses;

  return calculateSimilarity(unknown.ions, unknown.intensities,knownMasses,knownIntensities);

END;
$$;


ALTER FUNCTION public.calculatesimilarity(unknown spectra, library bigint) OWNER TO mona;

--
-- Name: calculatesimilarity(text, bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION calculatesimilarity(unknown text, library bigint) RETURNS double precision
    LANGUAGE plpgsql
    AS $$

DECLARE


BEGIN

  return calculatesimilarity(convertspectra(unknown,true,50),library);

END;
$$;


ALTER FUNCTION public.calculatesimilarity(unknown text, library bigint) OWNER TO mona;

--
-- Name: calculatesimilarity(bigint, bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION calculatesimilarity(unknown bigint, library bigint) RETURNS double precision
    LANGUAGE plpgsql
    AS $$

DECLARE

    unknownMasses float8[];
    unknownIntensities float8[];
    knownMasses float8[];
    knownIntensities float8[];
    result float8;

BEGIN

    

    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO unknownMasses, unknownIntensities from binned_ions where spectrum_id = unknown ;
    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library;

    return calculateSimilarity(unknownMasses, unknownIntensities,knownMasses,knownIntensities);

END;
$$;


ALTER FUNCTION public.calculatesimilarity(unknown bigint, library bigint) OWNER TO mona;

--
-- Name: calculatesimilarity(double precision[], double precision[], bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION calculatesimilarity(unknownmasses double precision[], unknownintensities double precision[], library bigint) RETURNS double precision
    LANGUAGE plpgsql
    AS $$

DECLARE

    knownMasses float8[];
    knownIntensities float8[];
    result float8;

BEGIN

    
    SELECT  array_agg(mass) as masses, array_agg(intensity) INTO knownMasses, knownIntensities from binned_ions where spectrum_id = library order by masses;

    raise notice 'library: %/%', knownMasses, knownIntensities;
    raise notice 'unknown: %/%', unknownMasses, unknownIntensities;

    return calculateSimilarity(unknownMasses, unknownIntensities,knownMasses,knownIntensities);

END;
$$;


ALTER FUNCTION public.calculatesimilarity(unknownmasses double precision[], unknownintensities double precision[], library bigint) OWNER TO mona;

--
-- Name: calculatesimilarity(double precision[], double precision[], double precision[], double precision[]); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION calculatesimilarity(unknownmasses double precision[], unknownintensities double precision[], knownmasses double precision[], knownintensities double precision[]) RETURNS double precision
    LANGUAGE plpgsql
    AS $$

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

    
    array_len =  0;

    sameIon = 0;

    
   FOR i IN array_lower(unknownMasses, 1) .. array_upper(unknownMasses, 1)
   LOOP
        unknownSpectra[unknownMasses[i]] = unknownIntensities[i];
       

       if unknownMasses[i] > array_len
       then
            array_len = unknownMasses[i];
        end if;
   END LOOP;


    
   FOR i IN array_lower(knownMasses, 1) .. array_upper(knownMasses, 1)
   LOOP
        librarySpectra[knownMasses[i]] = knownIntensities[i];
        


       if knownMasses[i] > array_len
       then
            array_len = knownMasses[i];
       end if;
   END LOOP;

    
    array_len = array_len + 1;


    
    

    

    

    for i in 0 .. array_len
    LOOP
        
        IF unknownSpectra[i] is not null and librarySpectra[i] is not null
        then
            sameIons[sameIon] = i;
            sameSpectraRelativeValuesunknown[sameIon] = unknownSpectra[i];
            sameSpectraRelativeValueslibrary[sameIon] = librarySpectra[i];
            sameIon = sameIon + 1;
        END IF;

    END LOOP;


    
    for i in 0 .. sameIon
    LOOP
        
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

    

    RETURN result;

exception
    when division_by_zero then
        return 0.0;
END;
$$;


ALTER FUNCTION public.calculatesimilarity(unknownmasses double precision[], unknownintensities double precision[], knownmasses double precision[], knownintensities double precision[]) OWNER TO mona;

--
-- Name: convertspectra(text); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION convertspectra(spectra text) RETURNS spectra
    LANGUAGE plpgsql
    AS $$

DECLARE
    result spectra;
    masses float8[];
    intensities float8[];

    spectraSplit text[];
    ionSplit text[];

    maxValue float8 = 0;

BEGIN


    
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

    
    for i in 1 .. array_upper(intensities,1)
    loop
        intensities[i] = intensities[i]/maxValue;
    end loop;

        result.ions = masses;
        result.intensities = intensities;

    
    RETURN result;

END;
$$;


ALTER FUNCTION public.convertspectra(spectra text) OWNER TO mona;

--
-- Name: convertspectra(text, boolean, integer); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION convertspectra(spectra text, bin boolean DEFAULT true, toppeaks integer DEFAULT 5) RETURNS spectra
    LANGUAGE plpgsql
    AS $$

DECLARE
    result spectra;
    masses float8[];
    intensities float8[];

    spectraSplit text[];
    ionSplit text[];

    maxValue float8 = 0;

BEGIN


    
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

    
    for i in 1 .. array_upper(intensities,1)
    loop
        intensities[i] = intensities[i]/maxValue;
    end loop;

    
    if bin then
        
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

    
    RETURN result;

END;
$$;


ALTER FUNCTION public.convertspectra(spectra text, bin boolean, toppeaks integer) OWNER TO mona;

--
-- Name: findsimularspectra(bigint, double precision, bigint, bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION findsimularspectra(unknownspectra bigint, minsimilarity double precision DEFAULT 700, identicalmasscount bigint DEFAULT 5, resultcount bigint DEFAULT 10) RETURNS TABLE(similarity double precision, id bigint)
    LANGUAGE plpgsql
    AS $_$

  DECLARE
    sql text := 'select calculateSimilarity(a.id,$1) as similarity, a.id as id from spectrum a, binned_ions b ' ||
                'where a.id = b.spectrum_id and a.id in ' ||
                '(select spectrum_id from binned_ions where spectrum_id != $1 and hash != $5 and mass in (select mass from binned_ions ' ||
                'where spectrum_id = $1 order by intensity desc limit $2) group by spectrum_id having count(spectrum_id) = $2) group by a.id ' ||
                'having count(b.spectrum_id) < 2 * (select count(*)  from binned_ions where spectrum_id = $1) and calculateSimilarity(a.id,$1) > $3 order by similarity DESC limit $4';

   sourceMasses int8 := 0;
   hashCode text :='';
  BEGIN

    select "hash" into hashCode from spectrum a where a.id = unknownSpectra;

    select count(spectrum_id) as masses into sourceMasses from binned_ions where spectrum_id = unknownSpectra;

    if(identicalMassCount > sourceMasses)
    then
      raise notice 'identical mass count reduced from % to %, since spectra has not enough ions!', identicalMassCount, sourceMasses;
      identicalMassCount = sourceMasses;
    end if;

    RETURN QUERY EXECUTE sql USING unknownSpectra,identicalMassCount,minSimilarity,resultCount,hashCode;
  end

  $_$;


ALTER FUNCTION public.findsimularspectra(unknownspectra bigint, minsimilarity double precision, identicalmasscount bigint, resultcount bigint) OWNER TO mona;

--
-- Name: findsimularspectra(text, double precision, bigint, bigint); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION findsimularspectra(unknownspectra text, minsimilarity double precision DEFAULT 700, identicalmasscount bigint DEFAULT 5, resultcount bigint DEFAULT 10) RETURNS TABLE(similarity double precision, id bigint)
    LANGUAGE plpgsql
    AS $_$

  DECLARE

    sql text := 'select calculateSimilarity($1,$2,a.id) as similarity, a.id as id from spectrum a, binned_ions b ' ||
                'where a.id = b.spectrum_id and a.id in (' ||
                'select spectrum_id from binned_ions where mass in ( '||
                'select unnest($6) ) ' ||
                'group by spectrum_id having count(spectrum_id) = $5 ) group by a.id ' ||
                'having count(b.spectrum_id) < 2 * array_upper($1,1) ' ||
                'and calculateSimilarity($1,$2,a.id) > $3 order by similarity DESC limit $4';

   sourceMasses int8 := 0;

   
   parsedSpectra spectra;

    unknownMasses float8[];

    unknownIntensities float8[];

   topIons float8[];

  BEGIN


    parsedSpectra = convertSpectra(unknownSpectra,true);
    unknownMasses = parsedSpectra.ions;
    unknownIntensities = parsedSpectra.intensities;

    
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

  $_$;


ALTER FUNCTION public.findsimularspectra(unknownspectra text, minsimilarity double precision, identicalmasscount bigint, resultcount bigint) OWNER TO mona;

--
-- Name: get_type(text); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION get_type(stringvalue text) RETURNS character varying
    LANGUAGE plpgsql
    AS $$

begin
    begin
        begin
           if(stringValue::double precision is not null) then
            begin
                begin
                    if(stringValue::integer is not null) then
                        return 'integer';
                    end if;

               exception when numeric_value_out_of_range then
                    return 'long';
                end;
            exception when invalid_text_representation then
                return 'double';
            end;

            return 'double';

           elsif(stringValue::boolean is not null) then
                return 'boolean';
           else
                return 'string';    
           end if;       

        EXCEPTION when invalid_text_representation  then
            return 'string';
        end;
    exception when numeric_value_out_of_range then
        return 'unknown type';
    end;
end;

$$;


ALTER FUNCTION public.get_type(stringvalue text) OWNER TO mona;

--
-- Name: sha1(bytea); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION sha1(bytea) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
      SELECT encode(digest($1, 'sha1'), 'hex')
 $_$;


ALTER FUNCTION public.sha1(bytea) OWNER TO mona;

--
-- Name: sha1(text); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION sha1(text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
      SELECT encode(digest($1, 'sha1'), 'hex')
 $_$;


ALTER FUNCTION public.sha1(text) OWNER TO mona;

--
-- Name: spectra_statistics_for_metadata(text); Type: FUNCTION; Schema: public; Owner: mona
--

CREATE FUNCTION spectra_statistics_for_metadata(field text, OUT count bigint, OUT value text) RETURNS SETOF record
    LANGUAGE sql
    AS $_$

select
   count,
   value
from
   (  select
      count(*) as count,
      value
   from
      (  

   select
         distinct f.splash || inchi_key,
         h.string_value as value 
      from
         tag a,
         spectrum b,
         compound c,
         compound_link d,
         tag_link e,
         splash f, 
         meta_data g,
         meta_data_value h
      where
         f.spectrum_id = b.id
         and g.id = h.meta_data_id
         and h.owner_id = b.id
         and e.tag_id = a.id 
         and e.owner_id = b.id 
         and b.id = d.spectrum_id 
         and d.compound_id = c.id 
         and lower(g.name) = $1

  ) a 
   group by
      value  ) a  ;
               

$_$;


ALTER FUNCTION public.spectra_statistics_for_metadata(field text, OUT count bigint, OUT value text) OWNER TO mona;

SET default_with_oids = false;

--
-- Name: authentication_token; Type: TABLE; Schema: public; Owner: mona
--

CREATE TABLE authentication_token (
    id bigint NOT NULL,
    version bigint NOT NULL,
    email_address character varying(255) NOT NULL,
    token_value character varying(255) NOT NULL
);


ALTER TABLE public.authentication_token OWNER TO mona;

--
-- Name: ion; Type: TABLE; Schema: public; Owner: mona
--

CREATE TABLE ion (
    id bigint NOT NULL,
    intensity double precision,
    mass double precision,
    spectrum_id bigint
);


ALTER TABLE public.ion OWNER TO mona;

--
-- Name: spectrum; Type: TABLE; Schema: public; Owner: mona
--

CREATE TABLE spectrum (
    id bigint NOT NULL,
    score_id bigint,
    submitter_id bigint,
    splash_id bigint,
    hash character varying(255),
    deleted boolean
);


ALTER TABLE public.spectrum OWNER TO mona;

--
-- Name: binned_ions; Type: VIEW; Schema: public; Owner: mona
--

CREATE VIEW binned_ions AS
    SELECT a.spectrum_id, round(a.mass) AS mass, sum(a.intensity) AS intensity, b.hash FROM ion a, spectrum b WHERE ((b.id = a.spectrum_id) AND (a.intensity > (0.05)::double precision)) GROUP BY a.mass, a.spectrum_id, b.hash;


ALTER TABLE public.binned_ions OWNER TO mona;

--
-- Name: comment; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE comment (
    id bigint NOT NULL,
    comment text NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL
);


ALTER TABLE public.comment OWNER TO mona;

--
-- Name: compound; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE compound (
    id bigint NOT NULL,
    inchi text,
    inchi_key character varying(255),
    mol_file text
);


ALTER TABLE public.compound OWNER TO mona;

--
-- Name: compound_comment; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE compound_comment (
    compound_comments_id bigint,
    comment_id bigint
);


ALTER TABLE public.compound_comment OWNER TO mona;

--
-- Name: compound_link; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE compound_link (
    id bigint NOT NULL,
    compound_id bigint NOT NULL,
    spectrum_id bigint NOT NULL,
    type character varying(255) NOT NULL
);


ALTER TABLE public.compound_link OWNER TO mona;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: mona
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO mona;

--
-- Name: impact; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE impact (
    id bigint NOT NULL,
    impact_value double precision NOT NULL,
    reason character varying(255) NOT NULL,
    score_id bigint NOT NULL,
    scoring_class character varying(255) NOT NULL
);


ALTER TABLE public.impact OWNER TO mona;

--
-- Name: meta_data; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE meta_data (
    id bigint NOT NULL,
    category_id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(1000) NOT NULL,
    priority integer,
    requires_unit boolean NOT NULL,
    searchable boolean NOT NULL,
    type character varying(255) NOT NULL,
    hidden boolean
);


ALTER TABLE public.meta_data OWNER TO mona;

--
-- Name: meta_data_category; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE meta_data_category (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    visible boolean NOT NULL
);


ALTER TABLE public.meta_data_category OWNER TO mona;

--
-- Name: meta_data_value; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE meta_data_value (
    id bigint NOT NULL,
    computed boolean NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    meta_data_id bigint,
    owner_id bigint,
    reason_for_suspicion character varying(255),
    score_id bigint,
    suspect boolean NOT NULL,
    unit character varying(255),
    class character varying(255) NOT NULL,
    integer_value integer,
    string_value character varying(5000),
    boolean_value character varying(255),
    double_value double precision,
    deleted boolean,
    url character varying(5000)
);


ALTER TABLE public.meta_data_value OWNER TO mona;

--
-- Name: meta_data_value_view; Type: VIEW; Schema: public; Owner: mona
--

CREATE VIEW meta_data_value_view AS
    SELECT meta_data_value.id, meta_data_value.computed, meta_data_value.date_created, meta_data_value.last_updated, meta_data_value.meta_data_id, meta_data_value.owner_id, meta_data_value.reason_for_suspicion, meta_data_value.score_id, meta_data_value.suspect, meta_data_value.unit, meta_data_value.class, meta_data_value.integer_value, meta_data_value.string_value, meta_data_value.boolean_value, meta_data_value.double_value, meta_data_value.deleted FROM meta_data_value WHERE (meta_data_value.deleted = false);


ALTER TABLE public.meta_data_value_view OWNER TO mona;

--
-- Name: name; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE name (
    id bigint NOT NULL,
    compound_id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    name text NOT NULL,
    computed boolean,
    score_id bigint,
    source character varying(255)
);


ALTER TABLE public.name OWNER TO mona;

--
-- Name: news; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE news (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text,
    expires bigint NOT NULL,
    icon_class character varying(255) DEFAULT 'none'::character varying NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    title text NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL
);


ALTER TABLE public.news OWNER TO mona;

--
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


ALTER TABLE public.qrtz_blob_triggers OWNER TO postgres;

--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_calendars (
    sched_name character varying(120) NOT NULL,
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);


ALTER TABLE public.qrtz_calendars OWNER TO postgres;

--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_cron_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);


ALTER TABLE public.qrtz_cron_triggers OWNER TO postgres;

--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_fired_triggers (
    sched_name character varying(120) NOT NULL,
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_nonconcurrent boolean,
    requests_recovery boolean
);


ALTER TABLE public.qrtz_fired_triggers OWNER TO postgres;

--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_job_details (
    sched_name character varying(120) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_nonconcurrent boolean NOT NULL,
    is_update_data boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);


ALTER TABLE public.qrtz_job_details OWNER TO postgres;

--
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_locks (
    sched_name character varying(120) NOT NULL,
    lock_name character varying(40) NOT NULL
);


ALTER TABLE public.qrtz_locks OWNER TO postgres;

--
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_paused_trigger_grps (
    sched_name character varying(120) NOT NULL,
    trigger_group character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_paused_trigger_grps OWNER TO postgres;

--
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_scheduler_state (
    sched_name character varying(120) NOT NULL,
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


ALTER TABLE public.qrtz_scheduler_state OWNER TO postgres;

--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_simple_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


ALTER TABLE public.qrtz_simple_triggers OWNER TO postgres;

--
-- Name: qrtz_simprop_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_simprop_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);


ALTER TABLE public.qrtz_simprop_triggers OWNER TO postgres;

--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: postgres TO mona
--

CREATE TABLE qrtz_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);


ALTER TABLE public.qrtz_triggers OWNER TO postgres;

--
-- Name: query; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE query (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text NOT NULL,
    label character varying(255) NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    query text NOT NULL,
    query_count integer NOT NULL,
    query_export_id bigint
);


ALTER TABLE public.query OWNER TO mona;

--
-- Name: role; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE role (
    id bigint NOT NULL,
    version bigint NOT NULL,
    authority character varying(255) NOT NULL
);


ALTER TABLE public.role OWNER TO mona;

--
-- Name: score; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE score (
    id bigint NOT NULL,
    relative_score double precision NOT NULL,
    scaled_score double precision NOT NULL,
    score double precision NOT NULL
);


ALTER TABLE public.score OWNER TO mona;

--
-- Name: spectra_compound_keys; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE spectra_compound_keys (
    key text,
    value character varying(5000),
    name character varying(1000),
    text character varying(255),
    id bigint
);


ALTER TABLE public.spectra_compound_keys OWNER TO mona;

--
-- Name: spectra_database_distribution; Type: VIEW; Schema: public; Owner: mona
--

CREATE VIEW spectra_database_distribution AS
    SELECT ct.key, ct.splash, ct.binbase, ct.gcms, ct.gnps, ct.hmdb, ct.lcms, ct.lipidblast, ct.massbank FROM crosstab('select a.splash || ''-'' || inchi_key  as key,a.splash,lower(text) as category, TRUE as value  from splash a, compound b, compound_link f, spectrum c, tag_link d, tag e where  f.type = ''CHEMICAL'' and b.id = f.compound_id and f.spectrum_id = c.id and d.tag_id = e.id and d.owner_id = c.id and a.spectrum_id = c.id and deleted = false order by key'::text, 'select distinct lower(text) as category from tag where lower(text) in (''hmdb'',''gnps'',''massbank'',''lipidblast'',''gcms'',''lcms'',''binbase'') order by 1'::text) ct(key text, splash text, binbase boolean, gcms boolean, gnps boolean, hmdb boolean, lcms boolean, lipidblast boolean, massbank boolean);


ALTER TABLE public.spectra_database_distribution OWNER TO mona;

--
-- Name: spectra_scores_by_submitter; Type: VIEW; Schema: public; Owner: mona
--

CREATE VIEW spectra_scores_by_submitter AS
    SELECT count(*) AS count, avg(b.relative_score) AS relative_score, avg(b.scaled_score) AS scaled_score, avg(b.score) AS score, a.submitter_id FROM spectrum a, score b WHERE (a.score_id = b.id) GROUP BY a.submitter_id ORDER BY avg(b.relative_score);


ALTER TABLE public.spectra_scores_by_submitter OWNER TO mona;

--
-- Name: spectrum_comment; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE spectrum_comment (
    spectrum_comments_id bigint,
    comment_id bigint
);


ALTER TABLE public.spectrum_comment OWNER TO mona;

--
-- Name: spectrum_query_download; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE spectrum_query_download (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    email_address text,
    export_file text NOT NULL,
    label text NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    query text NOT NULL,
    query_count integer NOT NULL,
    query_file text NOT NULL
);


ALTER TABLE public.spectrum_query_download OWNER TO mona;

--
-- Name: splash; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE splash (
    id bigint NOT NULL,
    version bigint NOT NULL,
    spectrum_id bigint NOT NULL,
    splash character varying(255) NOT NULL,
    block1 character varying(255) NOT NULL,
    block2 character varying(255) NOT NULL,
    block3 character varying(255) NOT NULL
);


ALTER TABLE public.splash OWNER TO mona;

--
-- Name: statistics; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE statistics (
    id bigint NOT NULL,
    category character varying(255) NOT NULL,
    date_created timestamp without time zone NOT NULL,
    description text NOT NULL,
    title character varying(255) NOT NULL,
    value double precision NOT NULL
);


ALTER TABLE public.statistics OWNER TO mona;

--
-- Name: submitter; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE submitter (
    id bigint NOT NULL,
    account_enabled boolean NOT NULL,
    account_expired boolean NOT NULL,
    account_locked boolean NOT NULL,
    date_created timestamp without time zone NOT NULL,
    email_address character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    institution character varying(255),
    last_name character varying(255) NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    password character varying(255) NOT NULL,
    password_expired boolean NOT NULL
);


ALTER TABLE public.submitter OWNER TO mona;

--
-- Name: submitter_role; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE submitter_role (
    role_id bigint NOT NULL,
    submitter_id bigint NOT NULL
);


ALTER TABLE public.submitter_role OWNER TO mona;

--
-- Name: supports_meta_data; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE supports_meta_data (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL
);


ALTER TABLE public.supports_meta_data OWNER TO mona;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE tag (
    id bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    rule_based boolean NOT NULL,
    text character varying(255) NOT NULL
);


ALTER TABLE public.tag OWNER TO mona;

--
-- Name: tag_link; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE tag_link (
    id bigint NOT NULL,
    owner_id bigint NOT NULL,
    tag_id bigint NOT NULL
);


ALTER TABLE public.tag_link OWNER TO mona;

--
-- Name: webhook; Type: TABLE; Schema: public; Owner: mona TO mona
--

CREATE TABLE webhook (
    id bigint NOT NULL,
    version bigint NOT NULL,
    date_created timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    mona_event character varying(255) NOT NULL,
    submitter_id bigint NOT NULL,
    target_url character varying(255) NOT NULL
);


ALTER TABLE public.webhook OWNER TO mona;

--
-- Name: authentication_token_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY authentication_token
    ADD CONSTRAINT authentication_token_pkey PRIMARY KEY (id);


--
-- Name: comment_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (id);


--
-- Name: compound_inchi_key_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY compound
    ADD CONSTRAINT compound_inchi_key_key UNIQUE (inchi_key);


--
-- Name: compound_link_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY compound_link
    ADD CONSTRAINT compound_link_pkey PRIMARY KEY (id);


--
-- Name: compound_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY compound
    ADD CONSTRAINT compound_pkey PRIMARY KEY (id);


--
-- Name: impact_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY impact
    ADD CONSTRAINT impact_pkey PRIMARY KEY (id);


--
-- Name: ion_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY ion
    ADD CONSTRAINT ion_pkey PRIMARY KEY (id);


--
-- Name: meta_data_category_name_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY meta_data_category
    ADD CONSTRAINT meta_data_category_name_key UNIQUE (name);


--
-- Name: meta_data_category_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY meta_data_category
    ADD CONSTRAINT meta_data_category_pkey PRIMARY KEY (id);


--
-- Name: meta_data_name_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY meta_data
    ADD CONSTRAINT meta_data_name_key UNIQUE (name);


--
-- Name: meta_data_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY meta_data
    ADD CONSTRAINT meta_data_pkey PRIMARY KEY (id);


--
-- Name: meta_data_value_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY meta_data_value
    ADD CONSTRAINT meta_data_value_pkey PRIMARY KEY (id);


--
-- Name: name_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY name
    ADD CONSTRAINT name_pkey PRIMARY KEY (id);


--
-- Name: news_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY news
    ADD CONSTRAINT news_pkey PRIMARY KEY (id);


--
-- Name: qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);


--
-- Name: qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);


--
-- Name: qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);


--
-- Name: qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);


--
-- Name: qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);


--
-- Name: qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);


--
-- Name: qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres TO mona
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: query_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY query
    ADD CONSTRAINT query_pkey PRIMARY KEY (id);


--
-- Name: role_authority_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_authority_key UNIQUE (authority);


--
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: score_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY score
    ADD CONSTRAINT score_pkey PRIMARY KEY (id);


--
-- Name: spectrum_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY spectrum
    ADD CONSTRAINT spectrum_pkey PRIMARY KEY (id);


--
-- Name: spectrum_query_download_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY spectrum_query_download
    ADD CONSTRAINT spectrum_query_download_pkey PRIMARY KEY (id);


--
-- Name: splash_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY splash
    ADD CONSTRAINT splash_pkey PRIMARY KEY (id);


--
-- Name: statistics_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY statistics
    ADD CONSTRAINT statistics_pkey PRIMARY KEY (id);


--
-- Name: submitter_email_address_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY submitter
    ADD CONSTRAINT submitter_email_address_key UNIQUE (email_address);


--
-- Name: submitter_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY submitter
    ADD CONSTRAINT submitter_pkey PRIMARY KEY (id);


--
-- Name: submitter_role_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY submitter_role
    ADD CONSTRAINT submitter_role_pkey PRIMARY KEY (role_id, submitter_id);


--
-- Name: supports_meta_data_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY supports_meta_data
    ADD CONSTRAINT supports_meta_data_pkey PRIMARY KEY (id);


--
-- Name: tag_link_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY tag_link
    ADD CONSTRAINT tag_link_pkey PRIMARY KEY (id);


--
-- Name: tag_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (id);


--
-- Name: tag_text_key; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT tag_text_key UNIQUE (text);


--
-- Name: webhook_pkey; Type: CONSTRAINT; Schema: public; Owner: mona TO mona
--

ALTER TABLE ONLY webhook
    ADD CONSTRAINT webhook_pkey PRIMARY KEY (id);


--
-- Name: compound_link_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX compound_link_index ON compound_link USING btree (compound_id, spectrum_id, type);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_c ON qrtz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_g; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_g ON qrtz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_j ON qrtz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_state; Type: INDEX; Schema: public; Owner: postgres TO mona
--

CREATE INDEX idx_qrtz_t_state ON qrtz_triggers USING btree (sched_name, trigger_state);


--
-- Name: inchi_key_idx; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX inchi_key_idx ON compound USING btree (inchi_key);


--
-- Name: index-category-name; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-category-name" ON meta_data USING btree (category_id, name);


--
-- Name: index-compound-name; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-compound-name" ON name USING btree (compound_id, name);


--
-- Name: index-meta-name; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-meta-name" ON meta_data USING btree (name);


--
-- Name: index-spectra-meta-boolean; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-spectra-meta-boolean" ON meta_data_value USING btree (boolean_value, meta_data_id, owner_id);


--
-- Name: index-spectra-meta-double; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-spectra-meta-double" ON meta_data_value USING btree (double_value, meta_data_id, owner_id);


--
-- Name: index-spectra-meta-string; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-spectra-meta-string" ON meta_data_value USING btree (string_value, meta_data_id, owner_id);


--
-- Name: index-spectrum-deleted; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX "index-spectrum-deleted" ON spectrum USING btree (deleted);


--
-- Name: index_impact_score_id; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX index_impact_score_id ON impact USING btree (score_id, id);


--
-- Name: index_meta_data_values_deleted; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX index_meta_data_values_deleted ON meta_data_value USING btree (deleted, string_value, owner_id) WHERE ((deleted = true) AND (string_value IS NOT NULL));


--
-- Name: index_meta_data_values_stringvalue_isnotnull; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX index_meta_data_values_stringvalue_isnotnull ON meta_data_value USING btree (string_value, owner_id) WHERE (string_value IS NOT NULL);


--
-- Name: ion_intensity_mass_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX ion_intensity_mass_index ON ion USING btree (intensity, mass);


--
-- Name: ions_spectrum_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX ions_spectrum_id_index ON ion USING btree (spectrum_id);


--
-- Name: lower_name_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX lower_name_index ON name USING btree (lower(name));


--
-- Name: metadata_value_metadata_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX metadata_value_metadata_id_index ON meta_data_value USING btree (meta_data_id);


--
-- Name: metadata_value_owner_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX metadata_value_owner_index ON meta_data_value USING btree (owner_id);


--
-- Name: metavalue_score_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX metavalue_score_index ON meta_data_value USING btree (score_id);


--
-- Name: name_gin_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX name_gin_index ON name USING gin (to_tsvector('english'::regconfig, name));


--
-- Name: name_gist_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX name_gist_index ON name USING gist (name gist_trgm_ops);


--
-- Name: name_index_like_compound; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX name_index_like_compound ON name USING btree (compound_id, name text_pattern_ops);


--
-- Name: names_compound_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX names_compound_id_index ON name USING btree (compound_id);


--
-- Name: news_type_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX news_type_index ON news USING btree (type);


--
-- Name: spectracompoundkeysindexnametext; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectracompoundkeysindexnametext ON spectra_compound_keys USING btree (text, name);


--
-- Name: spectrum_comments_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_comments_index ON spectrum_comment USING btree (spectrum_comments_id, comment_id);


--
-- Name: spectrum_none_deleted; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_none_deleted ON spectrum USING btree (deleted) WHERE (deleted = false);


--
-- Name: spectrum_not_deleted; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_not_deleted ON spectrum USING btree (deleted) WHERE (deleted = false);


--
-- Name: spectrum_score_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_score_id_index ON spectrum USING btree (score_id);


--
-- Name: spectrum_splash_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_splash_id_index ON spectrum USING btree (splash_id);


--
-- Name: spectrum_submitter_id_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX spectrum_submitter_id_index ON spectrum USING btree (submitter_id);


--
-- Name: splash_block2_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX splash_block2_index ON splash USING gist (block2 gist_trgm_ops);


--
-- Name: splash_spectra_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX splash_spectra_index ON splash USING btree (spectrum_id);


--
-- Name: statistics_category_index; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX statistics_category_index ON statistics USING btree (category);


--
-- Name: tag_deleted; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX tag_deleted ON tag USING btree (text) WHERE ((text)::text <> 'deleted'::text);


--
-- Name: tag_link_owner_tag; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX tag_link_owner_tag ON tag_link USING btree (owner_id, tag_id);


--
-- Name: tag_link_tag; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX tag_link_tag ON tag_link USING btree (tag_id);


--
-- Name: tag_link_tag_id; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX tag_link_tag_id ON tag_link USING btree (tag_id, id);


--
-- Name: text_idx; Type: INDEX; Schema: public; Owner: mona TO mona
--

CREATE INDEX text_idx ON tag USING btree (text);


--
-- Name: fk19808be1c3249; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY ion
    ADD CONSTRAINT fk19808be1c3249 FOREIGN KEY (spectrum_id) REFERENCES spectrum(id);


--
-- Name: fk19808fd5f963d; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY ion
    ADD CONSTRAINT fk19808fd5f963d FOREIGN KEY (id) REFERENCES supports_meta_data(id);


--
-- Name: fk337a8bbbb5ac09; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY name
    ADD CONSTRAINT fk337a8bbbb5ac09 FOREIGN KEY (compound_id) REFERENCES compound(id);


--
-- Name: fk53f14bf1b078d9d8; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum_comment
    ADD CONSTRAINT fk53f14bf1b078d9d8 FOREIGN KEY (spectrum_comments_id) REFERENCES spectrum(id);


--
-- Name: fk53f14bf1fb12c14b; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum_comment
    ADD CONSTRAINT fk53f14bf1fb12c14b FOREIGN KEY (comment_id) REFERENCES comment(id);


--
-- Name: fk80e417d130c0abcc; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum
    ADD CONSTRAINT fk80e417d130c0abcc FOREIGN KEY (score_id) REFERENCES score(id);


--
-- Name: fk80e417d145dd90cb; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum
    ADD CONSTRAINT fk80e417d145dd90cb FOREIGN KEY (submitter_id) REFERENCES submitter(id);


--
-- Name: fk80e417d1fd5f963d; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum
    ADD CONSTRAINT fk80e417d1fd5f963d FOREIGN KEY (id) REFERENCES supports_meta_data(id);


--
-- Name: fk_27iqfua8yswbqbn2nrw6u0piv; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY webhook
    ADD CONSTRAINT fk_27iqfua8yswbqbn2nrw6u0piv FOREIGN KEY (submitter_id) REFERENCES submitter(id);


--
-- Name: fk_32a83mfsf57tn7uoaj6k7o1ag; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY spectrum
    ADD CONSTRAINT fk_32a83mfsf57tn7uoaj6k7o1ag FOREIGN KEY (splash_id) REFERENCES splash(id);


--
-- Name: fk_485et593y8djtq7h6nte29h3c; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY splash
    ADD CONSTRAINT fk_485et593y8djtq7h6nte29h3c FOREIGN KEY (spectrum_id) REFERENCES spectrum(id);


--
-- Name: fk_4m4vppgc58ard1xyn3ywjcofv; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY compound_link
    ADD CONSTRAINT fk_4m4vppgc58ard1xyn3ywjcofv FOREIGN KEY (compound_id) REFERENCES compound(id);


--
-- Name: fk_9rlsrdmnocx95om3pj7hg493u; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY name
    ADD CONSTRAINT fk_9rlsrdmnocx95om3pj7hg493u FOREIGN KEY (score_id) REFERENCES score(id);


--
-- Name: fk_9tyub4p8nwgystu2dand4eqf0; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY compound_link
    ADD CONSTRAINT fk_9tyub4p8nwgystu2dand4eqf0 FOREIGN KEY (spectrum_id) REFERENCES spectrum(id);


--
-- Name: fk_ngyyvop8lg8e4j55ftcldatf0; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY query
    ADD CONSTRAINT fk_ngyyvop8lg8e4j55ftcldatf0 FOREIGN KEY (query_export_id) REFERENCES spectrum_query_download(id);


--
-- Name: fkb9613d4630c0abcc; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY impact
    ADD CONSTRAINT fkb9613d4630c0abcc FOREIGN KEY (score_id) REFERENCES score(id);


--
-- Name: fkc03c8da482bd7c98; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY meta_data
    ADD CONSTRAINT fkc03c8da482bd7c98 FOREIGN KEY (category_id) REFERENCES meta_data_category(id);


--
-- Name: fkc05983cb1750468c; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY compound_comment
    ADD CONSTRAINT fkc05983cb1750468c FOREIGN KEY (compound_comments_id) REFERENCES compound(id);


--
-- Name: fkc05983cbfb12c14b; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY compound_comment
    ADD CONSTRAINT fkc05983cbfb12c14b FOREIGN KEY (comment_id) REFERENCES comment(id);


--
-- Name: fkd275597f145793eb; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY tag_link
    ADD CONSTRAINT fkd275597f145793eb FOREIGN KEY (tag_id) REFERENCES tag(id);


--
-- Name: fkd275597f60812749; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY tag_link
    ADD CONSTRAINT fkd275597f60812749 FOREIGN KEY (owner_id) REFERENCES supports_meta_data(id);


--
-- Name: fkdc46c9abfd5f963d; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY compound
    ADD CONSTRAINT fkdc46c9abfd5f963d FOREIGN KEY (id) REFERENCES supports_meta_data(id);


--
-- Name: fke9dd0dec45dd90cb; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY submitter_role
    ADD CONSTRAINT fke9dd0dec45dd90cb FOREIGN KEY (submitter_id) REFERENCES submitter(id);


--
-- Name: fke9dd0decda0db671; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY submitter_role
    ADD CONSTRAINT fke9dd0decda0db671 FOREIGN KEY (role_id) REFERENCES role(id);


--
-- Name: fkfe25b71630c0abcc; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY meta_data_value
    ADD CONSTRAINT fkfe25b71630c0abcc FOREIGN KEY (score_id) REFERENCES score(id);


--
-- Name: fkfe25b71660812749; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY meta_data_value
    ADD CONSTRAINT fkfe25b71660812749 FOREIGN KEY (owner_id) REFERENCES supports_meta_data(id);


--
-- Name: fkfe25b716b98e5f14; Type: FK CONSTRAINT; Schema: public; Owner: mona
--

ALTER TABLE ONLY meta_data_value
    ADD CONSTRAINT fkfe25b716b98e5f14 FOREIGN KEY (meta_data_id) REFERENCES meta_data(id);


--
-- Name: qrtz_blob_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_cron_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simple_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_sched_name_fkey FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details(sched_name, job_name, job_group);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: authentication_token; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE authentication_token FROM PUBLIC;
REVOKE ALL ON TABLE authentication_token FROM mona;
GRANT ALL ON TABLE authentication_token TO mona;


--
-- Name: ion; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE ion FROM PUBLIC;
REVOKE ALL ON TABLE ion FROM mona;
GRANT ALL ON TABLE ion TO mona;


--
-- Name: spectrum; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE spectrum FROM PUBLIC;
REVOKE ALL ON TABLE spectrum FROM mona;
GRANT ALL ON TABLE spectrum TO mona;


--
-- Name: comment; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE comment FROM PUBLIC;
REVOKE ALL ON TABLE comment FROM mona;
GRANT ALL ON TABLE comment TO mona;


--
-- Name: compound; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE compound FROM PUBLIC;
REVOKE ALL ON TABLE compound FROM mona;
GRANT ALL ON TABLE compound TO mona;


--
-- Name: compound_comment; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE compound_comment FROM PUBLIC;
REVOKE ALL ON TABLE compound_comment FROM mona;
GRANT ALL ON TABLE compound_comment TO mona;


--
-- Name: impact; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE impact FROM PUBLIC;
REVOKE ALL ON TABLE impact FROM mona;
GRANT ALL ON TABLE impact TO mona;


--
-- Name: meta_data; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE meta_data FROM PUBLIC;
REVOKE ALL ON TABLE meta_data FROM mona;
GRANT ALL ON TABLE meta_data TO mona;


--
-- Name: meta_data_category; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE meta_data_category FROM PUBLIC;
REVOKE ALL ON TABLE meta_data_category FROM mona;
GRANT ALL ON TABLE meta_data_category TO mona;


--
-- Name: meta_data_value; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE meta_data_value FROM PUBLIC;
REVOKE ALL ON TABLE meta_data_value FROM mona;
GRANT ALL ON TABLE meta_data_value TO mona;


--
-- Name: name; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE name FROM PUBLIC;
REVOKE ALL ON TABLE name FROM mona;
GRANT ALL ON TABLE name TO mona;


--
-- Name: news; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE news FROM PUBLIC;
REVOKE ALL ON TABLE news FROM mona;
GRANT ALL ON TABLE news TO mona;


--
-- Name: qrtz_blob_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_blob_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_blob_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_blob_triggers TO postgres;
GRANT ALL ON TABLE qrtz_blob_triggers TO mona;


--
-- Name: qrtz_calendars; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_calendars FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_calendars FROM postgres;
GRANT ALL ON TABLE qrtz_calendars TO postgres;
GRANT ALL ON TABLE qrtz_calendars TO mona;


--
-- Name: qrtz_cron_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_cron_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_cron_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_cron_triggers TO postgres;
GRANT ALL ON TABLE qrtz_cron_triggers TO mona;


--
-- Name: qrtz_fired_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_fired_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_fired_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_fired_triggers TO postgres;
GRANT ALL ON TABLE qrtz_fired_triggers TO mona;


--
-- Name: qrtz_job_details; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_job_details FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_job_details FROM postgres;
GRANT ALL ON TABLE qrtz_job_details TO postgres;
GRANT ALL ON TABLE qrtz_job_details TO mona;


--
-- Name: qrtz_locks; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_locks FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_locks FROM postgres;
GRANT ALL ON TABLE qrtz_locks TO postgres;
GRANT ALL ON TABLE qrtz_locks TO mona;


--
-- Name: qrtz_paused_trigger_grps; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_paused_trigger_grps FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_paused_trigger_grps FROM postgres;
GRANT ALL ON TABLE qrtz_paused_trigger_grps TO postgres;
GRANT ALL ON TABLE qrtz_paused_trigger_grps TO mona;


--
-- Name: qrtz_scheduler_state; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_scheduler_state FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_scheduler_state FROM postgres;
GRANT ALL ON TABLE qrtz_scheduler_state TO postgres;
GRANT ALL ON TABLE qrtz_scheduler_state TO mona;


--
-- Name: qrtz_simple_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_simple_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_simple_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_simple_triggers TO postgres;
GRANT ALL ON TABLE qrtz_simple_triggers TO mona;


--
-- Name: qrtz_simprop_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_simprop_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_simprop_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_simprop_triggers TO postgres;
GRANT ALL ON TABLE qrtz_simprop_triggers TO mona;


--
-- Name: qrtz_triggers; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE qrtz_triggers FROM PUBLIC;
REVOKE ALL ON TABLE qrtz_triggers FROM postgres;
GRANT ALL ON TABLE qrtz_triggers TO postgres;
GRANT ALL ON TABLE qrtz_triggers TO mona;


--
-- Name: role; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE role FROM PUBLIC;
REVOKE ALL ON TABLE role FROM mona;
GRANT ALL ON TABLE role TO mona;


--
-- Name: score; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE score FROM PUBLIC;
REVOKE ALL ON TABLE score FROM mona;
GRANT ALL ON TABLE score TO mona;


--
-- Name: spectrum_comment; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE spectrum_comment FROM PUBLIC;
REVOKE ALL ON TABLE spectrum_comment FROM mona;
GRANT ALL ON TABLE spectrum_comment TO mona;


--
-- Name: statistics; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE statistics FROM PUBLIC;
REVOKE ALL ON TABLE statistics FROM mona;
GRANT ALL ON TABLE statistics TO mona;


--
-- Name: submitter; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE submitter FROM PUBLIC;
REVOKE ALL ON TABLE submitter FROM mona;
GRANT ALL ON TABLE submitter TO mona;


--
-- Name: submitter_role; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE submitter_role FROM PUBLIC;
REVOKE ALL ON TABLE submitter_role FROM mona;
GRANT ALL ON TABLE submitter_role TO mona;


--
-- Name: supports_meta_data; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE supports_meta_data FROM PUBLIC;
REVOKE ALL ON TABLE supports_meta_data FROM mona;
GRANT ALL ON TABLE supports_meta_data TO mona;


--
-- Name: tag; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE tag FROM PUBLIC;
REVOKE ALL ON TABLE tag FROM mona;
GRANT ALL ON TABLE tag TO mona;


--
-- Name: tag_link; Type: ACL; Schema: public; Owner: mona
--

REVOKE ALL ON TABLE tag_link FROM PUBLIC;
REVOKE ALL ON TABLE tag_link FROM mona;
GRANT ALL ON TABLE tag_link TO mona;


--
-- PostgreSQL database dump complete
--

