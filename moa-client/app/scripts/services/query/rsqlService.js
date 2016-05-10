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

        function getQuery() {
            return QueryCache.getRsqlQuery();
        }

        function setRsqlQuery(query) {
            QueryCache.setRsqlQuery(query);
        }

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
            var compoundQuery = '';
            var metadataQuery = '';


            // build compound string
            if (typeof(filtered.compound) === 'object' && (JSON.stringify(filtered.compound) !== JSON.stringify({}))) {
                compoundQuery = compoundQuery.concat(buildCompoundQueryString(filtered.compound));
            }

            // build metadata string
            if (typeof(filtered.metadata) === 'object' && JSON.stringify(filtered.metadata) !== JSON.stringify({})) {
                metadataQuery = buildMetaDataQueryString(filtered.metadata);
            }
            // build tag string

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

        function buildMetaDataQueryString(metadata) {
            var query = '';

            // handle exact mass & tolerance
            if (typeof(metadata.exactMass) !== 'undefined') {
                // concat first operand
                query = query.concat(' ', filtered.firstOperand.toLowerCase());
                var leftOffset = metadata.exactMass - metadata.tolerance;
                var rightOffset = metadata.exactMass + metadata.tolerance;
                query += " metaData=q='name==exact mass and " + "(value>=" + leftOffset + "or value<=" + rightOffset + ")'";
            }

            // handle formula
            if (typeof(metadata.formula) !== 'undefined' && metadata.formula !== '') {
                // get 2nd operand
            }


            //  for (var i = 0, l = metaDataQuery.length; i < l; i++) {
            //     var object = metaDataQuery[i];
            //     var operator = object.value.eq ? "==" : "!=";
            //
            //     if (i > 0) {
            //         queryString += ' and ';
            //     }
            //
            //     queryString += "metaData=q='name" + operator + '\"' + object.value.eq || object.value.ne + '\"\'';
            // }
            //
            // return queryString;
        }

        function buildCompoundQueryString(compound) {
            var query = '';

            // handle compound name
            if (typeof(compound.name) !== 'undefined') {
                query += "compound.names=q='name==" + '\"' + compound.name + '\"\'';

            }

            // handle compound inchiKey
            else if (typeof(compound.inchiKey) !== 'undefined') {
                query += "compound=q=inchiKey==" + '\"' + compound.inchiKey + '\"\'';

            }

            return query;
        }

    }
})();
