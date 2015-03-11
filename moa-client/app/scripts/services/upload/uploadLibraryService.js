/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function ($rootScope, ApplicationError, Spectrum, gwMspService, gwChemifyService, AuthenticationService, gwCtsService, $log, $q, $timeout, gwMassbankService, $filter, AsyncService, MetaDataOptimizationService) {
    // Representing this service
    var self = this;

    // Number of submitted spectra
    self.completedSpectraCount = 0;
    self.uploadedSpectraCount = 0;
    self.uploadStartTime = -1;


    /**
     * obtains a promise for us to get to the an inchi key for a spectra object
     * @param spectra
     * @returns {Deferred}
     */
    function obtainKey(spectra) {
        var deferred = $q.defer();

        /**
         * helper function to resolve the correct inchi by name
         * @param spectra
         */
        var resolveByName = function (spectra) {
            if (spectra.name) {
                gwChemifyService.nameToInChIKey(spectra.name, function (key) {
                    if (key == null) {
                        deferred.reject("sorry no key found, at name to inchi key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        deferred.resolve(spectra);
                    }
                });
            }

            //if we have a bunch of names
            else if (spectra.names && spectra.names.length > 0) {
                gwChemifyService.nameToInChIKey(spectra.names[0], function (key) {
                    if (key == null) {
                        deferred.reject("sorry no key found, at names to inchi key!");
                    }
                    else {
                        spectra.inchiKey = key;
                        deferred.resolve(spectra);
                    }
                });
            }
            //we got nothing so we give up
            else {
                deferred.reject("sorry given object was invalid, we need a name, or an inchi code (inchi) or an array with names for this to work!")
            }
        };

        //if we got an inchi code
        if (spectra.inchi) {
            //no work needed
            deferred.resolve(spectra);

        }
        //in case we got a smile
        else if (spectra.smile) {
            gwCtsService.convertSmileToInChICode(spectra.smile, function (data) {
                spectra.inchi = data.inchicode;
                spectra.inchiKey = data.inchikey;

                deferred.resolve(spectra);
            });
        }
        //in case we got an inchi
        else if (spectra.inchiKey) {
            gwCtsService.convertInchiKeyToMol(spectra.inchiKey, function (molecule) {

                //
                if (molecule == null && spectra.inchi == null) {
                    resolveByName(spectra);
                }
                else {
                    if (molecule != null) {
                        spectra.molFile = molecule;
                    }
                    deferred.resolve(spectra);
                }
            });
        }
        else {
            resolveByName(spectra);
        }

        return deferred.promise;
    }

    /**
     * assembles a spectra and prepares it for upload
     * @param submitter
     * @param saveSpectrumCallback
     * @param spectrumObject
     * @param additionalData
     */
    function workOnSpectra(submitter, saveSpectrumCallback, spectrumObject, additionalData) {

        var defer = $q.defer();


        //if we have  a key or an inchi
        if (spectrumObject.inchiKey != null && spectrumObject.inchi != null) {

            self.submitSpectrum(spectrumObject, submitter, saveSpectrumCallback, additionalData).then(function (submittedSpectra) {

                //assign our result
                defer.resolve(submittedSpectra);

            });
        }

        //we need to get a key or inchi code
        else {
            //get the key
            obtainKey(spectrumObject).then(function (spectrumWithKey) {
                //only if we have an inchi or a molfile we can submit this file
                if (spectrumWithKey.inchi != null || spectrumWithKey.molFile != null) {
                    //$log.debug('submitting object:\n\n' + $filter('json')(spectrumWithKey));
                    self.submitSpectrum(spectrumWithKey, submitter, saveSpectrumCallback, additionalData).then(function (submittedSpectra) {

                        defer.resolve(submittedSpectra);

                    });
                }

                else {
                    $log.error("invalid " + $filter('json')(spectrumWithKey));
                    defer.reject(new Error('dropped object from submission, since it was declared invalid, it had neither an InChI or a Molfile, which means the provide InChI key most likely was not found!'));
                }
            }).catch(function (error) {

                $log.warn(error + '\n' + $filter('json')(spectrumObject));
                defer.reject(error);
            });
        }

        //return our promise
        return defer.promise;
    }


    /**
     *
     * @param spectra
     * @param submitter
     * @param saveSpectrumCallback
     * @param additionalData
     */
    self.submitSpectrum = function (spectra, submitter, saveSpectrumCallback, additionalData) {
        $log.debug("submitting spectra...");

        var deferred = $q.defer();

        //optimize all our metadata
        MetaDataOptimizationService.optimizeMetaData(spectra.meta).then(function (metaData) {

            $log.debug("building final spectra...");

            var s = self.buildSpectrum();

            s.biologicalCompound.inchiKey = spectra.inchiKey;
            s.biologicalCompound.inchi = spectra.inchi;

            //assign all the defined name of the spectra
            if (angular.isDefined(spectra.name)) {
                s.biologicalCompound.names = [];
                s.chemicalCompound.names = [];

                s.biologicalCompound.names.push(spectra.name);
                s.chemicalCompound.names.push(spectra.name);

            }

            //assign all names of the spectra
            else if (angular.isDefined(spectra.names)) {
                for (var i = 0; i < spectra.names.length; i++) {
                    s.biologicalCompound.names.push(spectra.names[i]);
                    s.chemicalCompound.names.push(spectra.names[i]);
                }
            }
            s.biologicalCompound.metaData = [];

            s.chemicalCompound.inchiKey = spectra.inchiKey;
            s.chemicalCompound.inchi = spectra.inchi;

            if (spectra.molFile != null) {
                s.chemicalCompound.molFile = spectra.molFile.toString('utf8');
                s.biologicalCompound.molFile = spectra.molFile.toString('utf8');
            }

            s.chemicalCompound.metaData = [];

            s.spectrum = spectra.spectrum;

            if (angular.isDefined(spectra.tags)) {
                spectra.tags.forEach(function (tag) {
                    s.tags.push(tag);
                });
            }

            s.comments = [{comment: "this spectra was added to the system, by utilizing a library upload."}];
            if (angular.isDefined(spectra.comments)) {
                s.comments.push({comment: spectra.comments});
            }

            metaData.forEach(function (e) {
                s.metaData.push(e);
            });

            if (angular.isDefined(additionalData)) {
                if (angular.isDefined(additionalData.tags)) {
                    additionalData.tags.forEach(function (tag) {
                        for (var i = 0; i < s.tags.length; i++) {
                            if (s.tags[i].text == tag.text)
                                return;
                        }

                        s.tags.push(tag);
                    });
                }

                if (angular.isDefined(additionalData.meta)) {
                    additionalData.tags.forEach(function (e) {
                        s.metaData.push(e);
                    });
                }

                if (angular.isDefined(additionalData.comments)) {
                    s.comments.push({comment: additionalData.comments});
                }
            }

            s.submitter = submitter;

            $log.debug("submit to actual server");
            //$log.info($filter('json')(s));
            saveSpectrumCallback(s);


            //assign our result
            deferred.resolve(s);
        });

        return deferred.promise;
    };


    /**
     *
     * @returns {Spectrum}
     */
    self.buildSpectrum = function () {
        var spectrum = new Spectrum();
        spectrum.biologicalCompound = {names: []};
        spectrum.chemicalCompound = {names: []};
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
    self.loadSpectraFile = function (file, callback, fireUploadProgress) {
        var fileReader = new FileReader();

        // Call the callback function with the loaded data once the file has been read
        fileReader.onload = function (event) {
            callback(event.target.result, file.name);

            if (angular.isDefined(fireUploadProgress)) {
                fireUploadProgress(100);
            }
        };

        // progress notification
        fileReader.onprogress = function (event) {
            if (event.lengthComputable && angular.isDefined(fireUploadProgress)) {
                fireUploadProgress(parseInt(((event.loaded / event.total) * 100), 10));
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
    self.countData = function (data, origin) {
        if (angular.isDefined(origin)) {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                return gwMspService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                return gwMassbankService.countSpectra(data);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            return gwMspService.countSpectra(data);
        }
    };

    /**
     *
     * @param data
     * @param callback
     * @param origin
     */
    self.processData = function (data, callback, origin) {
        // Add origin to spectrum metadata before callback
        var addOriginMetadata = function (spectrum) {
            if (angular.isDefined(origin)) {
                spectrum.meta.push({name: 'origin', value: origin});
            }

            callback(spectrum);
        };

        // Parse data
        if (angular.isDefined(origin)) {
            if (origin.toLowerCase().indexOf(".msp") > 0) {
                $log.debug("uploading msp file...");
                gwMspService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                $log.debug("uploading massbank file...");
                gwMassbankService.convertFromData(data, addOriginMetadata);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            gwMspService.convertFromData(data, addOriginMetadata);
        }
    };

    /**
     * simples uploader
     * @param files
     * @param saveSpectrumCallback
     * @param wizardData
     */
    self.uploadSpectra = function (files, saveSpectrumCallback, wizardData) {
        for (var i = 0; i < files.length; i++) {
            self.loadSpectraFile(files[i], function (data, origin) {
                self.processData(data, function (spectrum) {

                    self.uploadSpectrum(spectrum, saveSpectrumCallback)

                }, origin);


            })
        }
    };

    /**
     *
     * @param wizardData
     * @param saveSpectrumCallback
     */
    self.uploadSpectrum = function (wizardData, saveSpectrumCallback) {

        AuthenticationService.getCurrentUser().then(function (submitter) {

            self.uploadedSpectraCount += 1;

            AsyncService.addToPool(function () {
                var defered = $q.defer();

                workOnSpectra(submitter, saveSpectrumCallback, wizardData).then(function (data) {
                    defered.resolve(data);
                    updateUploadProgress();
                }).catch(function (error) {
                    $log.error("found an error: " + error);
                    defered.reject(error);
                    updateUploadProgress();
                });

                return defered.promise;
            });
        });
        broadcastUploadProgress();

    };


    /**
     * Checks if spectra are being processed and uploaded
     */
    self.isUploading = function () {
        return self.completedSpectraCount < self.uploadedSpectraCount;
    };


    /**
     * Updates and broadcasts the upload progress
     */
    var updateUploadProgress = function () {
        self.completedSpectraCount++;
        broadcastUploadProgress();
    };


    /**
     * Requires separate function for broadcasting at start of upload
     */
    var broadcastUploadProgress = function () {
        $rootScope.$broadcast('spectra:uploadprogress', self.completedSpectraCount, self.uploadedSpectraCount);
    }
});