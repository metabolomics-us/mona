/**
 * Created by sajjan on 7/13/16.
 * Updated by nolanguzman on 10/31/2021
 */
import {Location} from '@angular/common';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {CompoundConversionService} from '../../services/compound-conversion.service';
import {AsyncService} from '../../services/upload/async.service';
import {NGXLogger} from 'ngx-logger';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {AuthenticationService} from '../../services/authentication.service';
import {FilterPipe} from '../../filters/filter.pipe';
import {Component, OnInit} from '@angular/core';
import {first, map} from 'rxjs/operators';
import {SlicePipe} from '@angular/common';
import {debounceTime, distinctUntilChanged, switchMap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {faCloudUploadAlt, faSpinner, faExclamationTriangle, faUser,
        faMinusSquare, faPlusSquare, faArrowLeft, faArrowRight, faSignInAlt} from '@fortawesome/free-solid-svg-icons';
import {Router} from '@angular/router';
import {SpectrumModel} from "../../mocks/spectrum.model";
import {Metadata} from "../../mocks/metadata.model";

@Component({
    selector: 'basic-uploader',
    templateUrl: '../../views/spectra/upload/basicUploader.html'
})
export class BasicUploaderComponent implements OnInit{
    currentSpectrum;
    metadata;
    page: number;
    fileHasMultipleSpectra;
    showIonTable;
    ionTableSort;
    pasteError;
    spectrum;
    queryState;
    spectraCount;
    compoundProcessing;
    compoundError;
    compoundMolError;
    uploadError;
    error;
    tags;
    filenames;
    pastedSpectrum;
    fileUpload;
    metaDataErrors;

    faCloudUpload = faCloudUploadAlt;
    faSpinner = faSpinner;
    faExclamationTriangle = faExclamationTriangle;
    faMinusSquare = faMinusSquare;
    faPlusSquare = faPlusSquare;
    faArrowLeft = faArrowLeft;
    faArrowRight = faArrowRight;
    faSignInAlt = faSignInAlt;
    faUser = faUser;

    constructor( public location: Location,  public uploadLibraryService: UploadLibraryService,
                 public compoundConversionService: CompoundConversionService,  public asyncService: AsyncService,
                 public logger: NGXLogger,  public http: HttpClient,  public filterPipe: FilterPipe,
                 public authenticationService: AuthenticationService,  public slice: SlicePipe,
                 public router: Router){}

    ngOnInit() {
        this.currentSpectrum = null;
        this.metadata = {};
        this.page = 0;
        this.fileHasMultipleSpectra = false;
        this.showIonTable = true;
        this.metaDataErrors = [];

        /**
         * Sort order for the ion table - default m/z ascending
         */
        this.ionTableSort = '-ion';
    }

    // ngbTypeahead needs an observable so we shove the metadata typeahead results into an observable and appropriately sort
    metadataNames(text$: Observable<string>) {
        return text$.pipe(
            distinctUntilChanged(),
            debounceTime(300),
            switchMap((searchText) => {
                return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/metaData/names`)
                    .pipe(map((data: any) => {
                        let filtered ;
                        filtered = data.sort((x, y) => {
                           if (x.count < y.count){
                               return 1;
                           } else if (x.count > y.count) {
                               return -1;
                           } else{
                               return 0;
                           }
                        });
                        filtered = filtered.filter((x) => {
                            return x.name.includes(searchText);
                        });
                        filtered = this.slice.transform(filtered, 0, 8);
                        filtered = filtered.map((x) => x.name);
                        return filtered;
                    }));
            }));
    }

  sortIonTable(column) {
    if (column === 'ion') {
      if (this.ionTableSort === '+ion') { this.ionTableSort = '-ion'; } else { this.ionTableSort = '+ion'; }
    }
    else if (column === 'intensity') {
      if (this.ionTableSort === '+intensity') { this.ionTableSort = '-intensity'; } else { this.ionTableSort = '+intensity'; }
    }
    else if (column === 'annotation') {
      if (this.ionTableSort === '+annotation') { this.ionTableSort = '-annotation'; } else { this.ionTableSort = '+annotation'; }
    }
    else if (column === 'relativeIntensity') {
      if (this.ionTableSort === '+relativeIntensity') { this.ionTableSort = '-relativeIntensity'; } else { this.ionTableSort = '+relativeIntensity'; }
    }
  }


    /**
     * Handle switching pages
     */
    previousPage() {
        window.scrollTo(0, 0);
        this.page--;
    }

    nextPage() {
        window.scrollTo(0, 0);
        this.page++;
    }

    restart() {
        this.currentSpectrum = null;
        this.page = 0;
        this.fileHasMultipleSpectra = false;

        // Scroll to top of the page
        window.scrollTo(0, 0);
    }


    parsePastedSpectrum(spectrum) {
        this.pasteError = null;
        let spectrumString = '';
        let ions = [];
        let basePeakIntensity = 0;

        if (spectrum === null || spectrum === '') {
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
                };
            });
        } else if (spectrum.match(/([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?/g)) {
            spectrum = spectrum.split(/[\n\s]+/);

            if (spectrum.length % 2 === 0) {
                this.spectrum = [];

                for (let i = 0; i < spectrum.length / 2; i++) {
                    if (spectrumString !== '') {
                        spectrumString += ' ';
                    }
                    spectrumString += spectrum[2 * i] + ':' + spectrum[2 * i + 1];

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
                this.pasteError = 'Spectrum does not have complete ion/intensity pairs';
            }
        } else {
            this.pasteError = 'Unrecognized spectrum format';
        }


        if (this.pasteError === null) {
            ions.forEach((x) => {
                x.relativeIntensity = 999 * x.intensity / basePeakIntensity;
            });

            this.queryState = 2;
            this.spectraCount = 1;
            this.page = 2;

            this.currentSpectrum = {names: [''], meta: [{}], ions, spectrum: spectrumString};
            this.showIonTable = this.currentSpectrum.ions.length < 500;
        }
    }


    resetCompound() {
        this.currentSpectrum.molFile = '';
        this.currentSpectrum.inchi = '';
        this.currentSpectrum.inchiKey = '';
        this.currentSpectrum.smiles = '';
        this.currentSpectrum.names = [''];
    }


    /**
     * Convert an array of names to an InChIKey based on the first result
     * @param names array of compound names
     * @param callback callback function to get name
     */
    namesToInChIKey(names, callback) {
        if (names.length === 0) {
            callback(null);
        } else {
            this.compoundConversionService.nameToInChIKey(names[0], (molecule) => {
                if (molecule !== null) {
                    callback(molecule);
                } else {
                    this.namesToInChIKey(names.slice(1), callback);
                }
            }, (error) => {
                this.namesToInChIKey(names.slice(1), callback);
            });
        }
    }

    /**
     * Pull names from CTS given an InChIKey and update the currentSpectrum
     */
    pullNames(inchiKey) {
        // Only pull if there are no names provided
        if (this.currentSpectrum.names.length === 0 || (this.currentSpectrum.names.length === 1 && this.currentSpectrum.names[0] === '')) {
            this.compoundConversionService.InChIKeyToName(
                inchiKey,
                 (data) => {
                    this.currentSpectrum.names = this.currentSpectrum.names.filter((x) => {
                        return x !== '';
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
    }

    /**
     * Pull compound summary given an InChI
     */
    processInChI(inchi) {
        this.compoundConversionService.parseInChI(
            this.currentSpectrum.inchi,
             (response) => {
                this.logger.debug('Parse Inchi response: ' + response);
                this.currentSpectrum.smiles = response.smiles;
                this.currentSpectrum.inchiKey = response.inchiKey;
                this.currentSpectrum.molFile = response.molData;

                this.pullNames(response.inchiKey);
            },
             (response) => {
                this.compoundError = 'Unable to process provided InChI!';
                this.compoundProcessing = false;
            }
        );
    }

    processInChIKey(inchiKey) {
        this.compoundConversionService.getInChIByInChIKey(
            inchiKey,
             (data) => {
                this.logger.info('InChi By InchiKey Reponse: ' + data);
                this.currentSpectrum.inchi = data[0];
                this.processInChI(data[0]);
            },
             (response) => {
                if (response.status === 200) {
                    this.compoundError = 'No results found for provided InChIKey!';
                } else {
                    this.compoundError = 'Unable to process provided InChIKey!';
                }

                this.compoundProcessing = false;
            }
        );
    }

    /**
     * Generate MOL file from available compound information
     */
    retrieveCompoundData() {
        this.logger.info('Retrieving MOL data...');

        this.compoundError = undefined;
        this.compoundProcessing = true;


        // Process InChI
        if (this.currentSpectrum.inchi) {
            this.processInChI(this.currentSpectrum.inchi);
        }

        // Process SMILES
        else if (this.currentSpectrum.smiles) {
            this.compoundConversionService.parseSMILES(
                this.currentSpectrum.smiles,
                 (response) => {
                    this.logger.debug('Parse smiles response ' + response);
                    this.currentSpectrum.inchi = response.inchi;
                    this.currentSpectrum.inchiKey = response.inchiKey;
                    this.currentSpectrum.molFile = response.molData;

                    this.pullNames(response.inchiKey);
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
            this.namesToInChIKey(this.currentSpectrum.names, (inchiKey) => {
                this.logger.debug('Name to inchikey response: ' + inchiKey);
                if (inchiKey !== null) {
                    this.logger.info('Found InChIKey: ' + inchiKey);
                    this.currentSpectrum.inchiKey = inchiKey;
                    this.processInChIKey(inchiKey);
                } else {
                    this.compoundError = 'Unable to find a match for provided name!';
                    this.compoundProcessing = false;
                }
            });
        }

        else {
            // Having this would cause the error to show before the user even inputted anything (on basic uploader)
            // this.compoundError = 'Please provide compound details';
            this.compoundError = '';
            this.compoundProcessing = false;
        }
    }


    parseMolFile(files) {
        if (files.length === 1) {
            const file = files[0];
            const fileReader = new FileReader();

            fileReader.onload = (event) => {
                this.currentSpectrum.molFile = event.target.result;
                this.convertMolToInChI();
            };

            fileReader.readAsText(files[0]);
        }
    }

    convertMolToInChI() {
        if (typeof this.currentSpectrum.molFile !== 'undefined' && this.currentSpectrum.molFile !== '') {
            this.compoundProcessing = false;

            this.compoundConversionService.parseMOL(
                this.currentSpectrum.molFile,
                 (response) => {
                    this.currentSpectrum.inchi = response.inchi;
                    this.currentSpectrum.smiles = response.smiles;
                    this.currentSpectrum.inchiKey = response.inchiKey;

                    this.pullNames(response.inchiKey);
                },
                 (response) => {
                    this.compoundMolError = 'Unable to process provided MOL data!';
                    this.compoundProcessing = false;
                }
            );
        } else {
            this.compoundMolError = '';
        }
    }

    addName() {
        if (this.currentSpectrum.names[this.currentSpectrum.names.length - 1] !== '') {
            this.currentSpectrum.names.push('');
        }
    }

    removeName(index: number) {
        this.currentSpectrum.names.splice(index, 1);
    }

    trackByIndex(index: number, item: any) {
        return index;
    }



    /**
     * Parse spectra
     * @param files files from input in html
     */
    parseFiles(event) {
        this.page = 1;
        this.uploadError = null;
        this.uploadLibraryService.isSTP = false;
        this.uploadLibraryService.loadSpectraFile(event.target.files[0],
             (data, origin) => {
                this.logger.info('Loading file ' + event.target.files[0].name + '...');

                this.uploadLibraryService.processData(data, (spectrum) => {
                    if (!this.currentSpectrum) {
                        // Create list of ions
                        this.logger.info('Parsing ions...');

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

                            const mz = parseFloat(x[0]);
                            const intensity = parseFloat(x[1]);

                            if (intensity > spectrum.basePeakIntensity) {
                                spectrum.basePeak = mz;
                                spectrum.basePeakIntensity = intensity;
                            }

                            return {
                                ion: mz,
                                intensity,
                                annotation,
                                selected: true
                            };
                        });

                        for (let i = 0; i < spectrum.ions.length; i++) {
                            spectrum.ions[i].relativeIntensity = 100 * spectrum.ions[i].intensity / spectrum.basePeakIntensity;
                        }

                        // Remove annotations and origin from metadata
                        spectrum.hiddenMetadata = spectrum.meta.filter((metadata) => {
                            return metadata.name === 'origin' || (typeof metadata.category !== 'undefined' && metadata.category === 'annotation');
                        });

                        spectrum.meta = spectrum.meta.filter((metadata) => {
                            return metadata.name !== 'origin' && (typeof metadata.category !== 'undefined' || metadata.category !== 'annotation');
                        });

                        // Add an empty metadata field if none exist
                        if (spectrum.meta.length === 0) {
                            spectrum.meta.push({name: '', value: ''});
                        }


                        this.logger.info('Loaded spectrum from file ' + event.target.files[0].name);

                        this.currentSpectrum = spectrum;
                        this.page = 2;
                        this.showIonTable = this.currentSpectrum.ions.length < 500;

                        if (!this.currentSpectrum.molFile) {
                            this.retrieveCompoundData();
                        }
                    } else {
                        this.fileHasMultipleSpectra = true;
                    }
                }, origin);
            }
        );
    }


    addMetadataField() {
        this.currentSpectrum.meta.push({name: '', value: ''});
    }

    removeMetadataField(index) {
        this.currentSpectrum.meta.splice(index, 1);
    }



    /**
     * Upload current data
     */
    validateSpectrum(spectrum) {
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

        if ((typeof spectrum.inchi === 'undefined' || spectrum.inchi === '') &&
            (typeof spectrum.molFile === 'undefined' || spectrum.molFile === '') &&
            (typeof spectrum.smiles === 'undefined' || spectrum.smiles === '')) {
            spectrum.errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
        }

        if (spectrum.errors.length > 0) {
            this.error = 'There are some errors in the data you have provided.';
            window.scrollTo(0, 0);

            return false;
        } else {
            return true;
        }
    }


    finalizeAndValidateSpectra() {
        // Add additional components to spectrum object
        if (typeof this.metadata.chromatography !== 'undefined' && this.metadata.chromatography !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'sample introduction', value: this.metadata.chromatography});
        }

        if (typeof this.metadata.derivatization !== 'undefined' && this.metadata.derivatization !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'derivatization', value: this.metadata.derivatization});
        }

        if (typeof this.metadata.mslevel !== 'undefined' && this.metadata.mslevel !== '' && this.metadata.chromatography !== null
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'ms level', value: this.metadata.mslevel});
        }

        if (typeof this.metadata.precursormz !== 'undefined' && this.metadata.precursormz !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'precursor m/z', value: this.metadata.precursormz});
        }

        if (typeof this.metadata.precursortype !== 'undefined' && this.metadata.precursortype !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'precursor type', value: this.metadata.precursortype});
        }

        if (typeof this.metadata.ionization !== 'undefined' && this.metadata.ionization !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'ionization', value: this.metadata.ionization});
        }

        if (typeof this.metadata.ionmode !== 'undefined' && this.metadata.ionmode !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'ionization mode', value: this.metadata.ionmode});
        }

        if (typeof this.metadata.authors !== 'undefined' && this.metadata.authors !== ''
          && this.metadata.chromatography !== null) {
            this.currentSpectrum.meta.push({name: 'authors', value: this.metadata.authors});
        }

        // Validate spectrum object
        return true;
    }

    uploadFile() {
        if (this.finalizeAndValidateSpectra()) {
            // Reset the spectrum count if necessary
            if (!this.uploadLibraryService.isUploading()) {
                this.uploadLibraryService.completedSpectraCount = 0;
                this.uploadLibraryService.failedSpectraCount = 0;
                this.uploadLibraryService.uploadedSpectraCount = 0;
                this.uploadLibraryService.uploadStartTime = new Date().getTime();
            }

            this.uploadLibraryService.uploadSpectra([this.currentSpectrum],  (spectrum) => {
                this.logger.info('submitting spectrum');
                this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, spectrum,
                    {headers: {
                            Authorization: `Bearer ${this.authenticationService.getCurrentUser().accessToken}`
                    }}).pipe(first()).subscribe((data: any) => {
                    this.logger.info('Spectra successfully Upload!');
                    this.logger.info('Reference ID: ' + data.id);
                    this.uploadLibraryService.uploadedSpectra.push(data.id);
                }, (err) => {
                        this.logger.info('ERROR', err);
                });
            });

            this.router.navigate(['/upload/status']).then();
        }
    }


    /**
     *
     * @param name string name of metadata
     * @returns Observable return observable
     */
    // This rest call is not working at all, will have to dig in backend and see why that is
    queryMetadataValues(name: any): (text$: Observable<string>) => Observable<any> {
        return (text$: Observable<string>) =>
            text$.pipe(
                debounceTime(300),
                distinctUntilChanged(),
                switchMap(switchText =>
                    this.http.get<any>(`${environment.REST_BACKEND_SERVER}/rest/metaData/values?name=${encodeURI(name)}&search=${encodeURI(switchText)}`)
                )
            );
    }


    /**
     * provides us with an overview of all our tags
     * @param query json object
     * @returns Promise with result
     * Performs initialization and acquisition of data used by the wizard
     */
    loadTags(query) {
        return new Promise((resolve) => {
           resolve(this.filterPipe.transform(this.tags, query));
        });
    }

    validateMetadata() {
      const emptyPieces = this.currentSpectrum.meta.filter((x) => {
        return x.name.replace(/\s+/g, '') === '' || x.value.replace(/\s+/g, '') === '';
      });

      if (emptyPieces.length > 0) {
        this.metaDataErrors = [];
        this.metaDataErrors.push('One or more metadata entries are missing data, please provide entries in both fields of remove entry with minus button');
      } else {
        this.metaDataErrors = [];
        this.nextPage();
      }
    }
}
