/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError, gwMspService, gwChemifyService, AuthentificationService, gwCtsService, $log, $q) {

    /**
     * uploads an msp file to the system
     * @param files object containing our data
     * @param buildSpectrum a function to build our spectrum object
     * @param saveSpectrumCallback a function to call to save our generated spectra object
     */
    this.uploadMSP = function (files, buildSpectrum, saveSpectrumCallback) {

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {


            //convert our files
            gwMspService.convertFromFile(files, function (spectra) {

                //find the inchi key for the given name
                gwChemifyService.nameToInChIKey(spectra.name, function (key) {

                    $log.debug('received key: ' + key);
                    //if a key was found
                    if (key != null) {

                        //let's get the correct mol file for this key
                        gwCtsService.convertInchiKeyToMol(key, function (molFile) {

                            $log.debug('received mol: ' + molFile);
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


                                $log.debug('saving spectra: ' + s);
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

            });
        });

    };

});