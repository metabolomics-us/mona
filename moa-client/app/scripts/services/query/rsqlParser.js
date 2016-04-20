/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory(rsqlParser, rsqlParser);

    /* @ngInject */
    function rsqlParser($log) {
        return {parseRSQL: parseRSQL};

        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return query
         */
        function parseRSQL(query) {
            var compoundQuery = "";
            var metaDataQuery = "";
            var tagsQuery = "";

            if (Object.keys(query.compound).length !== 0 && JSON.stringify(query.compound) !== JSON.stringify({})) {
                compoundQuery = buildCompoundQueryString(query.compound);
            }

            if (query.metadata.length !== 0) {
                metaDataQuery = buildMetaDataQueryString(query.metadata);
            }

            if (query.tags.length !== 0) {
                tagsQuery = buildTagsQueryString(query.tags);
            }

            return compoundQuery + ' and ' + metaDataQuery + ' and ' + tagsQuery;

        }

        function buildTagsQueryString(tagQuery) {
            var queryString = "";
            for (var i = 0, l = tagQuery.length; i < l; i++) {
                if (i > 0) {
                    queryString += ' and ';
                }
                queryString += "tags=q='name.eq==" + tagQuery.name.eq + '\"\'';

            }
            return queryString;
        }

        function buildMetaDataQueryString(metaDataQuery) {
            var queryString = "";
            for (var i = 0, l = metaDataQuery.length; i < l; i++) {
                var object = metaDataQuery[i];
                var operator = object.value.eq ? "==" : "!=";

                if (i > 0) {
                    queryString += ' and ';
                }

                queryString += "metaData=q='name" + operator + '\"' + object.value.eq || object.value.ne + '\"\'';
            }
            return queryString;
        }

        function buildCompoundQueryString(compoundQuery) {
            var bio = "";
            var chem = "";

            // handle compound name
            if (typeof(compoundQuery.name) !== 'undefined') {
                bio += "biologicalCompound.names=q='name==" + '\"' + compoundQuery.name + '\"\'';
                chem += "chemicalCompound.names=q='name==" + '\"' + compoundQuery.name + '\"\'';

            }

            // handle compound inchiKey
            if (typeof(compoundQuery.inchiKey) !== 'undefined') {
                bio += " or biologicalCompound=q=inchiKey==" + '\"' + compoundQuery.inchiKey + '\"\'';
                chem += " or chemicalCompound=q=inchiKey==" + '\"' + compoundQuery.inchiKey + '\"\'';
            }
            console.log(bio + ' or ' + chem);
            return bio + ' or ' + chem;
        }

    }
})();
