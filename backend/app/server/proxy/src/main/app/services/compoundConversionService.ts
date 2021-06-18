/**
 * Created by sajjan on 12/5/2017.
 */
import * as angular from 'angular';

class CompoundConversionService{
    private static $inject = ['$log', '$http', 'gwCtsService', 'gwChemifyService', 'REST_BACKEND_SERVER', 'CTSURL'];
    private $log;
    private $http;
    private gwCtsService;
    private gwChemifyService;
    private REST_BACKEND_SERVER;
    private CTSURL;

    constructor($log, $http, gwCtsService, gwChemifyService, REST_BACKEND_SERVER, CTSURL) {
        this.$log = $log;
        this.$http = $http;
        this.gwCtsService = gwCtsService;
        this.gwChemifyService = gwChemifyService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.CTSURL = CTSURL;
    }

    /**
     * Converts the given name to an InChIKey via Chemify
     */
    nameToInChIKey = (name, callback, errorCallback) => {
        this.gwChemifyService.nameToInChIKey(name, callback, errorCallback);
    };

    /**
     * Returns high ranking names for given InChIKey from the CTS
     */
    InChIKeyToName = (inchiKey, callback, errorCallback) => {
        this.$http.get(this.CTSURL +'/rest/convert/InChIKey/Chemical%20Name/'+ inchiKey).then(
            (response) => {
                if (response.data.length > 0 && response.data[0].results.length > 0) {
                    callback(response.data[0].results)
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
    getInChIByInChIKey = (inchiKey, callback, errorCallback) => {
        this.$http.get(this.CTSURL +'/rest/convert/InChIKey/InChI%20Code/'+ inchiKey).then(
            (response) => {
                if (response.data.length > 0 && response.data[0].results.length > 0) {
                    callback(response.data[0].results)
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
    parseSMILES = (smiles, callback, errorCallback) => {
        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER +'/rest/conversion/smiles',
            data: {string: smiles}
        }).then(callback, errorCallback);
    };

    /**
     * Calculate compound summary from InChI
     */
    parseInChI = (inchi, callback, errorCallback) => {
        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER +'/rest/conversion/inchi',
            data: {string: inchi}
        }).then(callback, errorCallback);
    };

    /**
     * Calculate compound summary from MOL data
     */
    parseMOL = (mol, callback, errorCallback) => {
        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER +'/rest/conversion/mol',
            data: {string: mol}
        }).then(callback, errorCallback);
    };

}

angular.module('moaClientApp')
    .service('CompoundConversionService', CompoundConversionService);


