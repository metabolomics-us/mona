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