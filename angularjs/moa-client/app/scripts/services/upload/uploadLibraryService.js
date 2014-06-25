/**
 * Created by wohlgemuth on 6/25/14.
 */

/**
 * handles the upload of library spectra to the system
 */
app.service('UploadLibraryService', function (ApplicationError,gwMspService, gwChemifyService,AuthentificationService) {

    /**
     * uploads an msp file to the system
     * @param files object containing our data
     * @param buildSpectrum a function to build our spectrum object
     */
    this.uploadMSP = function (files, buildSpectrum) {

        //get the current user
        AuthentificationService.getCurrentUser().then(function (submitter) {

            //convert our files
            gwMspService.convertFromFile(files, function (spectra) {

                //generate the related inchi key
                gwChemifyService.nameToInChIKey(spectra.name, function (key) {

                    //if a key was found
                    if (key != null) {
                        var s = buildSpectrum();

                        s.biologicalCompound.inchiKey = key;
                        s.chemicalCompound.inchiKey = key;
                        s.biologicalCompound.name = spectra.name;
                        s.chemicalCompound.name = spectra.name;

                        s.spectrum = spectra.spectrum;
                        s.tags.push({'text': 'imported'});
                        s.tags.push({'text': 'library'});
                        s.tags.push({'text': 'msp'});

                        spectra.meta.forEach(function (e) {
                            s.metadata.push(e);
                        });

                        s.submitter = submitter;

                        //push it to the rest service
                        s.$save();
                    }
                });
            });
        });
    }

});