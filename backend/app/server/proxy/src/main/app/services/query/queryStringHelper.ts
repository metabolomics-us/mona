/**
 * Service to build query for advance search
 * TODO DELETE
 */

import * as angular from 'angular';

class QueryStringHelper{
    private static $inject = ['$log'];
    private $log;
    private service;

    constructor($log) {
        this.$log = $log;
        this.service = {
            buildCompoundString: this.buildCompoundString,
            buildMetaString: this.buildMetaString,
            buildMeasurementString: this.buildMeasurementString,
            addMetaFilterQueryString: this.addMetaFilterQueryString
        };
    }

    buildCompoundString = (compound) => {
        let query = [];

        if (angular.isDefined(compound)) {
            for (let i = 0; i < compound.length; i++) {
                let curCompound = compound[i];

                for (let key in curCompound) {
                    if (curCompound.hasOwnProperty(key)) {
                        let value = curCompound[key];

                        switch (key) {
                            case 'name':
                                query.push("compound.names=q='name=match=" + '\".*' + value + '.*\"\'');
                                break;
                            case 'inchiKey':
                                query.push("compound.inchiKey==" + value);
                                break;
                            case 'partInchi':
                                query.push("compound.inchiKey=match=\".*" + value + ".*\"");
                                break;
                            case 'match':
                                query.push("compound.classification=q='value=match=" + '\".*' + value + '.*\"\'');
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        return query.length === 0 ? '' : query.length > 1 ? query.join(' or ') : query.join('');
    }

    buildMetaString(metadata, isCompound) {
        let query = [];

        isCompound = isCompound || false;
        if (angular.isDefined(metadata)) {
            for (let i = 0, l = metadata.length; i < l; i++) {
                let meta = metadata[i];
                let op = meta.operator;

                if (angular.isDefined(meta.name) && angular.isDefined(meta.value)) {
                    if (op === 'ne') {
                        query.push("metaData=q='name==\"" + meta.name + "\" and value!=\"" + meta.value + "\"'");
                    }
                    else if (op === 'eq') {
                        if (angular.isDefined(meta.tolerance)) {
                            let leftOffset = parseInt(meta.value) - meta.tolerance;
                            let rightOffset = parseInt(meta.value) + meta.tolerance;
                            query.push("metaData=q='name==\"" + meta.name + "\" and value>=\"" + leftOffset + "\" or value <=\"" + rightOffset + "\"'");

                        } else {
                            query.push("metaData=q='name==\"" + meta.name + "\" and value==\"" + meta.value + "\"'");
                        }
                    }
                    else {
                        query.push("metaData=q='name==\"" + meta.name + "\" and value=match=\".*" + meta.value + ".*\"'");
                    }
                }
            }
        }

        // if it's compound metadata, concat each meta with compound
        if (isCompound) {
            angular.forEach(query,  (elem, index) => {
                query[index] = 'compound.'.concat(elem);
            });
        }

        return query.length === 0 ? '' : query.length > 1 ? query.join(' and ') : query.join('');
    }

    buildMeasurementString(measurement) {
        let query: any;
        if (angular.isDefined(measurement)) {
            query = '';

            for (let i = 0; i < measurement.length; i++) {
                if (measurement[i].hasOwnProperty('exact mass')) {

                    let leftOffset = measurement[i]['exact mass'] - measurement[i + 1].tolerance;
                    let rightOffset = measurement[i]['exact mass'] + measurement[i + 1].tolerance;
                    query += "compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                }
            }
        }
        return query;

    }

    // handles custom groupMeta for Keyword filter
    addMetaFilterQueryString(filterOptions) {
        let filtered = [];
        for (let key in filterOptions) {
            if (filterOptions.hasOwnProperty(key) && filterOptions[key].length > 0) {
                filtered.push(this.addGroupMetaQueryString(key, filterOptions[key]));
            }
        }

        return filtered.length === 0 ? '' : filtered.length > 1 ? filtered.join(' and ') : filtered.join('');
    }

    // helper method for addMetaFilterQueryString
    addGroupMetaQueryString(key, arr) {
        let query = [];

        for (let i = 0, l = arr.length; i < l; i++) {
            query.push("metaData=q='name==\"" + key + "\" and value==\"" + arr[i] + "\"'");
        }

        return '('.concat(query.length > 1 ? query.join(' or ') : query.join(''), ')');
    }
}

angular.module('moaClientApp')
    .service('qStrHelper', QueryStringHelper);

