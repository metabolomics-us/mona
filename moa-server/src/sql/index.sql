--required for internal spectra hashing
CREATE EXTENSION pgcrypto;


CREATE INDEX "metadata_value_metadata_id_index"
	ON "public"."meta_data_value"("meta_data_id");

CREATE INDEX "metadata_value_owner_index"
	ON "public"."meta_data_value"("owner_id");

CREATE INDEX "index-spectra-meta-string"
        ON "public"."meta_data_value"("string_value", "meta_data_id", "owner_id");

CREATE INDEX "index-spectra-meta-double"
        ON "public"."meta_data_value"("double_value", "meta_data_id", "owner_id");

CREATE INDEX "index-spectra-meta-boolean"
        ON "public"."meta_data_value"("boolean_value", "meta_data_id", "owner_id");

CREATE INDEX "index-compound-name"
        ON public.name USING btree (compound_id, name);

CREATE INDEX "index-spectrum-compounds"
        ON "public"."spectrum"("biological_compound_id", "chemical_compound_id");


CREATE INDEX "index-category-name"
        ON "public"."meta_data"("category_id", "name");

CREATE INDEX "index-meta-name"
        ON "public"."meta_data"("name");


CREATE INDEX "news_type_index"
	ON "public"."news"("type");


CREATE INDEX supports_meta_data_tag_index
	ON public.supports_meta_data_tag USING btree (supports_meta_data_tags_id, tag_id);

  CREATE INDEX "ions_spectrum_id_index"
	ON "public"."ion"("spectrum_id");


CREATE INDEX "tag_link_tag"
	ON "public"."tag_link"("tag_id");

CREATE INDEX "tag_link_owner_tag"
	ON "public"."tag_link"("owner_id", "tag_id");

CREATE INDEX "impact_index_score_id_id"
	ON "public"."impact"("score_id","id");

CREATE INDEX "spectrum_index_hash"
	ON "public"."spectrum"("hash");
create index name_index_like_compound on name (compound_id,name text_pattern_ops);

CREATE INDEX tag_link_tag_id
	ON public.tag_link USING btree (tag_id,id)  ;
CREATE INDEX tag_deleted ON tag(text) WHERE text <> 'deleted'     ;

create index index_meta_data_values_deleted on meta_data_value(deleted,string_value,owner_id) where deleted = true and string_value is not null;

create index index_meta_data_values_deleted on meta_data_value(deleted,string_value,owner_id) where deleted = true;

create index splash_block4_index on splash using gist (block4 gist_trgm_ops);

CREATE INDEX "index-spectrum-deleted"
        ON "public"."spectrum"("deleted");






