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
            buildMetaString: buildMetaString
        };
        return service;

        function buildCompoundString(compound) {
            var query = '';

            if (angular.isDefined(compound)) {
                for (var i = 0; i < compound.length; i++) {
                    var curCompound = compound[i];

                    if (query !== '') {
                        query += ' or ';
                    }

                    for (var key in curCompound) {
                        if (key === 'name') {
                            query += "compound.names=q='name==" + '\"' + curCompound[key] + '\"\'';
                        }
                        else if (key === 'inchiKey') {
                            query += "compound.inchiKey==" + curCompound[key] + "\"";
                        }
                        else {
                            query += "compound.classification=q='value==" + '\"' + curCompound[key] + '\"\'';
                        }
                    }
                }
            }
            return query;
        }

        function buildMetaString(metadata) {
            var query = '';

            if (angular.isDefined(metadata)) {
                for (var i = 0, l = metadata.length; i < l; i++) {
                    var meta = metadata[i];

                    if (angular.isDefined(meta.operator) && angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                        query = query !== '' ? query += ' and ' : query;
                        var op = meta.operator === 'ne' ? '!=' : '==';

                        query += "metaData=q='name==\"" + meta.name + "\" and value" + op + "\"" + meta.value + "\"'";
                    }
                    else {
                        if (angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                            query = query !== '' ? query += ' and ' : query;
                            query += "metaData=q='name==\"" + meta.name + "\" and value==\"" + meta.value + "\"'";
                        }
                    }
                }
            }

            return query;
        }

    }
})();
