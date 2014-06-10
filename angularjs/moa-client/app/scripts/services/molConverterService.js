/**
 * Created by Gert on 5/28/2014.
 */

'use strict';

/**
 * this service can be used to convert molecules into different formats
 */
app.service('MolConverter', function ($http, REST_BACKEND_SERVER, $q) {

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
