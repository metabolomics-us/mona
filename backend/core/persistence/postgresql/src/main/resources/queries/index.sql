create index if not exists search_table_mat_ids_text on public.search_table_mat(mona_id, text);
create index if not exists spectrum_result_id on public.spectrum_result(mona_id);
