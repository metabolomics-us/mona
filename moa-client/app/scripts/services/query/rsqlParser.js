/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory("rsqlParser", rsqlParser);

    /* @ngInject */
    function rsqlParser($log) {
        return {parseRSQL: parseRSQL};

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

            for(var i in metaDataQuery) {
                $log.info(i);
            }



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
