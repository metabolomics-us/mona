(function () {
    'use strict';

    SpectraSimilarityQueryController.$inject = ['$scope', '$location', '$log', 'UploadLibraryService', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .directive('similaritySearchForm', similaritySearchForm);

    function similaritySearchForm() {
        return {
            restrict: 'E',
            templateUrl: 'views/spectra/query/similaritySearchForm.html',
            controller: SpectraSimilarityQueryController
        };
    }

    /* @ngInject */
    function SpectraSimilarityQueryController($scope, $location, $log, UploadLibraryService, SpectraQueryBuilderService) {
        $scope.page = 0;

        $scope.parsePastedSpectrum = function(spectrum) {
            $scope.pasteError = null;

            if (spectrum == null || spectrum == "") {
                $scope.pasteError = 'Please input a valid spectrum!'
            } else if (spectrum.match(/([0-9]*\.?[0-9]+)\s*:\s*([0-9]*\.?[0-9]+)/g)) {
                $scope.spectrum = spectrum;
                $scope.page = 2;
            } else if (spectrum.match(/([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?/g)) {
                spectrum = spectrum.split(/[\n\s]+/);

                if (spectrum.length % 2 == 0) {
                    $scope.spectrum = [];

                    for (var i = 0; i < spectrum.length / 2; i++) {
                        $scope.spectrum.push(spectrum[2 * i] +':'+ spectrum[2 * i + 1]);
                    }

                    $scope.spectrum = $scope.spectrum.join(' ');
                    $scope.page = 2;
                } else {
                    $scope.pasteError = 'Spectrum does not have complete ion/intensity pairs!'
                }
            } else {
                $scope.pasteError = 'Unrecognized spectrum format!'
            }
        };

        /**
         * Parse spectra
         * @param files
         */
        $scope.parseFiles = function(files) {
            $scope.page = 1;
            $scope.spectrum = null;
            $scope.uploadError = null;

            UploadLibraryService.loadSpectraFile(files[0],
                function(data, origin) {
                    UploadLibraryService.processData(data, function(spectrum) {
                        $scope.$apply(function() {
                            // Create list of ions
                            $scope.spectrum = spectrum.spectrum;
                            $scope.page = 2;
                        });
                    }, origin);
                },
                function(progress) {
                    if (progress == 100) {
                        $scope.$apply(function() {
                            if ($scope.spectrum == null) {
                                $scope.page = 0;
                                $scope.uploadError = 'Unable to load spectra!';
                            } else {
                                $scope.page = 2;
                            }
                        });
                    }
                }
            );
        };

        /**
         * Execute similarity search
         * @param minSimilarity
         * @param precursorMZ
         * @param precursorMZTolerance
         * @param precursorToleranceUnit
         */
        $scope.search = function(minSimilarity, precursorMZ, precursorMZTolerance, precursorToleranceUnit) {
            var request = {spectrum: $scope.spectrum};

            if (minSimilarity != null && angular.isNumber(+minSimilarity)) {
                request.minSimilarity = parseFloat(minSimilarity);
            }

            if (precursorMZ != null && angular.isNumber(+precursorMZ)) {
                request.precursorMZ = parseFloat(precursorMZ);
            }

            if (precursorMZTolerance != null && angular.isNumber(+precursorMZTolerance)) {
                if (angular.isUndefined(precursorToleranceUnit) || precursorToleranceUnit == null || precursorMZTolerance == 'PPM') {
                    request.precursorTolerancePPM = parseFloat(precursorMZTolerance);
                }

                if (precursorToleranceUnit == 'Da') {
                    request.precursorToleranceDa = parseFloat(precursorMZTolerance);
                }
            }

            $log.info("Submitting similarity request: "+ JSON.stringify(request));

            SpectraQueryBuilderService.setSimilarityQuery(request);
            $location.path('/spectra/similaritySearch');
        };
    }
})();
