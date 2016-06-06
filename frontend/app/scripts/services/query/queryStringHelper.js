/**

 Service to build query for advance search
 */

(function () {
    'use strict';

    angular.module('moaClientApp')
        .factory('qStrHelper', qStrHelper);

    /* @ngInject */
    function qStrHelper($log) {
        var service = {
            buildCompoundString: buildCompoundString,
            buildMetaString: buildMetaString,
            buildMeasurementString: buildMeasurementString,
            addMetaFilterQueryString: addMetaFilterQueryString
        };
        return service;

        function buildCompoundString(compound) {
            var query = [];

            if (angular.isDefined(compound)) {
                for (var i = 0; i < compound.length; i++) {
                    var curCompound = compound[i];

                    for (var key in curCompound) {
                        if (key === 'name') {
                            query.push("compound.names=q='name=match=" + '\".*' + curCompound[key] + '.*\"\'');
                        }
                        else if (key === 'inchiKey') {
                            query.push("compound.inchiKey==" + curCompound[key] + "\"");
                        }
                        else {
                            query.push("compound.classification=q='value=match=" + '\".*' + curCompound[key] + '.*\"\'');
                        }
                    }
                }
            }
            return query.length === 0 ? '' : query.length > 1 ? query.join(' or ') : query.join('');
        }

        function buildMetaString(metadata, isCompound) {
            var query = [];

            isCompound = isCompound || false;
            if (angular.isDefined(metadata)) {
                for (var i = 0, l = metadata.length; i < l; i++) {
                    var meta = metadata[i];

                    if (angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                        if (angular.isDefined(meta.operator)) {
                            var op = meta.operator === 'ne' ? '!=' : '==';
                            query.push("metaData=q='name==\"" + meta.name + "\" and value" + op + "\"" + meta.value + "\"'");
                        }
                        else {
                            query.push("metaData=q='name==\"" + meta.name + "\" and value==\"" + meta.value + "\"'");
                        }
                    }
                }
            }

            // if it's compound metadata, concat each meta with compound
            if (isCompound) {
                angular.forEach(query, function (elem, index) {
                    query[index] = 'compound.'.concat(elem);
                });
            }

            return query.length === 0 ? '' : query.length > 1 ? query.join(' and ') : query.join('');
        }

        function buildMeasurementString(measurement) {
            if (angular.isDefined(measurement)) {
                var query = '';

                for (var i = 0; i < measurement.length; i++) {
                    if (measurement[i].hasOwnProperty('exact mass')) {

                        var leftOffset = measurement[i]['exact mass'] - measurement[i + 1].tolerance;
                        var rightOffset = measurement[i]['exact mass'] + measurement[i + 1].tolerance;
                        query += "compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                    }
                }
            }
            return query;

        }

        // handles custom groupMeta for Keyword filter
        function addMetaFilterQueryString(filterOptions) {
            var filtered = [];
            for (var key in filterOptions) {
                if (filterOptions.hasOwnProperty(key) && filterOptions[key].length > 0) {
                    filtered.push(addGroupMetaQueryString(key, filterOptions[key]));
                }
            }

            return filtered.length === 0 ? '' : filtered.length > 1 ? filtered.join(' and ') : filtered.join('');
        }

        // helper method for addMetaFilterQueryString
        function addGroupMetaQueryString(key, arr) {
            var query = [];

            for (var i = 0, l = arr.length; i < l; i++) {
                query.push("metaData=q='name==\"" + key + "\" and value==\"" + arr[i] + "\"'");
            }

            return '('.concat(query.length > 1 ? query.join(' or ') : query.join(''), ')');
        }

    }
})();
