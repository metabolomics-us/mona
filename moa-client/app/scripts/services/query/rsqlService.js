/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory("rsqlService", rsqlService);

    /* @ngInject */
    function rsqlService($log, QueryCache) {

        var service = {
            getQuery: getQuery,
            setRsqlQuery: setRsqlQuery,
            filterKeywordSearchOptions: filterKeywordSearchOptions

        };
        return service;


        function filterKeywordSearchOptions(options, instruments, ms, ionMode) {
            // filter compound
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(options.compound.name)) {
                options.compound.inchiKey = options.compound.name;
                delete options.compound.name;
            }
            else {
                delete options.compound.inchiKey;
            }

            // filter exact mass
            if (options.metadata.exactMass === null) {
                delete options.metadata.tolerance;
                delete options.metadata.exactMass;
            }

            // filter instruments
            for (var i = 0; i < instruments.length; i++) {
                var curInstrument = instruments[i];
                for (var j in curInstrument) {
                    if (j !== 'selectAll') {
                        angular.forEach(curInstrument[j], function (value, key) {
                            if (value.selected === true)
                                options.metadata.insType.push(value.name);
                        });
                    }
                }
            }

            angular.forEach(ionMode, function (value, key) {
                if (value.selected === true) {
                    options.metadata.ionMode.push(value.name);
                }
            });

            // add ms type to query
            angular.forEach(ms, function (value, key) {
                if (value.selected === true) {
                    options.metadata.msType.push(value.name);
                }
            });

            options = removeEmptyFields(options);
            setRsqlQuery(options);
            buildRsqlQuery();
            $log.log('filtered object below');
            $log.info(options);
        }

        function removeEmptyFields(options) {
            if (options.metadata.insType.length === 0) {
                delete options.metadata.insType;
            }

            if (options.metadata.msType.length === 0) {
                delete options.metadata.msType;
            }

            if (options.metadata.ionMode.length === 0) {
                delete options.metadata.ionMode;
            }

            return options;
        }

        function buildRsqlQuery() {
            var filtered = getQuery();
            var query = '';

            // build compound string
            // build metadata string
            // build tag string

        }
        function getQuery() {
            return QueryCache.getRsqlQuery();
        }

        function setRsqlQuery(query) {
            QueryCache.setRsqlQuery(query);
        }

        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return rsql query string
         */
        function parseRSQL(query) {

            var compoundQuery = "";
            var metaDataQuery = "";
            var tagsQuery = "";


            if (typeof(query.compound) === 'object' && (JSON.stringify(query.compound) !== JSON.stringify({}))) {
                compoundQuery = buildCompoundQueryString(query.compound);
            }

            if (typeof(query.metaData) === 'object' && (JSON.stringify(query.metaData) !== JSON.stringify({}))) {
                metaDataQuery = buildMetaDataQueryString(query.metaData);

            }

            if (typeof(query.tags) !== 'undefined' && query.tags.length !== 0) {
                tagsQuery = buildTagsQueryString(query.tags);
            }

            var compiledQuery = "";

            if (compoundQuery !== '') {
                compiledQuery += compoundQuery;
            }

            (metaDataQuery !== '') ? compiledQuery !== '' ? compiledQuery += ' and ' + metaDataQuery : compiledQuery = metaDataQuery
                : compiledQuery;

            (tagsQuery !== '') ? compiledQuery !== '' ? compiledQuery += ' and ' + tagsQuery : compiledQuery = tagsQuery
                : compiledQuery;

            return compiledQuery;

        }

        function buildTagsQueryString(tagQuery) {
            var queryString = "";
            for (var i = 0, l = tagQuery.length; i < l; i++) {
                if (i > 0) {
                    queryString += ' and ';
                }
                queryString += "tags=q='name.eq==" + tagQuery[i].name.eq + '\"\'';

            }
            return queryString;
        }

        function buildMetaDataQueryString(metaDataQuery) {
            var queryString = "";

            // remove empty fields
            if (metaDataQuery.exactMass === null) {
                delete metaDataQuery.tolerance;
                delete metaDataQuery.exactMass;
            }

            angular.forEach(metaDataQuery, function (value, key) {


            });

            // for(var i in metaDataQuery) {
            //     $log.info(typeof(metaDataQuery[i]));
            //     $log.log(metaDataQuery[i]);
            // }

            // for(var i in metaDataQuery) {
            //     $log.info();
            // }


            /** TODO: this section was built on legacy metadata array. Keeping for reference
             * The searchform is using a metadata object.
             *
             *
             for (var i = 0, l = metaDataQuery.length; i < l; i++) {
                var object = metaDataQuery[i];
                var operator = object.value.eq ? "==" : "!=";

                if (i > 0) {
                    queryString += ' and ';
                }

                queryString += "metaData=q='name" + operator + '\"' + object.value.eq || object.value.ne + '\"\'';
            }
             */
            return queryString;
        }

        function buildCompoundQueryString(compoundQuery) {
            var compound = '';

            // handle compound name
            if (typeof(compoundQuery.name) !== 'undefined') {
                compound += "compound.names=q='name==" + '\"' + compoundQuery.name + '\"\'';

            }

            // handle compound inchiKey
            else if (typeof(compoundQuery.inchiKey) !== 'undefined') {
                compound += "compound=q=inchiKey==" + '\"' + compoundQuery.inchiKey + '\"\'';

            }

            return compound;
        }

    }
})();
