/**
 * Created by sajjan on 12/5/2017.
 */
(function() {
    'use strict';

    compoundConversionService.$inject = ['$log', '$q', '$http', 'gwCtsService', 'gwChemifyService', 'REST_BACKEND_SERVER', 'CTSURL'];
    angular.module('moaClientApp')
        .service('CompoundConversionService', compoundConversionService);

    /* @ngInject */
    function compoundConversionService($log, $q, $http, gwCtsService, gwChemifyService, REST_BACKEND_SERVER, CTSURL) {

        /**
		 * Converts the given name to an InChIKey via Chemify
         */
        this.nameToInChIKey = function(name, callback, errorCallback) {
            gwChemifyService.nameToInChIKey(name, callback, errorCallback);
        };

        /**
		 * Returns high ranking names for given InChIKey from the CTS
         */
        this.InChIKeyToName = function(inchiKey, callback, errorCallback) {
            $http.get(CTSURL +'/rest/convert/InChIKey/Chemical%20Name/'+ inchiKey).then(
                function(response) {
                console.log(response)
                    console.log(response.data.length)
                    console.log(response.data[0].result.length)
                    if (response.data.length > 0 && response.data[0].result.length > 0) {
                        callback(response.data[0].result)
                    } else {
                        errorCallback({status: 200})
                    }
                },
                errorCallback
            );
        };

        /**
		 * Look up the InChI for given InChIKey from the CTS
         */
        this.getInChIByInChIKey = function(inchikey, callback, errorCallback) {
            $http.get(CTSURL +'/rest/convert/InChIKey/InChI%20Code/'+ inchiKey).then(
                function(response) {
                    if (response.data.length > 0 && response.data[0].result.length > 0) {
                        callback(response.data[0].result)
                    } else {
                        errorCallback({status: 200})
                    }
                },
                errorCallback
            );
        };

        /**
		 * Calculate compound summary from SMILES
         */
        this.parseSMILES = function(smiles, callback, errorCallback) {
            $http({
                method: 'POST',
                url: REST_BACKEND_SERVER +'/rest/conversion/smiles',
                data: {string: smiles}
            }).then(callback, errorCallback);
        };

        /**
		 * Calculate compound summary from InChI
         */
        this.parseInChI = function(inchi, callback, errorCallback) {
            $http({
                method: 'POST',
                url: REST_BACKEND_SERVER +'/rest/conversion/inchi',
                data: {string: inchi}
            }).then(callback, errorCallback);
        };

        /**
		 * Calculate compound summary from MOL data
         */
        this.parseMOL = function(mol, callback, errorCallback) {
            $http({
                method: 'POST',
                url: REST_BACKEND_SERVER +'/rest/conversion/mol',
                data: {string: mol}
            }).then(callback, errorCallback);
        };
    }
})();