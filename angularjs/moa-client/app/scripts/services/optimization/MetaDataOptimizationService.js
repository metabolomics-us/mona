/**
 * Created by wohlgemuth on 7/18/14.
 */
'use strict';

/**
 * a simple service to validate metadata and filter objects we don't really want to persist
 */
app.service('MetaDataOptimizationService', function (ApplicationError, $log, $q, $timeout, $filter) {

        /**
         * regular expression to find regex metadata field
         * @type {number}
         */
        var regex = /retention[ -_]?time/i;

        /**
         * retetnion with minutes
         * @type {RegExp}
         */
        var regexMinutes = /([0-9]+\.?[0-9]+).*min/;

        /**
         * retention time with seconds
         * @type {RegExp}
         */
        var regexSeconds = /([0-9]+\.?[0-9]+).*s/;

        /**
         * numeric value
         * @type {RegExp}
         */
        var regexNumeric = /([0-9]+\.?[0-9]+)/;

        /**
         * we want to ignore all of these metadata names
         * @type {Array}
         */
        var ignore = [
            'SCIENTIFIC_NAME',
            'LINEAGE',
            'ACCESSION',
            'SAMPLE',
            'COMPOUND_CLASS',
            'taxonomy',
            'COMMENT',
            'pubchem',
            'chemspider',
            'cas',
            'kegg',
            'knapsack',
            'lipidbank',
            'date',
            'cayman',
            'chebi',
            'hmdb',
            'nikkaji',
            'chempdb'
        ];

        /**
         * converts our retention time to seconds
         * @param metadata
         * @returns {*}
         */
        function convertRetentionTimeToSeconds(metadata) {

            if (regex.test(metadata.name)) {
                if (regexMinutes.test(metadata.value)) {
                    metadata.value = regexMinutes.exec(metadata.value)[1] * 60;
                }

                else if (regexSeconds.test(metadata.value)) {
                    metadata.value = regexSeconds.exec(metadata.value)[1];
                }
                else if (regexNumeric.test(metadata.value)) {
                    metadata.value = regexNumeric.exec(metadata.value)[1];
                    if (metadata.value < 90) {
                        //most likely minutes
                        metadata.value = metadata.value * 60;
                    }
                    else {
                        $log.warn("invalid pattern, skipped: " + $filter('json')(metadata));
                    }
                }
            }
            return metadata;
        }

        /**
         * works on the provided metadata array and returns a promise
         * @param metaData
         * @returns {*}
         */
        this.optimizeMetaData = function (metaData) {
//        $log.debug("optimizing metaData");
            var deferred = $q.defer();

            //build our result object
            var result = [];

            //build the list of values we want to ignore
            for (var i in metaData) {
                var object = metaData[i];
                var ignored = false;

                for (var x in ignore) {
                    if (object.name === ignore[x]) {
                        ignored = true;
                    }
                }

                if (ignored === false) {
                    object = convertRetentionTimeToSeconds(object);

                    if (object != null) {
                        result.push(object);
                    }
                }
                else {
                    //              $log.debug("ignored object: " + $filter("json")(object));
                }
            }

            //$log.debug("result object: " + $filter('json')(result));
            //right now we do nothing, maybe later we do something with this stuff
            deferred.resolve(result);


            return deferred.promise;
        };


    }
);