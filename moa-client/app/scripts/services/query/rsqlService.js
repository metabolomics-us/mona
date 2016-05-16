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

            // add ion mode
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

            // remove empty fields
            if (typeof(options.metadata.insType) !== 'undefined' && options.metadata.insType.length === 0) {
                delete options.metadata.insType;
            }

            if (typeof(options.metadata.msType) !== 'undefined' && options.metadata.msType.length === 0) {
                delete options.metadata.msType;
            }

            if (typeof(options.metadata.ionMode) !== 'undefined' && options.metadata.ionMode.length === 0) {
                delete options.metadata.ionMode;
            }

            setRsqlQuery(options);
            buildRsqlQuery();
        }


        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return rsql query string
         */

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

            //TODO build tag string

            var compiledQuery = '';

            // strip leading 'and' if compoundQuery is empty
            compoundQuery === '' ? compiledQuery = metadataQuery.slice(5) :
                compiledQuery = compiledQuery.concat(compoundQuery, metadataQuery);

            // set query in cache
            setRsqlQuery(compiledQuery);
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
                query = query.concat(' ', getQuery().firstOperand.toLowerCase());
                var leftOffset = metadata.exactMass - metadata.tolerance;
                var rightOffset = metadata.exactMass + metadata.tolerance;
                query += " metaData=q='name==\"exact mass\" and " + "(value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\")'";
            }

            // handle formula
            if (typeof(metadata.formula) !== 'undefined' && metadata.formula !== '') {
                var secondOperand = getQuery().secondOperand.toLowerCase();
                query += ' ' + secondOperand + ' ' + "metaData=q='name==\"formula\" and value==\"" + metadata.formula + "\"'";
            }


            // handle instrument Type
            if (typeof(metadata.insType) !== 'undefined' && metadata.insType.length !== 0) {
                for (var i = 0, l = metadata.insType.length; i < l; i++) {
                    query += " and metaData=q='name==\"instrument type\" and value==\"" + metadata.insType[i] + "\"'";
                }
            }

            // handle msType
            if (typeof(metadata.msType) !== 'undefined' && metadata.msType.length !== 0) {
                for (var i = 0, l = metadata.msType.length; i < l; i++) {
                    query += " and metaData=q='name==\"ms type\" and value==\"" + metadata.msType[i] + "\"'";
                }
            }

            // handle ionMode
            if (typeof(metadata.ionMode) !== 'undefined' && metadata.ionMode.length !== 0) {
                for (var i = 0; i < metadata.ionMode.length; i++) {
                    query += " and metaData=q='name==\"ion mode\" and value==\"" + metadata.ionMode[i] + "\"'";
                }
            }
            return query;
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
