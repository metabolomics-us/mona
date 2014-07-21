/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError, gwMspService, gwChemifyService, AuthentificationService, gwCtsService, $log, $q, $timeout, gwMassbankService, $filter, AsyncService, MetaDataOptimizationService) {

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

        //we have an inchi, which is the best
        if (spectra.inchi) {
            gwCtsService.convertInChICodeToMol(spectra.inchi, function (molFile) {
                    spectra.molFile = molFile;
                    deferred.resolve(spectra);
                },
                function (backup) {
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
                });
        }
        //we have an inchi key
        else if (spectra.inchiKey) {
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
     * @param origin - where did the object actually come from
     * @param submitter
     * @param buildSpectrum
     * @param saveSpectrumCallback
     * @param spectraObject
     */
    function workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectraObject) {

        //$log.debug('converting object:\n\n' + $filter('json')(spectra));

        //get the key
        obtainKey(spectraObject).then(function (spectraWithKey) {

            //get the mol file
            obtainMolFile(spectraWithKey).then(function (spectra) {

                //optimize all our metadata
                MetaDataOptimizationService.optimizeMetaData(spectra.meta).then(function (metaData) {

                    var s = buildSpectrum();

                    s.biologicalCompound.inchiKey = spectra.inchiKey;

                    //assign all the defined name of the spectra
                    if (angular.isDefined(spectra.name)) {
                        s.biologicalCompound.names = [

                            {name: spectra.name}
                        ];
                        s.chemicalCompound.names = [
                            {name: spectra.name}
                        ];

                    }
                    //assign all names of the spectra
                    else if (angular.isDefined(spectra.names)) {
                        for (var i = 0; i < spectra.names.length; i++) {
                            s.biologicalCompound.names.push({name: spectra.names[i]})
                            s.chemicalCompound.names.push({name: spectra.names[i]})
                        }
                    }
                    s.biologicalCompound.metaData = [];
                    s.biologicalCompound.molFile = spectra.molFile;

                    s.chemicalCompound.inchiKey = spectra.inchiKey;
                    s.chemicalCompound.molFile = spectra.molFile;
                    s.biologicalCompound.metaData = [];

                    s.spectrum = spectra.spectrum;

                    spectra.tags.forEach(function(e){
                        s.tags.push(e);
                    });

                    if (spectra.accurate) {
                        s.tags.push({'text': 'accurate'});
                    }
                    s.tags.push({'text': 'imported'});
                    s.tags.push({'text': 'library'});

                    s.comments = "this spectra was added to the system, by utilizing a library upload.";
                    metaData.forEach(function (e) {
                        s.metaData.push(e);
                    });

                    //adds a metadata field
                    if (angular.isDefined(origin)) {
                        s.metaData.push({name: 'origin', value: origin});
                    }

                    s.submitter = submitter;

                    saveSpectrumCallback(s);

                });

            });

        }, function (reason) {
            $log.error(reason);
        });
    }


    /**
     * uploads an msp file to the system
     * @param data object containing our data
     * @param buildSpectrum a function to build our spectrum object
     * @param saveSpectrumCallback a function to call to save our generated spectra object
     */
    function uploadMSP(data, buildSpectrum, saveSpectrumCallback, origin) {
        //$log.info("uploading msp file: " + origin);

        var pool = [];

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {


            var toWork = (function (spectra) {
                workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectra)
            });

            //convert our files
            var content = gwMspService.convertFromData(data, function (spectra) {
                AsyncService.addToPool(spectra, toWork);
            });

        });
    }

    /**
     * uploads data using mass bank
     * @param data
     * @param buildSpectrum
     * @param saveSpectrumCallback
     * @param origin
     */
    function uploadMassBank(data, buildSpectrum, saveSpectrumCallback, origin) {
        //$log.info("uploading mass bank file: " + origin);

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {


            var toWork = (function (spectra) {
                workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectra)
            });

            //convert our files
            var content = gwMassbankService.convertFromData(data, function (spectra) {
                AsyncService.addToPool(spectra, toWork);
            });

        });
    }

    /**
     * simples uploader
     * @param data
     * @param buildSpectrum
     * @param saveSpectrumCallback
     * @param origin
     */
    this.upload = function (data, buildSpectrum, saveSpectrumCallback, origin) {
        if (angular.isDefined(origin)) {

            if (origin.toLowerCase().indexOf(".msp") > 0) {
                uploadMSP(data, buildSpectrum, saveSpectrumCallback, origin);
            }
            else if (origin.toLowerCase().indexOf(".txt") > 0) {
                uploadMassBank(data, buildSpectrum, saveSpectrumCallback, origin);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            uploadMSP(data, buildSpectrum, saveSpectrumCallback, orign);
        }
    };

});