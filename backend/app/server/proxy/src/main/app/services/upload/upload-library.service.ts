/**
 * Created by wohlgemuth on 6/25/14.
 *
 * handles the upload of library spectra to the system
 */

import {NGXLogger} from "ngx-logger";
import {Spectrum} from "../persistence/spectrum.resource";
import {MspParserLibService} from "angular-msp-parser/dist/msp-parser-lib";
import {MgfParserLibService} from "angular-mgf-parser/dist/mgf-parser-lib";
import {ChemifyService} from "angular-cts-service/dist/cts-lib";
import {CtsService} from "angular-cts-service/dist/cts-lib";
import {AuthenticationService} from "../authentication.service";
import {MassbankParserLibService} from "angular-massbank-parser/dist/massbank-parser-lib";
import {HttpClient} from "@angular/common/http";
import {AsyncService} from "./async.service";
import {MetadataOptimization} from "../optimization/metadata-optimization.service";
import { Subject } from "rxjs";
import {Inject, Injectable} from "@angular/core";
import {downgradeInjectable} from "@angular/upgrade/static";
import * as angular from 'angular';

export class UploadLibraryService{
    public completedSpectraCountSub = new Subject<number>();
    public failedSpectraCountSub = new Subject<number>();
    public uploadedSpectraCountSub = new Subject<number>();

    public completedSpectraCount;
    public failedSpectraCount;
    public uploadedSpectraCount;

    public uploadStartTime;
    public uploadedSpectra;

    constructor(@Inject(NGXLogger) private logger: NGXLogger,
                @Inject(Spectrum) private spectrumService: Spectrum,
                @Inject(MspParserLibService) private mspParserLibService: MspParserLibService,
                @Inject(MgfParserLibService) private mgfParserLibService: MgfParserLibService,
                @Inject(ChemifyService) private chemifyService: ChemifyService,
                @Inject(CtsService) private ctsService: CtsService,
                @Inject(AuthenticationService) private authenticationService: AuthenticationService,
                @Inject(MassbankParserLibService) private massbankParserLibService: MassbankParserLibService,
                @Inject(HttpClient) private http: HttpClient,
                @Inject(AsyncService) private asyncService: AsyncService,
                @Inject(MetadataOptimization) private metadataOptimization: MetadataOptimization){
        this.completedSpectraCount = 0;
        this.failedSpectraCount = 0;
        this.uploadedSpectraCount = 0;
        this.uploadStartTime = -1;
        this.uploadedSpectra = [];
    }

    /**
     * obtains a promise for us to get to the an inchi key for a spectra object
     * @param spectra
     * @returns {Deferred}
     */
     obtainKey(spectra){
        //var deferred = $q.defer();

        /**
         * helper function to resolve the correct inchi by name
         * @param spectra
         */
        let resolveByName = (spectra, resolve, reject) => {
            if (spectra.name) {
                this.chemifyService.nameToInChIKey(spectra.name, (key) => {
                    if (key === null) {
                        reject("sorry no InChI Key found for " + spectra.name + ", at name to InChI key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        resolve(spectra);
                    }
                }, undefined);
            }

            //if we have a bunch of names
            else if (spectra.names && spectra.names.length > 0) {

                this.chemifyService.nameToInChIKey(spectra.names[0], (key) => {
                    if (key === null) {
                        reject("sorry no InChI Key found for " + spectra.names[0] + ", at names to InChI key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        resolve(spectra);
                    }
                }, undefined);
            }

            //we got nothing so we give up
            else {
                reject("sorry given object was invalid, we need a name, or an InChI code, InChI Key or an array with names for this to work!")
            }
        };

        const myPromise = new Promise((resolve, reject) => {
            if (spectra.inchi) {
                //no work needed
                resolve(spectra);
            }
            //in case we got a smiles
            else if (spectra.smiles) {
                this.ctsService.convertSmileToInChICode(spectra.smiles, (data) => {
                    spectra.inchi = data.inchicode;
                    spectra.inchiKey = data.inchikey;

                    resolve(spectra);
                }, undefined);
            }

            //in case we got an inchi
            else if (spectra.inchiKey) {
                this.ctsService.convertInchiKeyToMol(spectra.inchiKey, (molecule) => {
                    if (molecule === null && spectra.inchi === null) {
                        resolveByName(spectra, resolve, reject);
                    }
                    else {
                        if (molecule !== null) {
                            spectra.molFile = molecule;
                        }
                        resolve(spectra);
                    }
                }, undefined);
            }

            else {
                resolveByName(spectra, resolve, reject);
            }
        })

        return myPromise;
    }

    /**
     * assembles a spectra and prepares it for upload
     * @param submitter
     * @param saveSpectrumCallback
     * @param spectrumObject
     * @param additionalData
     */
     workOnSpectra(submitter, saveSpectrumCallback, spectrumObject, additionalData) {

        //var defer = $q.defer();
        const myPromise = new Promise((resolve, reject) => {
            //if we have  a key or an inchi
            if (spectrumObject.inchiKey !== null && spectrumObject.inchi !== null) {
                this.submitSpectrum(spectrumObject, submitter, saveSpectrumCallback, additionalData).then((submittedSpectra) => {
                    //assign our result
                    resolve(submittedSpectra);
                });
            }

            //we need to get a key or inchi code
            else {
                //get the key
                this.obtainKey(spectrumObject).then((spectrumWithKey: any) => {
                    //only if we have an inchi or a molfile we can submit this file
                    if (spectrumWithKey.inchi !== null || spectrumWithKey.molFile !== null) {
                        //$log.debug('submitting object:\n\n' + $filter('json')(spectrumWithKey));
                        this.submitSpectrum(spectrumWithKey, submitter, saveSpectrumCallback, additionalData).then((submittedSpectra) => {
                            resolve(submittedSpectra);
                        });
                    }

                    else {
                        this.logger.error("invalid " + JSON.stringify(spectrumWithKey));
                        reject(new Error('dropped object from submission, since it was declared invalid, it had neither an InChI or a Molfile, which means the provide InChI key most likely was not found!'));
                    }
                }).catch((error) => {
                    this.logger.warn(error + '\n' + JSON.stringify(spectrumObject));
                    reject(error);
                });
            }
        })

        return myPromise;
    }

    /**
     *
     * @param spectra
     * @param submitter
     * @param saveSpectrumCallback
     * @param additionalData
     */
    submitSpectrum = (spectra, submitter, saveSpectrumCallback, additionalData) => {
        //optimize all our metadata
        const myPromise = new Promise((resolve, reject) => {
            this.metadataOptimization.optimizeMetaData(spectra.meta).then((metaData: Object) => {

                console.log('Building Spectra');
                console.log(metaData);
                //$log.debug("building final spectra...");
                let s = this.buildSpectrum();

                //assign structure information
                if (spectra.inchiKey !== null)
                    s.biologicalCompound.inchiKey = spectra.inchiKey;
                if (spectra.inchi !== null)
                    s.biologicalCompound.inchi = spectra.inchi;

                if (typeof spectra.molFile !== 'undefined' && spectra.molFile !== null) {
                    s.biologicalCompound.molFile = spectra.molFile.toString('utf8');
                }

                //assign all the defined names of the spectra
                s.biologicalCompound.names = [];

                if (typeof spectra.name !== 'undefined') {
                    if (s.spectrum.name != "")
                        s.biologicalCompound.names.push({name: spectra.name});
                }

                if (typeof spectra.names !== 'undefined') {
                    for (let i = 0; i < spectra.names.length; i++) {
                        if (spectra.names[i] != "")
                            s.biologicalCompound.names.push({name: spectra.names[i]});
                    }
                }

                s.biologicalCompound.metaData = [];
                s.biologicalCompound.kind = 'biological';

                s.compound = [s.biologicalCompound];
                s.spectrum = spectra.spectrum;

                if (typeof spectra.tags !== 'undefined') {
                    spectra.tags.forEach((tag) => {
                        s.tags.push(tag);
                    });
                }

                // s.comments = [{comment: "this spectra was added to the system, by utilizing a library upload."}];
                // if (angular.isDefined(spectra.comments)) {
                //     s.comments.push({comment: spectra.comments});
                // }

                console.log(metaData[1]);
                Object.keys(metaData).forEach((e) => {
                    s.metaData.push(metaData[e]);
                });
                console.log(s.metaData);

                if (typeof additionalData !== 'undefined') {
                    if (typeof additionalData.tags !== 'undefined') {
                        additionalData.tags.forEach((tag) => {
                            for (let i = 0; i < s.tags.length; i++) {
                                if (s.tags[i].text === tag.text)
                                    return;
                            }

                            s.tags.push(tag);
                        });
                    }

                    if (typeof additionalData.meta !== 'undefined') {
                        additionalData.meta.forEach((e) => {
                            s.metaData.push(e);
                        });
                    }

                    if (typeof additionalData.comments !== 'undefined') {
                        s.comments.push({comment: additionalData.comments});
                    }
                }

                s.submitter = submitter;

                //$log.debug("submit to actual server");
                //$log.debug($filter('json')(s));
                saveSpectrumCallback(s);


                //assign our result
                resolve(s);
            });
        })
        return myPromise;
    };

    /**
     *
     * @returns {Spectrum}
     */
    buildSpectrum = () => {
        let spectrum = {
            biologicalCompound: {names: [],
                inchi: '',
                inchiKey: '',
                molFile: '',
                metaData: [],
                kind: ''
            },
            spectrum: undefined,
            tags: [],
            metaData: [],
            compound: undefined,
            comments: [],
            submitter: ''
        };
        return spectrum;
    };


    /**
     * Loads spectra file and returns the data to a callback function
     * @param file
     * @param callback
     * @param fireUploadProgress
     */
    loadSpectraFile = (file, callback, fireUploadProgress) => {
        let fileReader = new FileReader();

        // Call the callback function with the loaded data once the file has been read
        fileReader.onload = (event) => {
            callback(event.target.result, file.name);

            if (typeof fireUploadProgress !== 'undefined') {
                fireUploadProgress(100);
            }
        };

        // progress notification
        fileReader.onprogress = (event) => {
            if (event.lengthComputable && typeof fireUploadProgress !== 'undefined') {
                fireUploadProgress((event.loaded / event.total) * 100);
            }
        };

        //start the reading
        fileReader.readAsText(file);
    };


    /**
     *
     * @param data
     * @param origin
     * @returns {number}
     */
    countData = (data, origin) => {
        if (typeof origin !== 'undefined') {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                return this.mspParserLibService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf(".mgf") > 0) {
                return this.mspParserLibService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                return this.mspParserLibService.countSpectra(data);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            return this.mspParserLibService.countSpectra(data);
        }
    };

    /**
     *
     * @param data
     * @param callback
     * @param origin
     */
    processData = (data, callback, origin) => {
        // Add origin to spectrum metadata before callback
        let addOriginMetadata = (spectrum) => {
            if (typeof origin !== 'undefined') {
                spectrum.meta.push({name: 'origin', value: origin});
            }

            callback(spectrum);
        };

        // Parse data
        if (typeof origin !== 'undefined') {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                this.logger.debug("uploading msp file...");
                this.mspParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf(".mgf") > 0) {
                this.logger.debug("uploading mgf file...");
                this.mgfParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                this.logger.debug("uploading massbank file...");
                this.massbankParserLibService.convertFromData(data, addOriginMetadata);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            this.mspParserLibService.convertFromData(data, addOriginMetadata);
        }
    };

    /**
     * simples uploader
     * @param files
     * @param saveSpectrumCallback
     * @param wizardData
     */
    uploadSpectraFiles = (files, saveSpectrumCallback, wizardData) => {
        for (let i = 0; i < files.length; i++) {
            this.loadSpectraFile(files[i], (data, origin) => {
                this.processData(data, (spectrum) => {
                    this.uploadSpectrum(spectrum, saveSpectrumCallback, wizardData);
                }, origin);
            }, 0)
        }
    };

    /**
     * @param spectra
     * @param saveSpectrumCallback
     */
    uploadSpectra = (spectra, saveSpectrumCallback) => {
        for (let i = 0; i < spectra.length; i++) {
            this.uploadSpectrum(spectra[i], saveSpectrumCallback, {});
        }
    };

    /**
     *
     * @param wizardData
     * @param saveSpectrumCallback
     * @param additionalData
     */
    uploadSpectrum = (wizardData, saveSpectrumCallback, additionalData) => {
        this.authenticationService.currentUser.subscribe((submitter) => {
            this.uploadedSpectraCount += 1;

            this.asyncService.addToPool(() => {
                const myPromise = new Promise((resolve, reject) => {
                    this.workOnSpectra(submitter, saveSpectrumCallback, wizardData, additionalData).then((data) => {
                        resolve(data);
                        this.updateUploadProgress(true);
                    }).catch((error) => {
                        this.logger.error("found an error: " + error);
                        reject(error);
                        this.updateUploadProgress(false);
                    });
                })
                return myPromise;
            }, undefined);
        });
    };


    /**
     * Checks if spectra are being processed and uploaded
     */
    isUploading(): boolean {
        return this.completedSpectraCount + this.failedSpectraCount < this.uploadedSpectraCount;
    };


    /**
     * Updates and broadcasts the upload progress
     */
    updateUploadProgress = (success) => {
        if (typeof success === 'undefined') {
            // do nothing
        } else if (success) {
            this.completedSpectraCount++;
        } else if (!success) {
            this.failedSpectraCount++;
        }

        //Components will be able to subscribe to these variables to get the counts
        this.completedSpectraCountSub.next(this.completedSpectraCount);
        this.failedSpectraCountSub.next(this.failedSpectraCount);
        this.uploadedSpectraCountSub.next(this.uploadedSpectraCount);
    };
}


angular.module('moaClientApp')
    .factory('UploadLibraryService', downgradeInjectable(UploadLibraryService));


