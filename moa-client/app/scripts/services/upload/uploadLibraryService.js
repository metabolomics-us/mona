/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError, Spectrum, gwMspService, gwChemifyService, AuthentificationService, gwCtsService, $log, $q, $timeout, gwMassbankService, $filter, AsyncService, MetaDataOptimizationService) {
    var self = this;

    self.currentUploadProgress = 0;
    self.totalUploadProgress = 0;


    /**
     * obtains a promise for us to get to the an inchi key for a spectra object
     * @param spectra
     * @returns {Deferred}
     */
    function obtainKey(spectra) {
        var deferred = $q.defer();

        //if we got an inchi code
        if (spectra.inchi) {
            gwCtsService.convertInChICodeToKey(spectra.inchi, function (key) {
                if (key == null) {
                    deferred.reject("sorry no key found!");
                }
                else {
                    spectra.inchiKey = key;
                    deferred.resolve(spectra);
                }
            });
        }

        //if we just have a name
        else if (spectra.name) {
            gwChemifyService.nameToInChIKey(spectra.name, function (key) {
                if (key == null) {
                    deferred.reject("sorry no key found!");
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
                    deferred.reject("sorry no key found!");
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

        return deferred.promise;
    }

    /**
     * obtains a mol file for the spectra
     * @param spectra
     */
    function obtainMolFile(spectra) {

        var deferred = $q.defer();

        // define backup conversion using openbabel
        var babelConversion = function (backup) {
            $log.warn('utilizing backup service...');
            gwCtsService.convertInChICodeToMolUsingBabel(spectra.inchi, function (molFile) {
                spectra.molFile = molFile;

                //add a tag to it
                if (angular.isUndefined(spectra.tags)) {
                    spectra.tags = [];
                }

                spectra.tags.push({text: 'not confirmed identification!'});
                deferred.resolve(spectra);
            });
        };

        //we have an inchi, which is the best
        if (spectra.inchi) {
            $log.debug("using InChI code");
            gwCtsService.convertInChICodeToMol(spectra.inchi,
                function (molFile) {

                    if (molFile === null) {
                        $log.debug('did not recive a mole file');
                        babelConversion('cts returned null');
                    } else {
                        spectra.molFile = molFile;
                        deferred.resolve(spectra);
                    }
                },
                babelConversion
            );
        }
        //we have an inchi key
        else if (spectra.inchiKey) {
            $log.info("using InChI key");
            gwCtsService.convertInchiKeyToMol(spectra.inchiKey, function (molFile) {
                spectra.molFile = molFile;
                deferred.resolve(spectra);
            });
        }
        //we are screwed
        else {
            deferred.reject("sorry given object was invalid, we need an inchi code (inchi) or inchi key (key) as property!")
        }

        return deferred.promise;
    }

    /**
     * assembles a spectra and prepares it for upload
     * @param submitter
     * @param saveSpectrumCallback
     * @param spectraObject
     * @param additionalData
     */
    function workOnSpectra(submitter, saveSpectrumCallback, spectraObject, additionalData) {
        $log.debug('converting object:\n\n' + $filter('json')(spectraObject));

        //get the key
        obtainKey(spectraObject).then(function (spectraWithKey) {
            $log.debug('received key...');

            //get the mol file
            obtainMolFile(spectraWithKey).then(function (spectra) {

                $log.debug('received mol file...');

                //optimize all our metadata
                MetaDataOptimizationService.optimizeMetaData(spectra.meta).then(function (metaData) {

                    $log.debug('optimized metadata...');

                    var s = self.buildSpectrum();

                    s.biologicalCompound.inchiKey = spectra.inchiKey;
                    s.biologicalCompound.inchi = spectra.inchi;

                    //assign all the defined name of the spectra
                    if (angular.isDefined(spectra.names)) {
                        s.biologicalCompound.names = spectra.names;
                        s.chemicalCompound.names = spectra.names;
                    }
                    //assign all names of the spectra
                    else if (angular.isDefined(spectra.names)) {
                        for (var i = 0; i < spectra.names.length; i++) {
                            s.biologicalCompound.names.push({name: spectra.names[i]})
                            s.chemicalCompound.names.push({name: spectra.names[i]})
                        }
                    }
                    s.biologicalCompound.metaData = [];
                    s.biologicalCompound.molFile = spectra.molFile.toString('utf8');

                    s.chemicalCompound.inchiKey = spectra.inchiKey;
                    s.chemicalCompound.inchi = spectra.inchi;

                    s.chemicalCompound.molFile = spectra.molFile.toString('utf8');
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

                    if(angular.isDefined(additionalData)) {
                        if(angular.isDefined(additionalData.tags)) {
                            additionalData.tags.forEach(function (tag) {
                                for(var i = 0; i < s.tags.length; i++) {
                                    if(s.tags[i].text == tag.text)
                                        return;
                                }

                                s.tags.push(tag);
                            });
                        }

                        if(angular.isDefined(additionalData.meta)) {
                            additionalData.tags.forEach(function(e) {
                                s.metaData.push(e);
                            });
                        }

                        if(angular.isDefined(additionalData.comments)) {
                            s.comments.push({comment: additionalData.comments});
                        }
                    }

                    s.submitter = submitter;

                    //$log.info($filter('json')(s));
                    saveSpectrumCallback(s);

                });

            });

        }, function (reason) {
            $log.error(reason);
        });
    }


    /**
     *
     * @returns {Spectrum}
     */
    self.buildSpectrum = function() {
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
     */
    self.loadSpectraFile = function(file, callback, fireUploadProgress) {
        var fileReader = new FileReader();
        self.currentUploadProgress = 0;


        // Call the callback function with the loaded data once the file has been read
        fileReader.onload =  function(event) {
            callback(event.target.result, file.name);
            self.currentUploadProgress = 100;

            if(angular.isDefined(fireUploadProgress)) {
                fireUploadProgress(100);
            }
        };

        // progress notification
        fileReader.onprogress = function(event) {
            if(event.lengthComputable) {
                var progress = parseInt(((event.loaded / event.total) * 100), 10);
                self.currentUploadProgress = progress;

                if(angular.isDefined(fireUploadProgress)) {
                    fireUploadProgress(progress);
                }
            }
        };

        //start the reading
        fileReader.readAsText(file);
    };

    /**
     *
     * @param files
     * @param callback
     */
    self.loadSpectraFiles = function(files, callback) {
        self.totalUploadProgress = 0;

        for(var i = 0; i < files.length; i++) {
            self.loadSpectraFile(files[i], callback);
            self.totalUploadProgress = parseInt((((i + 1) / files.length) * 100), 10);
        }
    };


    /**
     *
     * @param data
     * @param origin
     * @returns {number}
     */
    self.countData = function(data, origin) {
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
     * @param buildSpectrum
     * @param callback
     * @param origin
     */
    self.processData = function(data, callback, origin) {
        // Add origin to spectrum metadata before callback
        var addOriginMetadata = function(spectrum) {
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
    self.uploadSpectra = function(files, saveSpectrumCallback, wizardData) {
        AuthentificationService.getCurrentUser().then(function (submitter) {
            var uploadSpectrum = function (file) {
                self.loadSpectraFile(file, function (data, origin) {
                    self.processData(data, function (spectrum) {
                        workOnSpectra(submitter, saveSpectrumCallback, spectrum, wizardData);
                    }, origin);
                })
            };


            for (var i = 0; i < files.length; i++) {
                AsyncService.addToPool(files[i], uploadSpectrum);
            }
        });
    };

    /**
     *
     * @param wizardData
     * @param saveSpectrumCallback
     */
    self.uploadSpectrum = function(wizardData, saveSpectrumCallback) {
        AuthentificationService.getCurrentUser().then(function (submitter) {
            AsyncService.addToPool(wizardData, function (data) {
                workOnSpectra(submitter, saveSpectrumCallback, wizardData);
            });
        });
    }
});
