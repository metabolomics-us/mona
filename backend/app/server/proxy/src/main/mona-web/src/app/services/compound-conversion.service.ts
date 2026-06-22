/**
 * Created by sajjan on 12/5/2017.
 */
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class CompoundConversionService{
    private apiUrl;
    constructor(public logger: NGXLogger, public http: HttpClient) {
      this.apiUrl = environment.ctsLiteUrl;
    }

    /**
     * Look up the first CTS-Lite match for an InChIKey. CTS-Lite returns a 200 with found_match false when
     * the key is unknown, so a miss surfaces through the error callback like any other failure
     */
    private matchInChIKey(inchiKey, callback, errorCallback) {
      this.http.post(`${this.apiUrl}/match`, {queries: inchiKey}).subscribe(
        (res: any) => {
          if (Array.isArray(res) && res.length > 0 && res[0].found_match && res[0].matches && res[0].matches.length > 0) {
            callback(res[0].matches[0]);
          } else {
            errorCallback({status: 200});
          }
        },
        (error) => errorCallback(error)
      );
    }

    /**
     * Returns the compound name for a given InChIKey from CTS-Lite
     */
    InChIKeyToName(inchiKey, callback, errorCallback) {
        this.matchInChIKey(
          inchiKey,
          (match) => {
            if (match.compound_name) {
              callback([match.compound_name]);
            } else {
              errorCallback({status: 200});
            }
          },
          errorCallback
        );
    }

    /**
     * Look up the InChI for given InChIKey from CTS-Lite
     */
    getInChIByInChIKey(inchiKey, callback, errorCallback) {
        this.matchInChIKey(
          inchiKey,
          (match) => {
            if (match.inchi) {
              callback([match.inchi]);
            } else {
              errorCallback({status: 200});
            }
          },
          errorCallback
        );
    }

    /**
     * Calculate compound summary from SMILES
     */
    parseSMILES(smiles, callback, errorCallback) {
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/conversion/smiles`, {string: smiles}).subscribe(
            (res) => {
                callback(res);
            },
            (error) => {
                errorCallback(error);
            });
    }

    /**
     * Calculate compound summary from InChI
     */
    parseInChI(inchi, callback, errorCallback) {
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/conversion/inchi`, {string: inchi}).subscribe(
            (res) => {
                callback(res);
            }, (error) => {
                errorCallback(error);
            }
        );
    }

    /**
     * Calculate compound summary from MOL data
     */
    parseMOL(mol, callback, errorCallback) {
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/conversion/mol`, {string: mol}).subscribe(
            (res) => {
                callback(res);
            }, (error) => {
                errorCallback(error);
            }
        );
    }

}
