import * as angular from 'angular';

class SimilaritySearchFormDirective {
    constructor() {
        return {
            restrict: 'E',
            templateUrl: '../../views/spectra/query/similaritySearchForm.html',
            controller: SimilaritySearchFormController,
            controllerAs: '$ctrl'
        };
    }
}

class SimilaritySearchFormController {
    private static $inject = ['$scope', '$location', '$log', 'UploadLibraryService', 'SpectraQueryBuilderService'];
    private $scope;
    private $location;
    private $log;
    private UploadLibraryService;
    private SpectraQueryBuilderService;
    private page;
    private pasteError;
    private spectrum;
    private uploadError;

    constructor($scope, $location, $log, UploadLibraryService, SpectraQueryBuilderService) {
        this.$scope = $scope;
        this.$location = $location;
        this.$log = $log;
        this.UploadLibraryService = UploadLibraryService;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    $onInit = () => {
        this.page = 0;
    }

    parsePastedSpectrum = (spectrum) => {
        this.pasteError = null;

        if (spectrum == null || spectrum == "") {
            this.pasteError = 'Please input a valid spectrum!'
        } else if (spectrum.match(/([0-9]*\.?[0-9]+)\s*:\s*([0-9]*\.?[0-9]+)/g)) {
            this.spectrum = spectrum;
            this.page = 2;
        } else if (spectrum.match(/([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?/g)) {
            spectrum = spectrum.split(/[\n\s]+/);

            if (spectrum.length % 2 == 0) {
                this.spectrum = [];

                for (let i = 0; i < spectrum.length / 2; i++) {
                    this.spectrum.push(spectrum[2 * i] +':'+ spectrum[2 * i + 1]);
                }

                this.spectrum = this.spectrum.join(' ');
                this.page = 2;
            } else {
                this.pasteError = 'Spectrum does not have complete ion/intensity pairs!'
            }
        } else {
            this.pasteError = 'Unrecognized spectrum format!'
        }
    };

    /**
     * Parse spectra
     * @param files
     */
    parseFiles = (files) => {
        this.page = 1;
        this.spectrum = null;
        this.uploadError = null;

        this.UploadLibraryService.loadSpectraFile(files[0],
            (data, origin) => {
                this.UploadLibraryService.processData(data, (spectrum) => {
                    this.$scope.$apply(() => {
                        // Create list of ions
                        this.spectrum = spectrum.spectrum;
                        this.page = 2;
                    });
                }, origin);
            },
            (progress) => {
                if (progress == 100) {
                    this.$scope.$apply(() => {
                        if (this.spectrum == null) {
                            this.page = 0;
                            this.uploadError = 'Unable to load spectra!';
                        } else {
                            this.page = 2;
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
    search = (minSimilarity, precursorMZ, precursorMZTolerance, precursorToleranceUnit) => {
        let request = {
            spectrum: this.spectrum,
            minSimilarity: 500,
            precursorMZ: null,
            precursorTolerancePPM: null,
            precursorToleranceDa: null
        };

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

        this.$log.info("Submitting similarity request: "+ JSON.stringify(request));

        this.SpectraQueryBuilderService.setSimilarityQuery(request);
        this.$location.path('/spectra/similaritySearch');
    };

}

angular.module('moaClientApp')
    .directive('similaritySearchForm', SimilaritySearchFormDirective);
