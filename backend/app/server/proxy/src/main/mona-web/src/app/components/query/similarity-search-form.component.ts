/**
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {NGXLogger} from 'ngx-logger';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {faSearch, faSpinner} from '@fortawesome/free-solid-svg-icons';
import {Router} from '@angular/router';

@Component({
    selector: 'similarity-search-form',
    templateUrl: '../../views/spectra/query/similaritySearchForm.html'
})
export class SimilaritySearchFormComponent implements OnInit{
    page;
    pasteError;
    spectrum;
    uploadError;
    pastedSpectrum;
    precursorToleranceUnit;
    minSimilarity;
    precursorMZ;
    precursorMZTolerance;
    fileUpload;
    filenames;
    faSearch = faSearch;
    faSpinner = faSpinner;

    constructor( public logger: NGXLogger,  public uploadLibraryService: UploadLibraryService,
                 public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public location: Location, public router: Router) {}

    ngOnInit() {
        this.page = 0;
        this.precursorToleranceUnit = 'PPM';
    }

    parsePastedSpectrum(spectrum) {
        this.pasteError = null;

        if (spectrum == null || spectrum === '') {
            this.pasteError = 'Please input a valid spectrum!';
        } else if (spectrum.match(/([0-9]*\.?[0-9]+)\s*:\s*([0-9]*\.?[0-9]+)/g)) {
            this.spectrum = spectrum;
            this.page = 2;
        } else if (spectrum.match(/([0-9]+\.?[0-9]*)[ \t]+([0-9]*\.?[0-9]+)(?:\s*(?:[;\n])|(?:"?(.+)"?\n?))?/g)) {
            spectrum = spectrum.split(/[\n\s]+/);

            if (spectrum.length % 2 === 0) {
                this.spectrum = [];

                for (let i = 0; i < spectrum.length / 2; i++) {
                    this.spectrum.push(spectrum[2 * i] + ':' + spectrum[2 * i + 1]);
                }

                this.spectrum = this.spectrum.join(' ');
                this.page = 2;
            } else {
                this.pasteError = 'Spectrum does not have complete ion/intensity pairs!';
            }
        } else {
            this.pasteError = 'Unrecognized spectrum format';
        }
    }

    /**
     * Parse spectra
     * @param files input file
     */
    parseFiles(event) {
        this.page = 1;
        this.spectrum = null;
        this.uploadError = null;

        return this.uploadLibraryService.loadSpectraFile(event.target.files[0],
            (data, origin) => {
                this.uploadLibraryService.processData(data, (spectrum) => {
                    if (spectrum === null || typeof spectrum === 'undefined') {
                        // Invalid block, the zero spectra check below reports it
                        return;
                    }
                    // Create list of ions
                    this.spectrum = spectrum.spectrum;
                    this.page = 2;
                }, origin);
            }
        ).then(() => {
            // The file was read but no valid spectrum was produced, return to the
            // form with an error instead of hanging on the loading page
            if (!this.spectrum) {
                this.uploadError = 'No valid mass spectra found in the uploaded file!';
                this.page = 0;
            }
        }).catch((reason) => {
            this.uploadError = reason instanceof Error ? reason.message : String(reason);
            this.page = 0;
        });
    }

    /**
     * Execute similarity search
     * @param minSimilarity float
     * @param precursorMZ string
     * @param precursorMZTolerance string
     * @param precursorToleranceUnit integer
     */
    search(minSimilarity, precursorMZ, precursorMZTolerance, precursorToleranceUnit) {
        // A stray form submit (e.g. Enter in a text field) must never launch
        // a search without a spectrum
        if (this.spectrum == null || this.spectrum === '') {
            this.pasteError = 'Please provide a mass spectrum before searching!';
            this.page = 0;
            return;
        }

        const request = {
            spectrum: this.spectrum,
            minSimilarity: 500,
            precursorMZ: null,
            precursorTolerancePPM: null,
            precursorToleranceDa: null,
            algorithm: 'default',
            removePrecursorIon: false,
            checkAllAdducts: false
        };

        if (minSimilarity != null && typeof +minSimilarity === 'number') {
            request.minSimilarity = parseFloat(minSimilarity);
        }

        if (precursorMZ != null && typeof +precursorMZ === 'number') {
            request.precursorMZ = parseFloat(precursorMZ);
        }

        if (precursorMZTolerance != null && typeof +precursorMZTolerance === 'number') {
            if (typeof precursorToleranceUnit === 'undefined' || precursorToleranceUnit == null || precursorMZTolerance === 'PPM') {
                request.precursorTolerancePPM = parseFloat(precursorMZTolerance);
            }

            if (precursorToleranceUnit === 'Da') {
                request.precursorToleranceDa = parseFloat(precursorMZTolerance);
            }
        }

        this.logger.info('Submitting similarity request: ' + JSON.stringify(request));

        this.spectraQueryBuilderService.setSimilarityQuery(request);
        this.router.navigate(['spectra/similaritySearch'], {skipLocationChange: false, replaceUrl: true}).then();
    }

}
