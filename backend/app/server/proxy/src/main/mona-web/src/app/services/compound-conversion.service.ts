/**
 * Created by sajjan on 12/5/2017.
 */
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {CtsService} from 'angular-cts-service/dist/cts-lib';
import {ChemifyService} from 'angular-cts-service/dist/cts-lib';
import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class CompoundConversionService{
    private apiUrl;
    constructor(public ctsService: CtsService, public chemifyService: ChemifyService,
                public logger: NGXLogger, public http: HttpClient) {
      this.apiUrl = environment.ctsUrl;
    }

    /**
     * Converts the given name to an InChIKey via Chemify
     */
    // OLD FUNCTION THAT NO LONGER WORKED BECAUSE URL CHANGED TO 'oldcts.fiehnlab.ucdavis.edu'
    // nameToInChIKey(name, callback, errorCallback) {
    //     // Now uses oldCtsUrl env variable assigned in main.ts as CtsLibModule.forRoot
    //     this.chemifyService.nameToInChIKey(name, callback, errorCallback);
    // }
    nameToInChIKey(name, callback, errorCallback) {
      const oldCtsUrl = 'http://oldcts.fiehnlab.ucdavis.edu';

      this.http.get(`${oldCtsUrl}/chemify/rest/identify/${name}`)
        .subscribe(
          (res: any) => callback(res[0].result),
          (err) => errorCallback(err)
        );
    }

    /**
     * Returns high ranking names for given InChIKey from the CTS
     */
    InChIKeyToName(inchiKey, callback, errorCallback) {
        this.http.get(`${this.apiUrl}/rest/convert/InChIKey/Chemical%20Name/${inchiKey}`).subscribe(
            (res: any) => {
                if (res.length > 0 && res[0].results.length > 0) {
                    // callback(res[0].results);
                    callback(res[0].results.slice(0, 5));
                } else {
                    errorCallback({status: 200});
                }
            },  (error) => {
                errorCallback(error);
            });
    }

    /**
     * Look up the InChI for given InChIKey from the CTS
     */
    getInChIByInChIKey(inchiKey, callback, errorCallback) {
        this.http.get(`${this.apiUrl}/rest/convert/InChIKey/InChI%20Code/${inchiKey}`).subscribe(
            (response: any) => {
                if (response.length > 0 && response[0].results.length > 0) {
                    callback(response[0].results);
                } else {
                    errorCallback({status: 200});
                }
            }, (error) => {
                errorCallback(error);
        });
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
