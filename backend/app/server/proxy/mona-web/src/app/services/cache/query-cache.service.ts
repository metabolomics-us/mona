/**
 * Created by sajjan on 8/21/14.
 *
 * Stores the current query for persistence and updating between views
 */

import {NGXLogger} from 'ngx-logger';
import {SpectraQueryBuilderService} from '../query/spectra-query-builder.service';
import {Injectable} from '@angular/core';

@Injectable()
export class QueryCacheService{
    public query;
    public queryString;


    constructor(public logger: NGXLogger, public spectraQueryBuilderService: SpectraQueryBuilderService) {
      /**
       * Stored query
       */
      this.query = null;

      /**
       * Stored rsqlQueryString
       */
      this.queryString = null;
    }

    /**
     * Clear all values stored in this cache factory
     */
    clear() {
        this.query = null;
        this.queryString = null;
    }

    /**
     * Returns whether a query exists
     * @returns boolean answering if spectra query exists
     */
    hasSpectraQuery() {
        return this.query !== null;
    }

    /**
     * returns our query or creates a default query if it does not exist
     * @returns query
     */
    getSpectraQuery(queryType) {
        // Create default query if none exists
        queryType = queryType || undefined;

        if (this.query === null && this.queryString === null) {
            this.spectraQueryBuilderService.prepareQuery();
        }

        return typeof(queryType) !== 'undefined' && queryType === 'string' ? this.queryString : this.query;
    }

    /**
     * sets a new spectra query
     * @param query string containing search query
     */
    setSpectraQuery(query) {
        // Only used for testing

        this.query = query;
    }

    setSpectraQueryString(queryString) {
        this.queryString = queryString;
    }

    /**
     * Resets the current query
     */
    resetSpectraQuery() {
        this.clear();
    }
}
