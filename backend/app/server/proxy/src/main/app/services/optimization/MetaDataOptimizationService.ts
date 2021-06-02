/**
 * Created by wohlgemuth on 7/18/14.
 */


/**
 *
 * TODO move to rule system on the server side
 *
 * general service to optimize metadata and take care of some formating issues
 */

import * as angular from 'angular';

class MetaDataOptimizationService{
    private static $inject = ['ApplicationError', '$log', '$q', '$timeout', '$filter'];
    private ApplicationError;
    private $log;
    private $q;
    private $timeout;
    private $filter;
    private regexNumeric;

    constructor(ApplicationError, $log, $q, $timeout, $filter) {
        this.ApplicationError = ApplicationError;
        this.$log = $log;
        this.$q = $q;
        this.$timeout = $timeout;
        this.$filter = $filter;
    }

    $onInit = () => {
        /**
         * numeric value
         * @type {RegExp}
         */
        this.regexNumeric = /([0-9]+\.?[0-9]+)/;
    }

    /** TODO: complete implementation and uncomment
     * converts our retention time to seconds
     * @param metadata
     * @returns {*}

     function convertRetentionTimeToSeconds(metadata) {

            /**
     * regular expression to find regex metadata field
     * @type {number}

     var regex = /retention[ -_]?time/i;

     /**
     * retetnion with minutes
     * @type {RegExp}

     var regexMinutes = /([0-9]+\.?[0-9]+).*min/;

     /**
     * retention time with seconds
     * @type {RegExp}

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
     }*/

    /**
     * converts the name and category
     * @param metadata
     */
     convertName = (metadata) => {
        if (metadata == null || metadata.name == null || metadata.name == "")
            return null;

        metadata.name = metadata.name.replace(/_/g, " ").toLowerCase();

        if (angular.isDefined(metadata.category)) {
            metadata.category = metadata.category.replace(/_/g, " ").toLowerCase();

        }
        return metadata;
    }


    /**TODO: complete implementation and uncommment
     * checks for collision energy and converts it
     * @param metadata
     * @returns {*}

     function convertUnits(metadata) {

            var regexEv = /^\+?(-?[0-9]+\.?[0-9]+).*ev$/i;
            var regexPercent = /^\+?(-?[0-9]+\.?[0-9]*)\s*\%(?:\s\(nominal\)$)?/i;
            var regexV = /^\+?(-?[0-9]+\.?[0-9]*).*v$/i;
            var regexC = /^\+?(-?[0-9]+\.?[0-9]*).*c$/i;
            var regexKpa = /^\+?(-?[0-9]+\.?[0-9]*).*kpa$/i;
            var regexMa = /^\+?(-?[0-9]+\.?[0-9]*).*mA$/i;
            var regexKv = /^\+?(-?[0-9]+\.?[0-9]*).*kv$/i;
            var regexFlowMinutes = /^(?:add +)?\+?(-?[0-9]+\.?[0-9]*).*ml\/min$/i;
            var regexFlowMicroMinutes = /^(?:add +)?\+?(-?[0-9]+\.?[0-9]*).*ul\/min$/i;
            var regexScanBysec = /^\+?(-?[0-9]+\.?[0-9]*).*sec\/scan.*$/i;


            if (regexEv.test(metadata.value)) {
                metadata.value = regexEv.exec(metadata.value)[1];
                metadata.unit = "eV";
            }
            else if (regexPercent.test(metadata.value)) {
                metadata.value = regexPercent.exec(metadata.value)[1];
                metadata.unit = "%";
            }
            else if (regexV.test(metadata.value)) {
                metadata.value = regexV.exec(metadata.value)[1];
                metadata.unit = "V";
            }
            else if (regexC.test(metadata.value)) {
                metadata.value = regexC.exec(metadata.value)[1];
                metadata.unit = "C";
            }
            else if (regexKpa.test(metadata.value)) {
                metadata.value = regexKpa.exec(metadata.value)[1];
                metadata.unit = "kPa";
            }
            else if (regexMa.test(metadata.value)) {
                metadata.value = regexMa.exec(metadata.value)[1];
                metadata.unit = "mA";
            }
            else if (regexKv.test(metadata.value)) {
                metadata.value = regexKv.exec(metadata.value)[1] * 1000;
                metadata.unit = "V";
            }
            else if (regexFlowMinutes.test(metadata.value)) {
                metadata.value = regexFlowMinutes.exec(metadata.value)[1];
                metadata.unit = "ml/min";
            }
            else if (regexFlowMicroMinutes.test(metadata.value)) {
                metadata.value = regexFlowMicroMinutes.exec(metadata.value)[1] / 1000;
                metadata.unit = "ml/min";
            }
            else if (regexScanBysec.test(metadata.value)) {
                metadata.value = regexScanBysec.exec(metadata.value)[1] / 1000;
                metadata.unit = "sec/scan";
            }

            return metadata;
        }*/

    /**
     * works on the provided metadata array and returns a promise
     * @param metaData
     * @returns {*}
     */
    optimizeMetaData = (metaData) => {
        //$log.debug("optimizing metaData");
        const myPromise = new Promise((resolve, reject) => {
            //build our result object
            let result = [];

            //build the list of values we want to ignore
            for (let i in metaData) {
                let object = metaData[i];

                object = this.convertName(object);
                //object = convertRetentionTimeToSeconds(object);
                //object = convertUnits(object);

                if (object !== null) {
                    result.push(object);
                }
            }
            resolve(result);
        });

        return myPromise;
    };
}

angular.module('moaClientApp')
    .service('MetaDataOptimizationService', MetaDataOptimizationService);

