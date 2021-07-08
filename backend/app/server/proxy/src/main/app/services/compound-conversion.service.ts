/**
 * Created by sajjan on 12/5/2017.
 */
import {HttpClient} from "@angular/common/http";
import {Inject} from "@angular/core";
import{environment} from "../environments/environment";
import {downgradeInjectable} from "@angular/upgrade/static";
import {CtsService} from "angular-cts-service/dist/cts-lib";
import {CtsConstants} from "angular-cts-service/dist/cts-lib";
import {ChemifyService} from "angular-cts-service/dist/cts-lib";
import {NGXLogger} from "ngx-logger";
import * as angular from 'angular';

export class CompoundConversionService{
    constructor(@Inject([CtsService, CtsConstants, ChemifyService, NGXLogger, HttpClient])
        private ctsService: CtsService, private ctsConstants:CtsConstants, private chemifyService: ChemifyService,
                private logger: NGXLogger, private http: HttpClient) {
    }

    /**
     * Converts the given name to an InChIKey via Chemify
     */
    nameToInChIKey = (name, callback, errorCallback) => {
        this.chemifyService.nameToInChIKey(name, callback, errorCallback);
    };

    /**
     * Returns high ranking names for given InChIKey from the CTS
     */
    InChIKeyToName = (inchiKey, callback, errorCallback) => {
        this.http.get(`${CtsConstants.apiUrl}/rest/convert/InChIKey/Chemical%20Name/${inchiKey}`).subscribe(
            (res) => {
                if (res["data"].length > 0 && res["data"][0].results.length > 0) {
                    callback(res["data"][0].results)
                } else {
                    errorCallback({status: 200})
                }
            },  (error) => {
                errorCallback(error);
            })
    };

    /**
     * Look up the InChI for given InChIKey from the CTS
     */
    getInChIByInChIKey = (inchiKey, callback, errorCallback) => {
        this.http.get(`${CtsConstants.apiUrl}/rest/convert/InChIKey/InChI%20Code/${inchiKey}`).subscribe(
            (response) => {
                if (response["data"].length > 0 && response["data"][0].results.length > 0) {
                    callback(response["data"][0].results)
                } else {
                    errorCallback({status: 200})
                }
            },(error) => {
                errorCallback(error);
        });
    };

    /**
     * Calculate compound summary from SMILES
     */
    parseSMILES = (smiles, callback, errorCallback) => {
        this.http.post(`${environment.REST_BACKEND_SERVER}rest/conversion/smiles`, {string: smiles}).subscribe(
            (res) => {
                callback(res);
            },
            (error) => {
                errorCallback(error);
            });
    };

    /**
     * Calculate compound summary from InChI
     */
    parseInChI = (inchi, callback, errorCallback) => {
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/conversion/inchi`, {string: inchi}).subscribe(
            (res) => {
                callback(res);
            }, (error) => {
                errorCallback(error);
            }
        );
    };

    /**
     * Calculate compound summary from MOL data
     */
    parseMOL = (mol, callback, errorCallback) => {
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/conversion/mol`, {string: mol}).subscribe(
            (res) => {
                callback(res);
            }, (error) => {
                errorCallback(error);
            }
        );
    };

}

angular.module('moaClientApp')
    .factory('CompoundConversionService', downgradeInjectable(CompoundConversionService));


