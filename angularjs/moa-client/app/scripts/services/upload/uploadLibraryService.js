/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError, gwMspService, gwChemifyService, AuthentificationService, gwCtsService, $log, $q, $timeout, gwMassbankService,$filter) {

    /**
     * assembles a spectra and prepares it for upload
     * @param origin - where did the object actually come from
     * @param spectra
     * @param submitter
     * @param buildSpectrum
     * @param saveSpectrumCallback
     */
    function workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectra) {

        $log.info('converting object:\n\n' + $filter('json')(spectra));
        //find the inchi key for the given name
        gwChemifyService.nameToInChIKey(spectra.name, function (key) {

            //if a key was found
            if (key != null) {

                //let's get the correct mol file for this key
                gwCtsService.convertInchiKeyToMol(key, function (molFile) {

                    if (molFile != null) {
                        var s = buildSpectrum();

                        s.biologicalCompound.inchiKey = key;

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
                        s.biologicalCompound.molFile = molFile;

                        s.chemicalCompound.inchiKey = key;
                        s.chemicalCompound.molFile = molFile;
                        s.biologicalCompound.metaData = [];

                        s.spectrum = spectra.spectrum;

                        if (spectra.accurate) {
                            s.tags.push({'text': 'accurate'});
                        }
                        s.tags.push({'text': 'imported'});
                        s.tags.push({'text': 'library'});
                        s.tags.push({'text': 'msp'});

                        s.comments = "this spectra was generated using the MSP Service, with an existing uploaded files";
                        spectra.meta.forEach(function (e) {
                            s.metaData.push(e);
                        });

                        //adds a metadata field
                        if (angular.isDefined(origin)) {
                            s.metaData.push({name: 'origin', value: origin});
                        }

                        s.submitter = submitter;

                        saveSpectrumCallback(s);
                    }
                    else {
                        $log.debug('was no able to find a mol file for: ' + key);
                    }
                });
            }
            else {
                $log.debug('was no able to find an InChI Key for: ' + spectra.name);
            }
        });
    }

    /**
     * a simple pool to ensure we are not using more than 'executionLimit' ajax calls while
     * uploading data to the server
     *
     * @param pool
     * @param executeFunction
     * @param executionLimit
     * @param poolRate
     */
    function asyncPool(pool, executeFunction, executionLimit, poolRate) {
        if (angular.isUndefined(executionLimit)) {
            executionLimit = 4;
        }
        if (angular.isUndefined(poolRate)) {
            poolRate = 1500;
        }

        //keeps track of our pool
        var poolRunning = false;

        //how many parallel processes do we want
        var poolLimit = executionLimit;

        //works over the pool
        var handlePool = function () {
            if (poolRunning == false) {
                try {
                    poolRunning = true;
                    for (var i = 0; i < poolLimit; i++) {
                        if (pool.length > 0) {

                            var object = pool[0];
                            pool.splice(0, 1);
                            executeFunction(object);
                        }
                    }
                }
                finally {
                    poolRunning = false;
                    $timeout(handlePool, poolRate);
                }
            }
        };

        $timeout(handlePool, poolRate);
    }

    /**
     * uploads an msp file to the system
     * @param data object containing our data
     * @param buildSpectrum a function to build our spectrum object
     * @param saveSpectrumCallback a function to call to save our generated spectra object
     */
    function uploadMSP(data, buildSpectrum, saveSpectrumCallback, origin) {
        $log.info("uploading msp file: " + origin);

        var pool = [];

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {


            var toWork = (function (spectra) {
                workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectra)
            });

            //convert our files
            var content = gwMspService.convertFromData(data, function (spectra) {
                pool.push(spectra);
            });

            //start the pool
            asyncPool(pool, toWork);

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
        $log.info("uploading mass bank file: " + origin);
        var pool = [];

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {


            var toWork = (function (spectra) {
                workOnSpectra(origin, submitter, buildSpectrum, saveSpectrumCallback, spectra)
            });

            //convert our files
            var content = gwMassbankService.convertFromData(data, function (spectra) {
                pool.push(spectra);
            });

            //start the pool
            asyncPool(pool, toWork);

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

            if (origin.toLowerCase().endsWith(".msp")) {
                uploadMSP(data, buildSpectrum, saveSpectrumCallback, origin);
            }
            else if (origin.toLowerCase().endsWith(".txt")) {
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