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
            prepareQuery: prepareQuery,
            getQuery: getQuery,
            setRsqlQuery: setRsqlQuery,
            addCompound: addCompound,
            addInstrumentTypes: addInstrumentTypes


        };
        return service;

        function prepareQuery() {
            return {
                firstOperand: 'AND',
                secondOperand: 'AND',
                compound: {
                    name: '',
                    inchiKey: null
                },
                metaData: {
                    insType: [],
                    msType: [],
                    ionMode: [],
                    exactMass: null,
                    tolerance: 0.5
                }
            };
        }


        function addInstrumentTypes(instruments) {
            //get query
            var query = getQuery();

            // init instrument [] if there's none
            if(typeof(query.metadata.insType) === 'undefined') {
                query.metadata.insType = [];
            }
            // loop through and add selected instruments
            for (var i = 0; i < instruments.length; i++) {
                var curInstrument = instruments[i];
                for (var j in curInstrument) {
                    if (j !== 'selectAll') {
                        angular.forEach(curInstrument[j], function (value, key) {
                            if (value.selected === true)
                                query.metadata.insType.push(value.name);
                        });
                    }
                }
            }

            // if there's no selected instruments, remove instruments from metadata
            if (query.metadata.insType.length === 0) {
                delete query.metadata.insType;
            }

            setRsqlQuery(query);
        }

        function addCompound(compoundOptions) {
            var query = getQuery();

            // filter inChiKey or compound name
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(compoundOptions.name)) {
                query.compound.inchiKey = compoundOptions.name;
                delete query.compound.name;
            }
            else {
                delete query.compound.inchiKey;
            }
            setRsqlQuery(query);
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

            if(compoundQuery !== '') {
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

            angular.forEach(metaDataQuery, function(value, key) {

                $log.info(value);
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

            $log.info(compound);
            return compound;
        }

    }
})();
