
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
CREATE index name_index_like_compound on name (compound_id,name text_pattern_ops);

CREATE INDEX tag_link_tag_id
    ON public.tag_link USING btree (tag_id,id)  ;
CREATE INDEX tag_deleted ON tag(text) WHERE text <> 'deleted'     ;

CREATE INDEX index_meta_data_values_deleted on meta_data_value(deleted,string_value,owner_id) where deleted = true and string_value is not null;

CREATE INDEX index_meta_data_values_deleted on meta_data_value(deleted,string_value,owner_id) where deleted = true;

CREATE INDEX splash_block2_index on splash using gist (block2 gist_trgm_ops);

CREATE INDEX "index-spectrum-deleted"
        ON "public"."spectrum"("deleted");

CREATE INDEX "metavalue_score_index"
    ON "public"."meta_data_value"("score_id");
CREATE INDEX "spectrum_score_id_index"
    ON "public"."spectrum"("score_id");
CREATE INDEX "spectrum_splash_id_index"
    ON "public"."spectrum"("splash_id");
CREATE INDEX "spectrum_submitter_id_index"
    ON "public"."spectrum"("submitter_id");

CREATE INDEX name_gist_index on name using gist (name gist_trgm_ops);

CREATE INDEX "ion_intensity_mass_index"
    ON "public"."ion"("intensity", "mass");

CREATE INDEX "splash_spectra_index"
    ON "public"."splash"("spectrum_id");