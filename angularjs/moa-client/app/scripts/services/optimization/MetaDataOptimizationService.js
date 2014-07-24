/**
 * Created by wohlgemuth on 7/18/14.
 */
'use strict';

/**
 * general service to optimize metadata and take care of some formating issues
 */
app.service('MetaDataOptimizationService', function (ApplicationError, $log, $q, $timeout, $filter) {

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

                metadata.unit = "s";
            }
            return metadata;
        }

        /**
         * converts the name and category
         * @param metadata
         */
        function convertName(metadata) {

            metadata.name = metadata.name.replace(/_/g, " ").toLowerCase();

            if (angular.isDefined(metadata.category)) {
                metadata.category = metadata.category.replace(/_/g, " ").toLowerCase();

            }
            return metadata;
        }

        /**
         * converts all the flow rates
         * @param metadata
         * @returns {*}
         */
        function convertFlowRate(metadata) {

            /**
             * regular expression to find regex metadata field
             * @type {number}
             */
            var regex = /flow[ -_]?rate/i;

            /**
             * retetnion with minutes
             * @type {RegExp}
             */
            var regexMinutes = /([0-9]+\.?[0-9]+).*ml\/min/i;

            var regexMicroMinutes = /([0-9]+\.?[0-9]+).*ul\/min/i;

            if (regex.test(metadata.name)) {
                if (regexMinutes.test(metadata.value)) {
                    metadata.value = regexMinutes.exec(metadata.value)[1];
                }
                else if (regexMicroMinutes.test(metadata.value)) {
                    metadata.value = regexMicroMinutes.exec(metadata.value)[1]/1000;
                }

                else {
                    $log.warn("invalid pattern, skipped: " + $filter('json')(metadata));
                }

                metadata.unit = "ml/min";
            }
            return metadata;
        }

        /**
         * checks for collision energy and converts it
         * @param metadata
         * @returns {*}
         */
        function convertCollisionEnergy(metadata) {

            var regex = /([0-9]+\.?[0-9]+).*ev/i;

            if (regex.test(metadata.value)) {
                metadata.value = regex.exec(metadata.value)[1];
                metadata.unit = "eV";
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
                    if (object.name.toLowerCase() === ignore[x].toLowerCase()) {
                        ignored = true;
                    }
                }

                if (ignored === false) {
                    object = convertName(object);
                    object = convertRetentionTimeToSeconds(object);
                    object = convertFlowRate(object);
                    object = convertCollisionEnergy(object);

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