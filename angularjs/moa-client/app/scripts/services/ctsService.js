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
        }
    }
);
