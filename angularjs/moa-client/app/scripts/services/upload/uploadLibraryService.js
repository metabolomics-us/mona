/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError, gwMspService, gwChemifyService, AuthentificationService, gwCtsService, $log, $q, $timeout) {

    /**
     * uploads an msp file to the system
     * @param files object containing our data
     * @param buildSpectrum a function to build our spectrum object
     * @param saveSpectrumCallback a function to call to save our generated spectra object
     */
    this.uploadMSP = function (files, buildSpectrum, saveSpectrumCallback) {

        var pool = [];

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {

            //convert our files
            gwMspService.convertFromFile(files, function (spectra) {

                pool.push(spectra);

            });


            var handlePool = function () {
                if (pool.length > 0) {
                    for (var i = 0; i < 10; i++) {
                        if (pool.length > 0) {
                            var spectra = pool[0];

                            pool.splice(0, 1);


                            //find the inchi key for the given name
                            gwChemifyService.nameToInChIKey(spectra.name, function (key) {

                                //if a key was found
                                if (key != null) {

                                    //let's get the correct mol file for this key
                                    gwCtsService.convertInchiKeyToMol(key, function (molFile) {

                                        if (molFile != null) {
                                            var s = buildSpectrum();

                                            s.biologicalCompound.inchiKey = key;
                                            s.biologicalCompound.names = [
                                                {name: spectra.name}
                                            ];
                                            s.biologicalCompound.metaData = [];
                                            s.biologicalCompound.molFile = molFile;

                                            s.chemicalCompound.inchiKey = key;
                                            s.chemicalCompound.names = [
                                                {name: spectra.name}
                                            ];
                                            s.chemicalCompound.molFile = molFile;
                                            s.biologicalCompound.metaData = [];

                                            s.spectrum = spectra.spectrum;
                                            s.tags.push({'text': 'imported'});
                                            s.tags.push({'text': 'library'});
                                            s.tags.push({'text': 'msp'});

                                            s.comments = "this spectra was generated using the MSP Service, with an existing uploaded files";
                                            spectra.meta.forEach(function (e) {
                                                s.metaData.push(e);
                                            });

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
                    }
                }
                $timeout(handlePool, 2500);

            };

            $timeout(handlePool, 2500);
        });
    };

});