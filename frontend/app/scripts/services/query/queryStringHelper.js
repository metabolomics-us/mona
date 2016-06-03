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
            var query = [];

            if (angular.isDefined(compound)) {
                for (var i = 0; i < compound.length; i++) {
                    var curCompound = compound[i];

                    for (var key in curCompound) {
                        if (key === 'name') {
                            query.push("compound.names=q='name==" + '\"' + curCompound[key] + '\"\'');
                        }
                        else if (key === 'inchiKey') {
                            query.push("compound.inchiKey==" + curCompound[key] + "\"");
                        }
                        else {
                            query.push("compound.classification=q='value==" + '\"' + curCompound[key] + '\"\'');
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

                    if (angular.isDefined(meta.operator) && angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                        var op = meta.operator === 'ne' ? '!=' : '==';
                        query.push("metaData=q='name==\"" + meta.name + "\" and value" + op + "\"" + meta.value + "\"'");

                    }
                    else {
                        if (angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                            query.push("metaData=q='name==\"" + meta.name + "\" and value==\"" + meta.value + "\"'");
                        }
                    }
                }
            }

            // if it's compound metadata, concat each meta with compound
            if(isCompound) {
                angular.forEach(query, function(elem,index) {
                   query[index] = 'compound.'.concat(elem);
                });
            }
            
            return query.length === 0 ? '' : query.length > 1 ? query.join(' and ') : query.join('');
        }

    }
})();
