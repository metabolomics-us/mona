package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum;

import java.util.List;

/**
 * Custom repository fragment for keyset (cursor) based export paging
 */
public interface SpectrumRepositoryCustom {

    /**
     * Returns up to limit spectra for the given filter query, ordered by id descending, starting
     * after lastId (exclusive). Pass null lastId for the first page. Unlike Page based paging this
     * issues no per-page count query and no growing offset, so it stays fast at the end of a large
     * export
     *
     * @param query  the RSQL filter, or null/empty for all spectra
     * @param lastId id of the last spectrum already returned, or null for the first page
     * @param limit  maximum number of spectra to return
     * @return the next keyset page of spectra
     */
    List<Spectrum> findForExport(String query, String lastId, int limit);

    /**
     * Case-insensitive contains search over spectrum metadata values, compound names and compound
     * metadata values. Each branch resolves matching spectrum ids through its own trigram index
     * and the branches are UNIONed, unlike the generic filter path whose OR across joined tables
     * cannot use any index. Results are ordered by id descending
     *
     * @param keyword the raw search term, matched as a substring
     * @param page    zero based page number
     * @param size    page size
     * @return the requested page of matching spectra
     */
    List<Spectrum> findByKeyword(String keyword, int page, int size);

    /**
     * Count of spectra matched by findByKeyword for the given keyword
     *
     * @param keyword the raw search term, matched as a substring
     * @return number of distinct matching spectra
     */
    long countByKeyword(String keyword);
}
