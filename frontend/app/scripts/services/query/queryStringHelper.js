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
            buildMeasurementString: buildMeasurementString
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

        function buildMeasurementString(measurement, operand, compiled) {
            if(angular.isDefined(measurement) && angular.isDefined(operand) && angular.isDefined(compiled)) {
                // build
                var query = '';

                // handle exact mass & tolerance
                for (var i = 0; i < measurement.length; i++) {

                    if (measurement[i].hasOwnProperty('exact mass')) {
                        // concat first operand
                        query += operand[0];
                        var leftOffset = measurement[i]['exact mass'] - measurement[i + 1].tolerance;
                        var rightOffset = measurement[i]['exact mass'] + measurement[i + 1].tolerance;
                        query += " compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                    }

                    // handle formula
                    if (measurement[i].hasOwnProperty('formula')) {
                        query = query == '' ? query.concat(operand[1]) : query.concat(' ', operand[1]);
                        query += " compound.metaData=q='name==\"formula\" and value==\"" + measurement[i].formula + "\"'";
                    }
                }
                return compiled === '' && query.substring(0, 3) === 'and' ? query.slice(4) :
                    compiled === '' && query.substring(0, 2) === 'or' ? query.slice(3) :
                        compiled.concat(' ', query);

            }

            else {
                return 'invalid number of parameters';
            }
        }

    }
})();
