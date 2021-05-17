/**
 * Created by sajjan on 7/13/16.
 */

import * as angular from 'angular';

class BasicUploaderController{
    private static $inject = ['$scope', '$rootScope', '$window', '$location', 'UploadLibraryService', 'CompoundConversionService', '$q', '$filter', 'AsyncService', '$log', 'REST_BACKEND_SERVER', '$http'];
    private $scope;
    private $rootScope;
    private $window;
    private $location;
    private UploadLibraryService;
    private CompoundConversionService;
    private $q;
    private $filter;
    private AsyncService;
    private $log;
    private REST_BACKEND_SERVER;
    private $http;
    private currentSpectrum;
    private metadata;
    private page;
    private fileHasMultipleSpectra;
    private showIonTable;
    private ionTableSort;
    private ionTableSortReverse;
    private pasteError;
    private spectrum;
    private queryState;
    private spectraCount;
    private compoundProcessing;
    private compoundError;
    private compoundMolError;
    private uploadError;
    private error;
    private currentUser;
    private tags;
    private metadataNames;

    constructor($scope, $rootScope, $window, $location, UploadLibraryService, CompoundConversionService, $q, $filter, AsyncService, $log, REST_BACKEND_SERVER, $http){
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.$window = $window;
        this.UploadLibraryService = UploadLibraryService;
        this.CompoundConversionService = CompoundConversionService;
        this.$q = $q;
        this.$filter = $filter;
        this.AsyncService = AsyncService;
        this.$log = $log;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
    }

    $onInit = () => {
        this.currentSpectrum = null;
        this.metadata = {};
        this.page = 0;
        this.fileHasMultipleSpectra = false;
        this.showIonTable = true;

        /**
         * Sort order for the ion table - default m/z ascending
         */
        this.ionTableSort = 'ion';
        this.ionTableSortReverse = false;

        this.$http.get(this.REST_BACKEND_SERVER + '/rest/metaData/names').then((data) => {
            this.metadataNames = data.data;
        });

        // Get tags
        this.$http.get(this.REST_BACKEND_SERVER + '/rest/tags').then((data) => {
            this.tags = data.data;
        });
    }

    sortIonTable = (column) => {
        if (column === 'ion') {
            this.ionTableSortReverse = (this.ionTableSort === '+ion') ? !this.ionTableSortReverse : false;
            this.ionTableSort = '+ion';
        }
        else if (column === 'intensity') {
            this.ionTableSortReverse = (this.ionTableSort === '-intensity') ? !this.ionTableSortReverse : false;
            this.ionTableSort = '-intensity';
        }
        else if (column === 'relativeIntensity') {
            this.ionTableSortReverse = (this.ionTableSort === '-relativeIntensity') ? !this.ionTableSortReverse : false;
            this.ionTableSort = '-relativeIntensity';
        }
        else if (column === 'annotation') {
            this.ionTableSortReverse = (this.ionTableSort === '-annotation') ? !this.ionTableSortReverse : false;
            this.ionTableSort = '-annotation';
        }
    };


    /**
     * Handle switching pages
     */
    previousPage = () => {
        this.$window.scrollTo(0, 0);
        this.page--;
    };

    nextPage = () => {
        this.$window.scrollTo(0, 0);
        this.page++;
    };

    restart = () => {
        this.currentSpectrum = null;
        this.page = 0;
        this.fileHasMultipleSpectra = false;

        // Scroll to top of the page
        this.$window.scrollTo(0, 0);
    };


    parsePastedSpectrum = (spectrum) => {
        this.pasteError = null;
        let spectrumString = '';
        let ions = [];
        let basePeakIntensity = 0;

        if (spectrum == null || spectrum == "") {
            this.pasteError = 'Please input a valid spectrum!';
        } else if (spectrum.match(/([0-9]*\.?[0-9]+)\s*:\s*([0-9]*\.?[0-9]+)/g)) {
            spectrumString = spectrum;

            ions = spectrum.split(' ').map((x) => {
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
                this.spectrum = [];

                for (let i = 0; i < spectrum.length / 2; i++) {
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
                this.pasteError = 'Spectrum does not have complete ion/intensity pairs!'
            }
        } else {
            this.pasteError = 'Unrecognized spectrum format!'
        }


        if (this.pasteError === null) {
            ions.forEach((x) =>{
                x.relativeIntensity = 999 * x.intensity / basePeakIntensity;
            });

            this.queryState = 2;
            this.spectraCount = 1;
            this.page = 2;

            this.currentSpectrum = {names: [''], meta: [{}], ions: ions, spectrum: spectrumString};
            this.showIonTable = this.currentSpectrum.ions.length < 500;
        }
    };


    resetCompound = () => {
        this.currentSpectrum.molFile = '';
        this.currentSpectrum.inchi = '';
        this.currentSpectrum.inchiKey = '';
        this.currentSpectrum.smiles = '';
        this.currentSpectrum.names = [''];
    };


    /**
     * Convert an array of names to an InChIKey based on the first result
     * @param names array of compound names
     * @param callback
     */
    namesToInChIKey = (names, callback) => {
        if (names.length == 0) {
            callback(null);
        } else {
            this.CompoundConversionService.nameToInChIKey(names[0], (molecule) => {
                if (molecule !== null) {
                    callback(molecule);
                } else {
                    this.namesToInChIKey(names.slice(1), callback);
                }
            }, (error) => {
                this.namesToInChIKey(names.slice(1), callback);
            });
        }
    };

    /**
     * Pull names from CTS given an InChIKey and update the currentSpectrum
     */
    pullNames = (inchiKey) => {
        // Only pull if there are no names provided
        if (this.currentSpectrum.names.length == 0 || (this.currentSpectrum.names.length == 1 && this.currentSpectrum.names[0] == '')) {
            this.CompoundConversionService.InChIKeyToName(
                inchiKey,
                 (data) => {
                    this.currentSpectrum.names = this.currentSpectrum.names.filter((x) => {
                        return x != '';
                    });

                    Array.prototype.push.apply(this.currentSpectrum.names, data);

                    this.compoundProcessing = false;
                },
                 () => {
                    this.compoundProcessing = false;
                }
            );
        } else {
            this.compoundProcessing = false;
        }
    };

    /**
     * Pull compound summary given an InChI
     */
    processInChI = (inchi) => {
        this.CompoundConversionService.parseInChI(
            this.currentSpectrum.inchi,
             (response) => {
                this.currentSpectrum.smiles = response.data.smiles;
                this.currentSpectrum.inchiKey = response.data.inchiKey;
                this.currentSpectrum.molFile = response.data.molData;

                this.pullNames(response.data.inchiKey);
            },
             (response) => {
                this.compoundError = 'Unable to process provided InChI!'
                this.compoundProcessing = false;
            }
        );
    };

    processInChIKey = (inchiKey) => {
        this.CompoundConversionService.getInChIByInChIKey(
            inchiKey,
             (data) => {
                this.currentSpectrum.inchi = data[0];
                this.processInChI(data[0]);
            },
             (response) => {
                if (response.status == 200) {
                    this.compoundError = 'No results found for provided InChIKey!';
                } else {
                    this.compoundError = 'Unable to process provided InChIKey!';
                }

                this.compoundProcessing = false;
            }
        );
    };

    /**
     * Generate MOL file from available compound information
     */
    retrieveCompoundData = () => {
        this.$log.info("Retrieving MOL data...");

        this.compoundError = undefined;
        this.compoundProcessing = true;


        // Process InChI
        if (this.currentSpectrum.inchi) {
            this.processInChI(this.currentSpectrum.inchi);
        }

        // Process SMILES
        else if (this.currentSpectrum.smiles) {
            this.CompoundConversionService.parseSMILES(
                this.currentSpectrum.smiles,
                 (response) => {
                    this.currentSpectrum.inchi = response.data.inchi;
                    this.currentSpectrum.inchiKey = response.data.inchiKey;
                    this.currentSpectrum.molFile = response.data.molData;

                    this.pullNames(response.data.inchiKey);
                },
                 (response) => {
                    this.compoundError = 'Unable to process provided SMILES!';
                    this.compoundProcessing = false;
                }
            );
        }

        // Process InChIKey
        else if (this.currentSpectrum.inchiKey) {
            this.processInChIKey(this.currentSpectrum.inchiKey);
        }

        // Process names
        else if (this.currentSpectrum.names.length > 0) {
            this.namesToInChIKey(this.currentSpectrum.names, function(inchiKey) {
                if (inchiKey !== null) {
                    this.$log.info('Found InChIKey: '+ inchiKey);
                    this.currentSpectrum.inchiKey = inchiKey;
                    this.processInChIKey(inchiKey);
                } else {
                    this.compoundError = 'Unable to find a match for provided name!';
                    this.compoundProcessing = false;
                }
            });
        }

        else {
            this.compoundError = 'Please provide compound details!';
            this.compoundProcessing = false;
        }
    };


    parseMolFile = (files) => {
        if (files.length == 1) {
            let file = files[0];
            let fileReader = new FileReader();

            fileReader.onload = (event) => {
                this.currentSpectrum.molFile = event.target.result;
                this.convertMolToInChI();
            };

            fileReader.readAsText(files[0]);
        }
    };

    convertMolToInChI =  () => {
        if (angular.isDefined(this.currentSpectrum.molFile) && this.currentSpectrum.molFile !== '') {
            this.compoundProcessing = false;

            this.CompoundConversionService.parseMOL(
                this.currentSpectrum.molFile,
                 (response) => {
                    this.currentSpectrum.inchi = response.data.inchi;
                    this.currentSpectrum.smiles = response.data.smiles;
                    this.currentSpectrum.inchiKey = response.data.inchiKey;

                    this.pullNames(response.data.inchiKey);
                },
                 (response) => {
                    this.compoundMolError = 'Unable to process provided MOL data!'
                    this.compoundProcessing = false;
                }
            );
        } else {
            this.compoundMolError = ''
        }
    };

    addName = () => {
        if (this.currentSpectrum.names[this.currentSpectrum.names.length - 1] !== '') {
            this.currentSpectrum.names.push('');
        }
    };


    /**
     * Parse spectra
     * @param files
     */
    parseFiles = (files) => {
        this.page = 1;
        this.uploadError = null;

        this.UploadLibraryService.loadSpectraFile(files[0],
             (data, origin) => {
                this.$log.info("Loading file "+ files[0].name +"...");

                this.UploadLibraryService.processData(data, (spectrum) => {
                    if (!this.currentSpectrum) {
                        // Create list of ions
                        this.$log.info("Parsing ions...");

                        spectrum.basePeak = 0;
                        spectrum.basePeakIntensity = 0;

                        spectrum.ions = spectrum.spectrum.split(' ').map((x) =>  {
                            x = x.split(':');
                            let annotation = '';

                            for (let i = 0; i < spectrum.meta.length; i++) {
                                if (spectrum.meta[i].category === 'annotation' && spectrum.meta[i].value === x[0]) {
                                    annotation = spectrum.meta[i].name;
                                }
                            }

                            let mz = parseFloat(x[0]);
                            let intensity = parseFloat(x[1]);

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

                        for (let i = 0; i < spectrum.ions.length; i++) {
                            spectrum.ions[i].relativeIntensity = 100 * spectrum.ions[i].intensity / spectrum.basePeakIntensity;
                        }

                        // Remove annotations and origin from metadata
                        spectrum.hiddenMetadata = spectrum.meta.filter((metadata) => {
                            return metadata.name === 'origin' || (angular.isDefined(metadata.category) && metadata.category === 'annotation');
                        });

                        spectrum.meta = spectrum.meta.filter((metadata) => {
                            return metadata.name !== 'origin' && (angular.isUndefined(metadata.category) || metadata.category !== 'annotation');
                        });

                        // Add an empty metadata field if none exist
                        if (spectrum.meta.length === 0) {
                            spectrum.meta.push({name: '', value: ''});
                        }


                        this.$log.info("Loaded spectrum from file "+ files[0].name);

                        this.$scope.$apply(() => {
                            this.currentSpectrum = spectrum;
                            this.page = 2;
                            this.showIonTable = this.currentSpectrum.ions.length < 500;

                            if (!this.currentSpectrum.molFile) {
                                this.retrieveCompoundData();
                            }
                        });
                    } else {
                        this.$log.info("Skipping additional spectrum in file "+ files[0].name);
                        this.fileHasMultipleSpectra = true;
                    }
                }, origin);
            },
            (progress) => {
                if (progress == 100) {
                    this.$scope.$apply(() => {
                        if (this.currentSpectrum == null) {
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


    addMetadataField = () => {
        this.currentSpectrum.meta.push({name: '', value: ''});
    };

    removeMetadataField = (index) => {
        this.currentSpectrum.meta.splice(index, 1);
    };



    /**
     * Upload current data
     */
    validateSpectrum = (spectrum) => {
        spectrum.errors = [];

        let ionCount = 0;

        for (let j = 0; j < spectrum.ions.length; j++) {
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
            this.error = 'There are some errors in the data you have provided.';
            this.$window.scrollTo(0, 0);

            return false;
        } else {
            return true;
        }
    };


    finalizeAndValidateSpectra() {
        // Add additional components to spectrum object
        if (angular.isDefined(this.metadata.chromatography) && this.metadata.chromatography != "") {
            this.currentSpectrum.meta.push({name: "sample introduction", value: this.metadata.chromatography});
        }

        if (angular.isDefined(this.metadata.derivatization) && this.metadata.derivatization != "") {
            this.currentSpectrum.meta.push({name: "derivatization", value: this.metadata.derivatization});
        }

        if (angular.isDefined(this.metadata.mslevel) && this.metadata.mslevel != "") {
            this.currentSpectrum.meta.push({name: "ms level", value: this.metadata.mslevel});
        }

        if (angular.isDefined(this.metadata.precursormz) && this.metadata.precursormz != "") {
            this.currentSpectrum.meta.push({name: "precursor m/z", value: this.metadata.precursormz});
        }

        if (angular.isDefined(this.metadata.precursortype) && this.metadata.precursortype != "") {
            this.currentSpectrum.meta.push({name: "precursor type", value: this.metadata.precursortype});
        }

        if (angular.isDefined(this.metadata.ionization) && this.metadata.ionization != "") {
            this.currentSpectrum.meta.push({name: "ionization", value: this.metadata.ionization});
        }

        if (angular.isDefined(this.metadata.ionmode) && this.metadata.ionmode != "") {
            this.currentSpectrum.meta.push({name: "ionization mode", value: this.metadata.ionmode});
        }

        if (angular.isDefined(this.metadata.authors) && this.metadata.authors != "") {
            this.currentSpectrum.meta.push({name: "authors", value: this.metadata.authors});
        }

        // Validate spectrum object
        return true;
    }

    uploadFile = () => {
        if (this.finalizeAndValidateSpectra()) {
            // Reset the spectrum count if necessary
            if (!this.UploadLibraryService.isUploading()) {
                this.UploadLibraryService.completedSpectraCount = 0;
                this.UploadLibraryService.failedSpectraCount = 0;
                this.UploadLibraryService.uploadedSpectraCount = 0;
                this.UploadLibraryService.uploadStartTime = new Date().getTime();
            }

            this.UploadLibraryService.uploadSpectra([this.currentSpectrum],  (spectrum) => {
                this.$log.info("submitting spectrum");
                this.$log.info(spectrum);

                let req = {
                    method: 'POST',
                    url: this.REST_BACKEND_SERVER + '/rest/spectra',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + this.currentUser.access_token
                    },
                    data: JSON.stringify(spectrum)
                };

                console.log(req);

                this.$http(req).then((data) => {
                        this.$log.info('Spectra successfully Upload!');
                        this.$log.info('Reference ID: ' + data.data.id);
                        this.$log.info(data);

                        this.$rootScope.$broadcast('spectra:uploadsuccess', data);
                    },
                     (error) => {
                        this.$log.info(error);
                        this.$rootScope.$broadcast('spectra:uploaderror', error);
                    });

                //spectrum.$batchSave(spectrum.submitter.access_token);
            }, [this.currentSpectrum]);

            this.$location.path('/upload/status');
        }
    };


    /**
     *
     * @param name
     * @param value
     * @returns {*}
     */
    queryMetadataValues = (name, value) => {
        if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '')
            value = '';

        return this.$http.get(
            this.REST_BACKEND_SERVER + '/rest/metaData/values?name='+ encodeURI(name) +'&search='+ encodeURI(value)
        ).then((data) => {
            return data.data.values;
        });
    };



    /**
     * provides us with an overview of all our tags
     * @param query
     * @returns {*}
     * Performs initialization and acquisition of data used by the wizard
     */
    loadTags = (query) => {
        let deferred = this.$q.defer();

        // First filters by the query and then removes any tags already selected
        deferred.resolve(this.$filter('filter')(this.tags, query));

        return deferred.promise;
    };
}

let BasicUploaderComponent = {
    selector: "basicUploader",
    templateUrl: '../../views/spectra/upload/basicUploader.html',
    bindings: {},
    controller: BasicUploaderController
}

angular.module('moaClientApp')
    .component(BasicUploaderComponent.selector, BasicUploaderComponent);
