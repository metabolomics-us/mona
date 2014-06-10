/**
 * Created by Gert on 5/28/2014.
 */

'use strict';

/**
 * provides us with access to the CTS system
 */
app.service('CTSService', function ($http, $q, REST_BACKEND_SERVER) {

        /**
         * returns all the known names for the given inchi key. This will be an array like this {name:value}
         * @param inchiKey
         * @returns {promise}
         */
        this.getNamesForInChIKey = function (inchiKey) {

            var deferred = $q.defer();
            $http.defaults.useXDomain = true;

            $http.get(REST_BACKEND_SERVER + '/rest/util/cts/inchiToName/' + inchiKey
            ).success(function (result) {
                    deferred.resolve(result);
                });

            return deferred.promise;
        };

        /**
         * converts the given molecule to an inchi key
         * @param molecule
         * @returns {*}
         */
        this.convertToInchiKey = function (molecule) {

            var deferred = $q.defer();

            $http.post(REST_BACKEND_SERVER + '/rest/util/converter/molToInchi', {
                    mol: molecule

                }
            ).success(function (result) {
                    deferred.resolve(result);
                });

            return deferred.promise;
        };

        /**
         * attemps to convert the given InChI Key to a mol file
         * @param inchiKey
         * @returns {*}
         */
        this.convertInchiKeyToMol = function (inchiKey) {

            var deferred = $q.defer();

            $http.post(REST_BACKEND_SERVER + '/rest/util/converter/inchiKeyToMol', {

                    inchi: inchiKey


                }
            ).
                success(function (result) {
                    deferred.resolve(result);
                });

            return deferred.promise;
        }

    }
);
