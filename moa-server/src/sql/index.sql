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



 CREATE INDEX "index_supports_metadata_tag_id"
	ON "public"."supports_meta_data_tag"("supports_meta_data_tags_id");


