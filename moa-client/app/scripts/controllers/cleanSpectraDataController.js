/**
 * Created by sajjan on 4/20/15.
 */
'use strict';

moaControllers.CleanSpectraDataController = function ($scope, UploadLibraryService) {
    $scope.spectraLoaded = 0;
    $scope.currentSpectrum;
    $scope.spectra = [];
    $scope.spectraIndex = 0;

    $scope.ionCuts = {};

    /*
     * Handle switching between spectra
     */
    $scope.previousSpectrum = function() {
        $scope.spectraIndex = ($scope.spectraIndex + $scope.spectra.length - 1) % $scope.spectra.length;
        $scope.currentSpectrum = $scope.spectra[$scope.spectraIndex];
    };

    $scope.nextSpectrum = function() {
        $scope.spectraIndex = ($scope.spectraIndex + 1) % $scope.spectra.length;
        $scope.currentSpectrum = $scope.spectra[$scope.spectraIndex];
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


    $scope.parseFile = function(files) {
        $scope.spectraLoaded = 1;

        UploadLibraryService.loadSpectraFile(files[0],
            function(data, origin) {
                UploadLibraryService.processData(data, function(spectrum) {
                    // Create list of ions
                    spectrum.basePeak = 0;

                    spectrum.ions = spectrum.spectrum.split(' ').map(function(x) {
                        x = x.split(':');
                        var annotation = '';

                        for (var i = 0; i < spectrum.meta.length; i++) {
                            if (spectrum.meta[i].category == 'annotation' && spectrum.meta[i].value == x[0]) {
                                annotation = spectrum.meta[i].name;
                            }
                        }

                        var intensity = parseFloat(x[1]);

                        if(intensity > spectrum.basePeak) {
                            spectrum.basePeak = intensity;
                        }

                        return {
                            ion: parseFloat(x[0]),
                            intensity: intensity,
                            annotation: annotation,
                            selected: true
                        }
                    });

                    // Add an empty metadata field if none exist
                    if (spectrum.meta.length == 0) {
                        spectrum.meta.push({name: '', value: ''});
                    }

                    $scope.$apply(function() {
                        $scope.spectra.push(spectrum);
                        $scope.currentSpectrum = $scope.spectra[0];
                        $scope.spectraLoaded = 2;
                    });
                }, origin);
            },
            function(progress) {}
        );
    };


    $scope.performIonCuts = function() {
        var cutIons = [];
        var retainedIons = [];

        if(angular.isDefined($scope.ionCuts.basePeak)) {
            var limit = $scope.ionCuts.basePeak * $scope.currentSpectrum.basePeak / 100;

            for(var i = 0; i < $scope.currentSpectrum.ions.length; i++) {
                if($scope.currentSpectrum.ions[i].intensity < limit) {
                    $scope.currentSpectrum.ions[i].selected = false;
                    cutIons.push(i);
                } else {
                    retainedIons.push(i);
                }
            }
        } else {
            for(var i = 0; i < $scope.currentSpectrum.ions.length; i++) {
                retainedIons.push(i);
            }
        }

        if(angular.isDefined($scope.ionCuts.nIons) && $scope.currentSpectrum.ions.length - cutIons.length > $scope.ionCuts.nIons) {
            retainedIons.sort(function(a, b) {
                return $scope.currentSpectrum.ions[b].intensity - $scope.currentSpectrum.ions[a].intensity;
            });

            for(var i = $scope.ionCuts.nIons; i < retainedIons.length; i++) {
                $scope.currentSpectrum.ions[retainedIons[i]].selected = false;
            }
        }
    };

    $scope.resetIonCuts = function() {
        for(var i = 0; i < $scope.currentSpectrum.ions.length; i++) {
            $scope.currentSpectrum.ions[i].selected = true;
        }
    };


    $scope.addName = function() {
        $scope.currentSpectrum.names.push('');
    };

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

    $scope.resetFile = function() {
        $scope.spectraLoaded = 0;
        $scope.spectraIndex = 0;
        $scope.spectra = [];
    }
};