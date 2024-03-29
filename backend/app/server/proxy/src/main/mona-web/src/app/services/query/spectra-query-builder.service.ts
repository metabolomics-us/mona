/**
 * Created by wohlgemuth on 7/10/14.
 *
 * a service to build our specific query object to be executed against the Spectrum service,
 * mostly required for the modal query dialog and so kinda special
 */
import { Router } from '@angular/router';
import { NGXLogger } from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class SpectraQueryBuilderService {
    query;
    queryString;
    similarityQuery;

    constructor(public router: Router, public logger: NGXLogger) {
        /**
         * Stored query
         */
        this.query = [];

        /**
         * Stored RSQL query string, only used when the query is set from outside this query builder
         */
        this.queryString = '';

        /**
         * Stored similarity query
         */
        this.similarityQuery = null;
    }

    getQuery() {
        if (this.query == null) {
            this.prepareQuery();
        }

        return this.query;
    }

    setQuery(query) {
        this.query = query;
        this.queryString = '';
    }

    setQueryString(queryString) {
        this.query = [];
        this.queryString = queryString;
    }

    prepareQuery(): void {
        this.logger.debug('Resetting query');

        this.query = [];
        this.queryString = '';
    }

    constructFinalString(): String {
      let finalString = '';
      this.query.forEach((x, index) => {
        if(index < this.query.length - 1) {
          finalString+=`exists(${x}) and `
        } else {
          finalString+=`exists(${x})`
        }
      });
      return finalString;
    }
    /**
     * Generate RSQL query from the query components.  Uses queryString as a base
     * if provided so that a user can start with a predefined or user-specified query
     * and add additional search terms to it.
     */
    getFilter() {
        if (this.queryString === '') {
          return this.constructFinalString();
        } else {
          this.query.concat([this.queryString]);
          return this.constructFinalString();
        }
    }

    reload() {
        const currentUrl = this.router.url;
        this.router.routeReuseStrategy.shouldReuseRoute = () => false;
        this.router.onSameUrlNavigation = 'reload';
        this.router.navigate([currentUrl]).then((res) => {
            this.logger.info('Page reloaded');
        });
    }

    executeQuery(replace = false) {
        const query = this.getFilter();

        if (query !== '') {
            this.logger.info('Executing RSQL query: "' + query + '"');
            this.router.navigate(['/spectra/browse'],
                {queryParams:
                        {
                            query
                        }
                }).then((res) => {
                    this.logger.info('Navigated to /spectra/browse');
            });
        } else {
            this.logger.info(`The current route is: ${this.router.url}`);
            this.logger.debug('Executing empty query');
            this.router.navigate(['/spectra/browse'], {skipLocationChange: false, replaceUrl: true}).then((res) => {
                this.logger.info('Navigated to /spectra/browse with empty params');
              });
        }

        replace = typeof replace !== 'undefined' ? replace : false;

        if (replace) {
            this.router.navigate(['/spectra/browse'],
                {
                    queryParams: {},
                    replaceUrl: true

                }).then((res) => {
                this.logger.info('Replaced /spectra/browse');
            });
        }
    }

    setSimilarityQuery(query) {
        this.similarityQuery = query;
    }

    hasSimilarityQuery()  {
        return this.similarityQuery !== null;
    }

    getSimilarityQuery() {
        return this.similarityQuery;
    }

    /**
     * Build a metadata query, using a recursive approach if dealing with an array of values
     * @param name name of metadata field
     * @param value value(s) to query by
     * @param collection metadata field to query within (e.g. metaData, compound.metaData, compound.classification)
     * @param tolerance tolerance value for floating-point queries
     * @param partialQuery whether to perform a partial string search
     * @returns string metadata query
     */
    buildMetaDataQuery(name, value, collection, tolerance, partialQuery) {
      // Handle array of values
      if (Array.isArray(value)) {
        const subqueries = value.map((x) => {
          return this.buildMetaDataQuery(name, x, collection, tolerance, partialQuery);
        });

        return '(' + subqueries.join(' or ') + ')';
      }

      // Handle individual values
      else {
        if (typeof tolerance !== 'undefined') {
          const leftBoundary = parseFloat(value) - tolerance;
          const rightBoundary = parseFloat(value) + tolerance;

          return collection + '.name:\'' + name + '\' and ' + collection + '.value>:' + leftBoundary + ' and ' + collection +'.value <:' + rightBoundary + '\'';
        } else if (typeof partialQuery !== 'undefined') {
          return collection + '.name:\'' + name + '\' and ' + collection + '.value~\'*' + value + '*\'';
        } else {
          return collection + '.name:\'' + name + '\' and ' + collection + '.value:\'' + value + '\'';
        }
      }
    }

  addMetaDataToQuery(name, value, partialQuery) {
    this.query.push(this.buildMetaDataQuery(name, value, 'metaData', undefined, partialQuery));
  }

  addNumericalMetaDataToQuery(name, value, tolerance) {
    this.query.push(this.buildMetaDataQuery(name, value, 'metaData', tolerance, undefined));
  }

  addCompoundMetaDataToQuery(name, value, partialQuery) {
    this.query.push(this.buildMetaDataQuery(name, value, 'compound.metaData', undefined, partialQuery));
  }

  addNumericalCompoundMetaDataToQuery(name, value, tolerance) {
    this.query.push(this.buildMetaDataQuery(name, value, 'compound.metaData', tolerance, undefined));
  }

  addClassificationToQuery(name, value, partialQuery) {
    this.query.push(this.buildMetaDataQuery(name, value, 'compound.classification', undefined, partialQuery));

  }

  addGeneralClassificationToQuery(value) {
    this.query.push('compound.classification.value:\'' + value + '\'' + ' and compound.classification.name!\'direct parent\' and compound.classification.name!\'alternative parent\'');
  }

  addNameToQuery(name) {
    this.query.push('compound.names.name~\'' + name + '\'');
  }

  buildTagQuery(value, collection, queryType) {
    // Handle array of values
    if (Array.isArray(value)) {
      const subqueries = value.map((x) => {
        return this.buildTagQuery(x, collection, queryType);
      });

      return '(' + subqueries.join(' or ') + ')';
    }

    // Handle individual values
    else {
      if (typeof queryType !== 'undefined' && queryType === 'match') {
        return collection + '.text~\'*' + value + '*\'';
      } else if (typeof queryType !== 'undefined' && queryType === 'ne') {
        return collection + '.text!\'' + value + '\'';
      } else {
        return collection + '.text:\'' + value + '\'';
      }
    }
  }

  addTagToQuery(query, queryType) {
    this.query.push(this.buildTagQuery(query, 'tags', queryType));
  }

  addCompoundTagToQuery(query, queryType) {
    this.query.push(this.buildTagQuery(query, 'compound.tags', queryType));
  }

  addSplashToQuery(query) {
    if (/^(splash[0-9]{2}-[a-z0-9]{4}-[0-9]{10}-[a-z0-9]{20})$/.test(query)) {
      this.query.push('splash.splash:\'' + query + '\'');
    } else if (/^splash[0-9]{2}/.test(query)) {
      this.query.push('splash.splash~\'*' + query + '*\'');
    } else {
      this.query.push('splash.splash~\'*' + query + '*\'');
    }
  }

  addUserToQuery(username): void {
      this.query.push('submitter.emailAddress:\'' + username + '\'');
  }

  addGenericSearch(query): void {
      this.query.push(`metaData.name:\'${query}\' or metaData.value:\'${query}\' or compound.names.name:\'${query}\' or compound.metaData.name:\'${query}\' or compound.metaData.value:\'${query}\'`)
  }
}
