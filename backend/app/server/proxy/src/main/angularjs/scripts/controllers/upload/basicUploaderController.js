/**
 * Created by sajjan on 7/13/16.
 */

(function () {
    'use strict';

    basicUploaderController.$inject = ['$scope', '$rootScope', '$window', '$location', 'UploadLibraryService', 'CompoundConversionService', '$q', '$filter', 'AsyncService', '$log', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .controller('BasicUploaderController', basicUploaderController);

    /* @ngInject */
    function basicUploaderController($scope, $rootScope, $window, $location, UploadLibraryService, CompoundConversionService,
                                        $q, $filter, AsyncService, $log, REST_BACKEND_SERVER, $http) {

        $scope.currentSpectrum = null;
        $scope.metadata = {};
        $scope.page = 0;
        $scope.fileHasMultipleSpectra = false;
        $scope.showIonTable = true;


        /**
         * Sort order for the ion table - default m/z ascending
         */
        $scope.ionTableSort = 'ion';
        $scope.ionTableSortReverse = false;

        $scope.sortIonTable = function (column) {
            if (column === 'ion') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '+ion') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '+ion';
            }
            else if (column === 'intensity') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-intensity') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-intensity';
            }
            else if (column === 'relativeIntensity') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-relativeIntensity') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-relativeIntensity';
            }
            else if (column === 'annotation') {
                $scope.ionTableSortReverse = ($scope.ionTableSort === '-annotation') ? !$scope.ionTableSortReverse : false;
                $scope.ionTableSort = '-annotation';
            }
        };


        /**
         * Handle switching pages
         */
        $scope.previousPage = function () {
            $window.scrollTo(0, 0);
            $scope.page--;
        };

        $scope.nextPage = function () {
            $window.scrollTo(0, 0);
            $scope.page++;
        };

        $scope.restart = function () {
            $scope.currentSpectrum = null;
            $scope.page = 0;
            $scope.fileHasMultipleSpectra = false;

            // Scroll to top of the page
            $window.scrollTo(0, 0);
        };


        $scope.parsePastedSpectrum = function(spectrum) {
            $scope.pasteError = null;
            var spectrumString = '';
            var ions = [];
            var basePeakIntensity = 0;

            if (spectrum == null || spectrum == "") {
                $scope.pasteError = 'Please input a valid spectrum!';
            } else if (spectrum.match(/([0-9]*\.?[0-9]+)\s*:\s*([0-9]*\.?[0-9]+)/g)) {
                spectrumString = spectrum;

                ions = spectrum.split(' ').map(function(x) {
                    x = x.split(':');
                    basePeakIntensity = Math.max(basePeakIntensity, parseFloat(x[1]));

                    return {
                        ion: parseFloat(x[0]),
                        intensity: parseFloat(x[1]),
                        ionStr: x[0],
                        intensityStr: x[1],
                        annotation: '',
                        selected: true
                    }
                });
            } else if (spectrum.match(/([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?/g)) {
                spectrum = spectrum.split(/[\n\s]+/);

                if (spectrum.length % 2 == 0) {
                    $scope.spectrum = [];

                    for (var i = 0; i < spectrum.length / 2; i++) {
                        if(spectrumString != '')
                            spectrumString += ' ';
                        spectrumString += spectrum[2 * i] +':'+ spectrum[2 * i + 1];

                        basePeakIntensity = Math.max(basePeakIntensity, parseFloat(spectrum[2 * i + 1]));

                        ions.push({
                            ion: parseFloat(spectrum[2 * i]),
                            intensity: parseFloat(spectrum[2 * i + 1]),
                            ionStr: spectrum[2 * i],
                            intensityStr: spectrum[2 * i + 1],
                            annotation: '',
                            selected: true
                        });
                    }
                } else {
                    $scope.pasteError = 'Spectrum does not have complete ion/intensity pairs!'
                }
            } else {
                $scope.pasteError = 'Unrecognized spectrum format!'
            }


            if ($scope.pasteError === null) {
                ions.forEach(function(x) {
                    x.relativeIntensity = 999 * x.intensity / basePeakIntensity;
                });

                $scope.queryState = 2;
                $scope.spectraCount = 1;
                $scope.page = 2;

                $scope.currentSpectrum = {names: [''], meta: [{}], ions: ions, spectrum: spectrumString};
                $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;
            }
        };


        $scope.resetCompound = function() {
            $scope.currentSpectrum.molFile = '';
            $scope.currentSpectrum.inchi = '';
            $scope.currentSpectrum.inchiKey = '';
            $scope.currentSpectrum.smiles = '';
            $scope.currentSpectrum.names = [''];
        };


        /**
         * Convert an array of names to an InChIKey based on the first result
         * @param names array of compound names
         * @param callback
         */
        var namesToInChIKey = function(names, callback) {
            if (names.length == 0) {
                callback(null);
            } else {
                CompoundConversionService.nameToInChIKey(names[0], function(molecule) {
                    if (molecule !== null) {
                        callback(molecule);
                    } else {
                        namesToInChIKey(names.slice(1), callback);
                    }
                }, function(error) {
                    namesToInChIKey(names.slice(1), callback);
                });
            }
        };

        /**
         * Pull names from CTS given an InChIKey and update the currentSpectrum
         */
        var pullNames = function(inchiKey) {
            // Only pull if there are no names provided
            if ($scope.currentSpectrum.names.length == 0 || ($scope.currentSpectrum.names.length == 1 && $scope.currentSpectrum.names[0] == '')) {
                CompoundConversionService.InChIKeyToName(
                    inchiKey,
                    function (data) {
                        $scope.currentSpectrum.names = $scope.currentSpectrum.names.filter(function(x) {
                            return x != '';
                        });

                        Array.prototype.push.apply($scope.currentSpectrum.names, data);

                        $scope.compoundProcessing = false;
                    },
                    function () {
                        $scope.compoundProcessing = false;
                    }
                );
            } else {
                $scope.compoundProcessing = false;
            }
        };

        /**
         * Pull compound summary given an InChI
         */
        var processInChI = function(inchi) {
            CompoundConversionService.parseInChI(
                $scope.currentSpectrum.inchi,
                function (response) {
                    $scope.currentSpectrum.smiles = response.data.smiles;
                    $scope.currentSpectrum.inchiKey = response.data.inchiKey;
                    $scope.currentSpectrum.molFile = response.data.molData;

                    pullNames(response.data.inchiKey);
                },
                function (response) {
                    $scope.compoundError = 'Unable to process provided InChI!'
                    $scope.compoundProcessing = false;
                }
            );
        };

        var processInChIKey = function(inchiKey) {
            CompoundConversionService.getInChIByInChIKey(
                inchiKey,
                function (data) {
                    $scope.currentSpectrum.inchi = data[0];
                    processInChI(data[0]);
                },
                function (response) {
                    if (response.status == 200) {
                        $scope.compoundError = 'No results found for provided InChIKey!';
                    } else {
                        $scope.compoundError = 'Unable to process provided InChIKey!';
                    }

                    $scope.compoundProcessing = false;
                }
            );
        };

        /**
         * Generate MOL file from available compound information
         */
        $scope.retrieveCompoundData = function() {
            $log.info("Retrieving MOL data...");

            $scope.compoundError = undefined;
            $scope.compoundProcessing = true;


            // Process InChI
            if ($scope.currentSpectrum.inchi) {
                processInChI($scope.currentSpectrum.inchi);
            }

            // Process SMILES
            else if ($scope.currentSpectrum.smiles) {
                CompoundConversionService.parseSMILES(
                    $scope.currentSpectrum.smiles,
                    function (response) {
                        $scope.currentSpectrum.inchi = response.data.inchi;
                        $scope.currentSpectrum.inchiKey = response.data.inchiKey;
                        $scope.currentSpectrum.molFile = response.data.molData;

                        pullNames(response.data.inchiKey);
                    },
                    function (response) {
                        $scope.compoundError = 'Unable to process provided SMILES!';
                        $scope.compoundProcessing = false;
                    }
                );
            }

            // Process InChIKey
            else if ($scope.currentSpectrum.inchiKey) {
                processInChIKey($scope.currentSpectrum.inchiKey);
            }

            // Process names
            else if ($scope.currentSpectrum.names.length > 0) {
                namesToInChIKey($scope.currentSpectrum.names, function(inchiKey) {
                    if (inchiKey !== null) {
                        $log.info('Found InChIKey: '+ inchiKey);
                        $scope.currentSpectrum.inchiKey = inchiKey;
                        processInChIKey(inchiKey);
                    } else {
                        $scope.compoundError = 'Unable to find a match for provided name!';
                        $scope.compoundProcessing = false;
                    }
                });
            }

            else {
                $scope.compoundError = 'Please provide compound details!';
                $scope.compoundProcessing = false;
            }
        };


        $scope.parseMolFile = function(files) {
            if (files.length == 1) {
                var file = files[0];
                var fileReader = new FileReader();

                fileReader.onload = function(event) {
                    $scope.currentSpectrum.molFile = event.target.result;
                    $scope.convertMolToInChI();
                };
    
                fileReader.readAsText(files[0]);
            }
        };

        $scope.convertMolToInChI = function () {
            if (angular.isDefined($scope.currentSpectrum.molFile) && $scope.currentSpectrum.molFile !== '') {
                $scope.compoundProcessing = false;

                CompoundConversionService.parseMOL(
                    $scope.currentSpectrum.molFile,
                    function (response) {
                        $scope.currentSpectrum.inchi = response.data.inchi;
                        $scope.currentSpectrum.smiles = response.data.smiles;
                        $scope.currentSpectrum.inchiKey = response.data.inchiKey;

                        pullNames(response.data.inchiKey);
                    },
                    function (response) {
                        $scope.compoundMolError = 'Unable to process provided MOL data!'
                        $scope.compoundProcessing = false;
                    }
                );
            } else {
                $scope.compoundMolError
            }
        };

        $scope.addName = function () {
            if ($scope.currentSpectrum.names[$scope.currentSpectrum.names.length - 1] !== '') {
                $scope.currentSpectrum.names.push('');
            }
        };


        /**
         * Parse spectra
         * @param files
         */
        $scope.parseFiles = function (files) {
            $scope.page = 1;
            $scope.uploadError = null;

            UploadLibraryService.loadSpectraFile(files[0],
                function (data, origin) {
                    $log.info("Loading file "+ files[0].name +"...");

                    UploadLibraryService.processData(data, function (spectrum) {
                        if (!$scope.currentSpectrum) {
                            // Create list of ions
                            $log.info("Parsing ions...");

                            spectrum.basePeak = 0;
                            spectrum.basePeakIntensity = 0;

                            spectrum.ions = spectrum.spectrum.split(' ').map(function (x) {
                                x = x.split(':');
                                var annotation = '';

                                for (var i = 0; i < spectrum.meta.length; i++) {
                                    if (spectrum.meta[i].category === 'annotation' && spectrum.meta[i].value === x[0]) {
                                        annotation = spectrum.meta[i].name;
                                    }
                                }

                                var mz = parseFloat(x[0]);
                                var intensity = parseFloat(x[1]);

                                if (intensity > spectrum.basePeakIntensity) {
                                    spectrum.basePeak = mz;
                                    spectrum.basePeakIntensity = intensity;
                                }

                                return {
                                    ion: mz,
                                    intensity: intensity,
                                    annotation: annotation,
                                    selected: true
                                }
                            });

                            for (var i = 0; i < spectrum.ions.length; i++) {
                                spectrum.ions[i].relativeIntensity = 100 * spectrum.ions[i].intensity / spectrum.basePeakIntensity;
                            }

                            // Remove annotations and origin from metadata
                            spectrum.hiddenMetadata = spectrum.meta.filter(function (metadata) {
                                return metadata.name === 'origin' || (angular.isDefined(metadata.category) && metadata.category === 'annotation');
                            });

                            spectrum.meta = spectrum.meta.filter(function (metadata) {
                                return metadata.name !== 'origin' && (angular.isUndefined(metadata.category) || metadata.category !== 'annotation');
                            });

                            // Add an empty metadata field if none exist
                            if (spectrum.meta.length === 0) {
                                spectrum.meta.push({name: '', value: ''});
                            }


                            $log.info("Loaded spectrum from file "+ files[0].name);

                            $scope.$apply(function() {
                                $scope.currentSpectrum = spectrum;
                                $scope.page = 2;
                                $scope.showIonTable = $scope.currentSpectrum.ions.length < 500;

                                if (!$scope.currentSpectrum.molFile) {
                                    $scope.retrieveCompoundData();
                                }
                            });
                        } else {
                            $log.info("Skipping additional spectrum in file "+ files[0].name);
                            $scope.fileHasMultipleSpectra = true;
                        }
                    }, origin);
                },
                function (progress) {
                    if (progress == 100) {
                        $scope.$apply(function() {
                            if ($scope.currentSpectrum == null) {
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


        $scope.addMetadataField = function () {
            $scope.currentSpectrum.meta.push({name: '', value: ''});
        };

        $scope.removeMetadataField = function (index) {
            $scope.currentSpectrum.meta.splice(index, 1);
        };
        


        /**
         * Upload current data
         */
        var validateSpectrum = function (spectrum) {
            spectrum.errors = [];

            var ionCount = 0;

            for (var j = 0; j < spectrum.ions.length; j++) {
                if (spectrum.ions[j].selected) {
                    ionCount++;
                }
            }

            if (ionCount === 0) {
                spectrum.errors.push('This spectrum has no selected ions!  It cannot be uploaded.');
            }

            if ((angular.isUndefined(spectrum.inchi) || spectrum.inchi === '') &&
                (angular.isUndefined(spectrum.molFile) || spectrum.molFile === '') &&
                (angular.isUndefined(spectrum.smiles) || spectrum.smiles === '')) {
                spectrum.errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
            }

            if (spectrum.errors.length > 0) {
                $scope.error = 'There are some errors in the data you have provided.';
                $window.scrollTo(0, 0);

                return false;
            } else {
                return true;
            }
        };


        function finalizeAndValidateSpectra() {
            // Add additional components to spectrum object
            if (angular.isDefined($scope.metadata.chromatography) && $scope.metadata.chromatography != "") {
                $scope.currentSpectrum.meta.push({name: "sample introduction", value: $scope.metadata.chromatography});
            }

            if (angular.isDefined($scope.metadata.derivatization) && $scope.metadata.derivatization != "") {
                $scope.currentSpectrum.meta.push({name: "derivatization", value: $scope.metadata.derivatization});
            }

            if (angular.isDefined($scope.metadata.mslevel) && $scope.metadata.mslevel != "") {
                $scope.currentSpectrum.meta.push({name: "ms level", value: $scope.metadata.mslevel});
            }

            if (angular.isDefined($scope.metadata.precursormz) && $scope.metadata.precursormz != "") {
                $scope.currentSpectrum.meta.push({name: "precursor m/z", value: $scope.metadata.precursormz});
            }

            if (angular.isDefined($scope.metadata.precursortype) && $scope.metadata.precursortype != "") {
                $scope.currentSpectrum.meta.push({name: "precursor type", value: $scope.metadata.precursortype});
            }

            if (angular.isDefined($scope.metadata.ionization) && $scope.metadata.ionization != "") {
                $scope.currentSpectrum.meta.push({name: "ionization", value: $scope.metadata.ionization});
            }

            if (angular.isDefined($scope.metadata.ionmode) && $scope.metadata.ionmode != "") {
                $scope.currentSpectrum.meta.push({name: "ionization mode", value: $scope.metadata.ionmode});
            }

            if (angular.isDefined($scope.metadata.authors) && $scope.metadata.authors != "") {
                $scope.currentSpectrum.meta.push({name: "authors", value: $scope.metadata.authors});
            }

            // Validate spectrum object
            return true;
        }

        $scope.uploadFile = function () {
            if (finalizeAndValidateSpectra()) {
                // Reset the spectrum count if necessary
                if (!UploadLibraryService.isUploading()) {
                    UploadLibraryService.completedSpectraCount = 0;
                    UploadLibraryService.failedSpectraCount = 0;
                    UploadLibraryService.uploadedSpectraCount = 0;
                    UploadLibraryService.uploadStartTime = new Date().getTime();
                }

                UploadLibraryService.uploadSpectra([$scope.currentSpectrum], function (spectrum) {
                    $log.info("submitting spectrum");
                    $log.info(spectrum);

                    var req = {
                        method: 'POST',
                        url: REST_BACKEND_SERVER + '/rest/spectra',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + $rootScope.currentUser.access_token
                        },
                        data: JSON.stringify(spectrum)
                    };

                    console.log(req);

                    $http(req).then(function (data) {
                            $log.info('Spectra successfully Upload!');
                            $log.info('Reference ID: ' + data.data.id);
                            $log.info(data);

                            $rootScope.$broadcast('spectra:uploadsuccess', data);
                        },
                        function (error) {
                            $log.info(error);
                            $rootScope.$broadcast('spectra:uploaderror', error);
                        });

                    //spectrum.$batchSave(spectrum.submitter.access_token);
                }, [$scope.currentSpectrum]);

                $location.path('/upload/status');
            }
        };


        /**
         *
         * @param name
         * @param value
         * @returns {*}
         */
        $scope.queryMetadataValues = function(name, value) {
            if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '')
                value = '';

            return $http.get(
                REST_BACKEND_SERVER + '/rest/metaData/values?name='+ encodeURI(name) +'&search='+ encodeURI(value)
            ).then(function(data) {
                return data.data.values;
            });
        };



        /**
         * provides us with an overview of all our tags
         * @param query
         * @returns {*}
         * Performs initialization and acquisition of data used by the wizard
         */
        $scope.loadTags = function (query) {
            var deferred = $q.defer();

            // First filters by the query and then removes any tags already selected
            deferred.resolve($filter('filter')($scope.tags, query));

            return deferred.promise;
        };

        /**
         * Performs initialization and acquisition of data
         */
        (function () {
            // Get metadata names
            $http.get(REST_BACKEND_SERVER + '/rest/metaData/names').then(function(data) {
                $scope.metadataNames = data.data;
            });

            // Get tags
            $http.get(REST_BACKEND_SERVER + '/rest/tags').then(function(data) {
                $scope.tags = data.data;
            });
        })();
     }
})();