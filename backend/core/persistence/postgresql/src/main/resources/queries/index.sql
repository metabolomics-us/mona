CREATE INDEX IF NOT EXISTS search_table_mona_id_text ON public.search_table ("mona_id", "text");
CREATE INDEX IF NOT EXISTS spectrum_result_mona_id ON public.spectrum_result ("mona_id");

