/**
 * Created by sajjan on 4/20/15.
 */
'use strict';

moaControllers.CleanSpectraDataController = function ($scope, $window, $location, UploadLibraryService, gwCtsService) {
    // Loaded spectra data/status
    $scope.spectraLoaded = 0;
    $scope.currentSpectrum;
    $scope.spectra = [];
    $scope.spectrumErrors = {};
    $scope.spectraIndex = 0;

    // Parameters provided for trimming spectra
    $scope.ionCuts = {};


    /*
     * Handle switching between spectra
     */
    var setSpectrum = function(index) {
        $scope.spectraIndex = index;
        $scope.currentSpectrum = $scope.spectra[$scope.spectraIndex];
        $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;
    };

    $scope.previousSpectrum = function() {
        setSpectrum(($scope.spectraIndex + $scope.spectra.length - 1) % $scope.spectra.length);
    };

    $scope.nextSpectrum = function() {
        setSpectrum(($scope.spectraIndex + $scope.spectra.length + 1) % $scope.spectra.length);
    };

    $scope.removeCurrentSpectrum = function() {
        $scope.spectra.splice($scope.spectraIndex, 1);

        if($scope.spectra.length == 0) {
            $scope.resetFile();
        } else if ($scope.spectraIndex == $scope.spectra.length) {
            setSpectrum($scope.spectraIndex - 1);
        } else {
            setSpectrum($scope.spectraIndex);
        }
    };

    $scope.resetFile = function() {
        $scope.spectraLoaded = 0;
        $scope.spectraIndex = 0;
        $scope.spectra = [];

        // Scroll to top of the page
        $window.scrollTo(0, 0);
    };


    /**
     * Sort order for the ion table - default m/z ascending
     */
    $scope.ionTableSort = 'ion';
    $scope.ionTableSortReverse = false;

    $scope.sortIonTable = function (column) {
        if (column == 'ion') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '+ion') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '+ion';
        }
        else if (column == 'intensity') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '-intensity') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '-intensity';
        }
        else if (column == 'annotation') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '-annotation') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '-annotation';
        }
    };


    /**
     * Perform mass spectrum trimming
     */

    $scope.performIonCuts = function(index) {
        if (angular.isUndefined(index)) {
            index = $scope.spectraIndex;
        }

        var cutIons = [];
        var retainedIons = [];

        var limit = 0;

        if (angular.isDefined($scope.ionCuts.absAbundance)) {
            limit = $scope.ionCuts.absAbundance;
        }

        if (angular.isDefined($scope.ionCuts.basePeak)) {
            var basePeakCut = $scope.ionCuts.basePeak * $scope.spectra[index].basePeak / 100
            limit = basePeakCut > limit ? basePeakCut : limit;
        }

        for(var i = 0; i < $scope.spectra[index].ions.length; i++) {
            if($scope.spectra[index].ions[i].intensity < limit) {
                $scope.spectra[index].ions[i].selected = false;
                cutIons.push(i);
            } else {
                retainedIons.push(i);
            }
        }

        if(angular.isDefined($scope.ionCuts.nIons) && retainedIons.length > $scope.ionCuts.nIons) {
            retainedIons.sort(function(a, b) {
                return $scope.spectra[index].ions[b].intensity - $scope.spectra[index].ions[a].intensity;
            });

            for(var i = $scope.ionCuts.nIons; i < retainedIons.length; i++) {
                $scope.spectra[index].ions[retainedIons[i]].selected = false;
            }
        }
    };

    $scope.performAllIonCuts = function() {
        for(var i = 0; i < $scope.spectra.length; i++) {
            $scope.performIonCuts(i);
        }
    };

    $scope.resetIonCuts = function() {
        for(var i = 0; i < $scope.currentSpectrum.ions.length; i++) {
            $scope.currentSpectrum.ions[i].selected = true;
        }
    };


    /**
     * Add a new name to the list
     */

    $scope.addName = function() {
        $scope.currentSpectrum.names.push('');
    };


    /**
     * Handle metadata functionality
     */

    $scope.addMetadataField = function() {
        $scope.currentSpectrum.meta.push({name: '', value: ''});
        $scope.$apply();
        $('#metadata_editor').scrollTop($('#metadata_editor')[0].scrollHeight);
    };

    $scope.removeMetadataField = function(index) {
        $scope.spectra[$scope.spectraIndex].meta.splice(index, 1);
    };

    $scope.applyMetadataToAll = function(index) {
        var metadata = $scope.currentSpectrum.meta[index];

        for(var i = 0; i < $scope.spectra.length; i++) {
            if(i != $scope.spectraIndex) {
                $scope.spectra[i].meta.push(metadata);
            }
        }
    };


    /**
     * Parse spectra
     * @param files
     */
    $scope.parseFile = function(files) {
        $scope.spectraLoaded = 1;

        for (var i = 0; i < files.length; i++) {
            UploadLibraryService.loadSpectraFile(files[i],
                function (data, origin) {
                    UploadLibraryService.processData(data, function (spectrum) {
                        // Create list of ions
                        spectrum.basePeak = 0;

                        spectrum.ions = spectrum.spectrum.split(' ').map(function (x) {
                            x = x.split(':');
                            var annotation = '';

                            for (var i = 0; i < spectrum.meta.length; i++) {
                                if (spectrum.meta[i].category == 'annotation' && spectrum.meta[i].value == x[0]) {
                                    annotation = spectrum.meta[i].name;
                                }
                            }

                            var intensity = parseFloat(x[1]);

                            if (intensity > spectrum.basePeak) {
                                spectrum.basePeak = intensity;
                            }

                            return {
                                ion: parseFloat(x[0]),
                                intensity: intensity,
                                annotation: annotation,
                                selected: true
                            }
                        });

                        // Remove annotations and origin from metadata
                        spectrum.meta = spectrum.meta.filter(function(metadata) {
                            if (metadata.name == 'origin') {
                                spectrum.origin = metadata.name;
                                return false;
                            } else {
                                return angular.isUndefined(metadata.category) || metadata.category != 'annotation';
                            }
                        });

                        // Add an empty metadata field if none exist
                        if (spectrum.meta.length == 0) {
                            spectrum.meta.push({name: '', value: ''});
                        }

                        $scope.$apply(function () {
                            $scope.spectra.push(spectrum);
                            $scope.spectraLoaded = 2;
                            setSpectrum(0);
                        });
                    }, origin);
                },
                function (progress) {}
            );
        }
    };


    /**
     * Handle MOL file input
     */
    $scope.parseMolFile = function(file) {
        if (file.length > 0) {
            var fileReader = new FileReader();

            fileReader.onload = function (event) {
                var data = event.target.result;
                console.log(data);

                // Accept only the first MOL file
                var sep1 = data.indexOf('$$$$'), sep2 = data.indexOf('M  END');
                console.log(sep1 +" "+ sep2)

                if (sep1 > -1 || sep2 > -1) {
                    if (sep1 == -1 || (sep1 > -1 && sep2 > -1 && sep1 > sep2)) {
                        sep1 = sep2;
                    }

                    data = data.substring(0, sep1);
                }

                $scope.currentSpectrum.molFile = data;
                $scope.$apply();
            };

            fileReader.readAsText(file[0]);
        }
    };

    $scope.convertMolToInChI = function() {
        if (angular.isDefined($scope.currentSpectrum.molFile) && $scope.currentSpectrum.molFile != '') {
            gwCtsService.convertToInchiKey($scope.currentSpectrum.molFile, function (result) {
                console.log(result);
                $scope.currentSpectrum.inchiKey = result.inchikey;
            });
        }
    };


    /**
     * Export a file as MSP
     */
    $scope.exportFile = function() {
        var msp = '';

        for (var i = 0; i < $scope.spectra.length; i++) {
            // Add separator
            if (i > 0) {
                msp += '\n\n';
            }

            // Add names
            msp += 'Name: '+ ($scope.spectra[i].names.length == 0 ? 'Unknown' : $scope.spectra[i].names[0]) +'\n';

            if (angular.isDefined($scope.spectra[i].inchiKey) && $scope.spectra[i].inchiKey != '') {
                msp += 'InChIKey: '+ $scope.spectra[i].inchiKey +'\n';
            }

            if (angular.isDefined($scope.spectra[i].inchi) && $scope.spectra[i].inchi != '') {
                msp += 'InChI: '+ $scope.spectra[i].inchi +'\n';
            }

            if (angular.isDefined($scope.spectra[i].smiles) && $scope.spectra[i].smiles != '') {
                msp += 'SMILES: '+ $scope.spectra[i].smiles +'\n';
            }

            for (var j = 1; j < $scope.spectra[i].names.length; j++) {
                msp += 'Synon: '+ $scope.spectra[i].names[j] +'\n';
            }

            // Add metadata
            for (var j = 0; j < $scope.spectra[i].meta.length; j++) {
                msp += $scope.spectra[i].meta[j].name +': '+ $scope.spectra[i].meta[j].value +'\n';
            }

            // Add mass spectrum
            var ions = [];

            for (var j = 0; j < $scope.spectra[i].ions.length; j++) {
                if ($scope.spectra[i].ions[j].selected) {
                    ions.push($scope.spectra[i].ions[j]);
                }
            }

            ions.sort(function(a, b) {
                return a[0] - b[0];
            });

            msp += 'Num Peaks: '+ ions.length +'\n';

            for (var j = 0; j < ions.length; j++) {
                msp += ions[j].ion +' '+ ions[j].intensity + (ions[j].annotation != '' ? ' '+ ions[j].annotation : '') +'\n';
            }
        }

        // Export file
        // http://stackoverflow.com/a/18197341/406772
        var pom = document.createElement('a');
        pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(msp));
        pom.setAttribute('download', 'export.msp');
        pom.style.display = 'none';

        document.body.appendChild(pom);
        pom.click();
        document.body.removeChild(pom);
    };


    /**
     *
     */
    $scope.waitForLogin = function() {
        $scope.$on('auth:login-success', function(event, data, status, headers, config) {
            if ($scope.spectraLoaded == 2) {
                $scope.uploadFile();
            }
        });
    };


    /**
     * Upload current data
     */
    var validateSpectra = function() {
        var invalid = [];

        for(var i = 0; i < $scope.spectra.length; i++) {
            $scope.spectra[i].errors = [];

            var ionCount = 0;

            for (var j = 0; j < $scope.spectra[i].ions.length; j++) {
                if ($scope.spectra[i].ions[j].selected) {
                    ionCount++;
                }
            }

            if (ionCount == 0) {
                $scope.spectra[i].errors.push('This spectrum has no selected ions!  It cannot be uploaded.');
            }

            if ((angular.isUndefined($scope.spectra[i].inchi) || $scope.spectra[i].inchi == '') &&
                    (angular.isUndefined($scope.spectra[i].molFile) || $scope.spectra[i].molFile == '')) {
                $scope.spectra[i].errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
            }


            if ($scope.spectra[i].errors.length > 0) {
                invalid.push(i);
            }
        }

        if (invalid.length > 0) {
            setSpectrum(invalid[0]);
            $scope.error = 'There are some errors in the data you have provided.  The';
            $window.scrollTo(0, 0);
        }

        return (invalid.length == 0);
    };


    $scope.uploadFile = function() {
        if(validateSpectra()) {
            // Reset the spectrum count if necessary
            if(!UploadLibraryService.isUploading()) {
                UploadLibraryService.completedSpectraCount = 0;
                UploadLibraryService.failedSpectraCount = 0;
                UploadLibraryService.uploadedSpectraCount = 0;
                UploadLibraryService.uploadStartTime = new Date().getTime();
            }

            UploadLibraryService.uploadSpectra($scope.spectra, function (spectrum) {
                spectrum.$batchSave();
            }, $scope.spectrum);

            $location.path('/uploadstatus');
        }
    };
};