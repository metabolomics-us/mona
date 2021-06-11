/**
 * Created by wohlgemuth on 6/25/14.
 *
 * handles the upload of library spectra to the system
 */

import * as angular from 'angular';

export class UploadLibraryService{
    private static $inject = ['$rootScope', 'ApplicationError', 'Spectrum', 'gwMspService', 'gwMgfService', 'gwChemifyService', 'AuthenticationService', 'gwCtsService', '$log', '$q', 'gwMassbankService', '$filter', 'AsyncService', 'MetaDataOptimizationService'];
    private $rootScope;
    private ApplicationError;
    private Spectrum;
    private gwMspService;
    private gwMgfService;
    private gwChemifyService;
    private AuthenticationService;
    private gwCtsService;
    private $log
    private $q;
    private gwMassbankService;
    private $filter;
    private AsyncService;
    private MetaDataOptimizationService;
    private completedSpectraCount;
    private failedSpectraCount;
    private uploadedSpectraCount;
    private uploadStartTime;
    private uploadedSpectra;

    constructor($rootScope, ApplicationError, Spectrum, gwMspService, gwMgfService, gwChemifyService, AuthenticationService, gwCtsService, $log, $q, gwMassbankService, $filter, AsyncService, MetaDataOptimizationService){
        this.$rootScope = $rootScope;
        this.ApplicationError = ApplicationError;
        this.Spectrum = Spectrum;
        this.gwMspService = gwMspService;
        this.gwMgfService = gwMgfService;
        this.gwChemifyService = gwChemifyService;
        this.AuthenticationService = AuthenticationService;
        this.gwCtsService = gwCtsService;
        this.$log = $log;
        this.$q = $q;
        this.gwMassbankService = gwMassbankService;
        this.$filter = $filter;
        this.AsyncService = AsyncService;
        this.MetaDataOptimizationService = MetaDataOptimizationService;
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
                this.gwChemifyService.nameToInChIKey(spectra.name, (key) => {
                    if (key === null) {
                        reject("sorry no InChI Key found for " + spectra.name + ", at name to InChI key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        resolve(spectra);
                    }
                });
            }

            //if we have a bunch of names
            else if (spectra.names && spectra.names.length > 0) {

                this.gwChemifyService.nameToInChIKey(spectra.names[0], (key) => {
                    if (key === null) {
                        reject("sorry no InChI Key found for " + spectra.names[0] + ", at names to InChI key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        resolve(spectra);
                    }
                });
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
                this.gwCtsService.convertSmileToInChICode(spectra.smiles, (data) => {
                    spectra.inchi = data.inchicode;
                    spectra.inchiKey = data.inchikey;

                    resolve(spectra);
                });
            }

            //in case we got an inchi
            else if (spectra.inchiKey) {
                this.gwCtsService.convertInchiKeyToMol(spectra.inchiKey, (molecule) => {
                    if (molecule === null && spectra.inchi === null) {
                        resolveByName(spectra, resolve, reject);
                    }
                    else {
                        if (molecule !== null) {
                            spectra.molFile = molecule;
                        }
                        resolve(spectra);
                    }
                });
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
                        this.$log.error("invalid " + this.$filter('json')(spectrumWithKey));
                        reject(new Error('dropped object from submission, since it was declared invalid, it had neither an InChI or a Molfile, which means the provide InChI key most likely was not found!'));
                    }
                }).catch((error) => {
                    this.$log.warn(error + '\n' + this.$filter('json')(spectrumObject));
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
        //$log.debug("submitting spectra...");
        //$log.debug($filter('json')(spectra));

        //$log.debug("additional data...");
        //$log.debug($filter('json')(additionalData));

        //optimize all our metadata
        const myPromise = new Promise((resolve, reject) => {
            this.MetaDataOptimizationService.optimizeMetaData(spectra.meta).then((metaData) => {

                //$log.debug("building final spectra...");
                let s = this.buildSpectrum();

                //assign structure information
                if (spectra.inchiKey !== null)
                    s.biologicalCompound.inchiKey = spectra.inchiKey;
                if (spectra.inchi !== null)
                    s.biologicalCompound.inchi = spectra.inchi;

                if (angular.isDefined(spectra.molFile) && spectra.molFile !== null) {
                    s.biologicalCompound.molFile = spectra.molFile.toString('utf8');
                }

                //assign all the defined names of the spectra
                s.biologicalCompound.names = [];

                if (angular.isDefined(spectra.name)) {
                    if (s.spectrum.name != "")
                        s.biologicalCompound.names.push({name: spectra.name});
                }

                if (angular.isDefined(spectra.names)) {
                    for (let i = 0; i < spectra.names.length; i++) {
                        if (spectra.names[i] != "")
                            s.biologicalCompound.names.push({name: spectra.names[i]});
                    }
                }

                s.biologicalCompound.metaData = [];
                s.biologicalCompound.kind = 'biological';

                s.compound = [s.biologicalCompound];
                s.spectrum = spectra.spectrum;

                if (angular.isDefined(spectra.tags)) {
                    spectra.tags.forEach((tag) => {
                        s.tags.push(tag);
                    });
                }

                // s.comments = [{comment: "this spectra was added to the system, by utilizing a library upload."}];
                // if (angular.isDefined(spectra.comments)) {
                //     s.comments.push({comment: spectra.comments});
                // }

                metaData.forEach((e) => {
                    s.metaData.push(e);
                });

                if (angular.isDefined(additionalData)) {
                    if (angular.isDefined(additionalData.tags)) {
                        additionalData.tags.forEach((tag) => {
                            for (let i = 0; i < s.tags.length; i++) {
                                if (s.tags[i].text === tag.text)
                                    return;
                            }

                            s.tags.push(tag);
                        });
                    }

                    if (angular.isDefined(additionalData.meta)) {
                        additionalData.meta.forEach((e) => {
                            s.metaData.push(e);
                        });
                    }

                    if (angular.isDefined(additionalData.comments)) {
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
        let spectrum = this.Spectrum;
        spectrum.biologicalCompound = {names: []};
        spectrum.tags = [];
        spectrum.metaData = [];

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

            if (angular.isDefined(fireUploadProgress)) {
                fireUploadProgress(100);
            }
        };

        // progress notification
        fileReader.onprogress = (event) => {
            if (event.lengthComputable && angular.isDefined(fireUploadProgress)) {
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
        if (angular.isDefined(origin)) {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                return this.gwMspService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf(".mgf") > 0) {
                return this.gwMgfService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                return this.gwMassbankService.countSpectra(data);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            return this.gwMspService.countSpectra(data);
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
            if (angular.isDefined(origin)) {
                spectrum.meta.push({name: 'origin', value: origin});
            }

            callback(spectrum);
        };

        // Parse data
        if (angular.isDefined(origin)) {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                this.$log.debug("uploading msp file...");
                this.gwMspService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf(".mgf") > 0) {
                this.$log.debug("uploading mgf file...");
                this.gwMgfService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                this.$log.debug("uploading massbank file...");
                this.gwMassbankService.convertFromData(data, addOriginMetadata);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            this.gwMspService.convertFromData(data, addOriginMetadata);
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
        this.AuthenticationService.getCurrentUser().then((submitter) => {
            this.uploadedSpectraCount += 1;

            this.AsyncService.addToPool(() => {
                const myPromise = new Promise((resolve, reject) => {
                    this.workOnSpectra(submitter, saveSpectrumCallback, wizardData, additionalData).then((data) => {
                        resolve(data);
                        this.updateUploadProgress(true);
                    }).catch((error) => {
                        this.$log.error("found an error: " + error);
                        reject(error);
                        this.updateUploadProgress(false);
                    });
                })
                return myPromise;
            });
        });

        this.broadcastUploadProgress();
    };


    /**
     * Checks if spectra are being processed and uploaded
     */
    isUploading = () => {
        return this.completedSpectraCount + this.failedSpectraCount < this.uploadedSpectraCount;
    };


    /**
     * Updates and broadcasts the upload progress
     */
    updateUploadProgress = (success) => {
        if (angular.isUndefined(success)) {
            // do nothing
        } else if (success) {
            this.completedSpectraCount++;
        } else if (!success) {
            this.failedSpectraCount++;
        }

        this.broadcastUploadProgress();
    };

    /**
     * Requires separate function for broadcasting at start of upload
     */
    broadcastUploadProgress = () => {
        this.$rootScope.$broadcast('spectra:uploadprogress', this.completedSpectraCount, this.failedSpectraCount, this.uploadedSpectraCount);
    }
}


angular.module('moaClientApp')
    .service('UploadLibraryService', UploadLibraryService);


