import * as angular from 'angular';
import {Component, Inject, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {Location} from "@angular/common";
import {NGXLogger} from "ngx-logger";
import {UploadLibraryService} from "../../services/upload/upload-library.service";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";

@Component({
    selector: 'similarity-search-form',
    templateUrl: '../../views/spectra/query/similaritySearchForm.html'
})
export class SimilaritySearchFormComponent implements OnInit{
    private page;
    private pasteError;
    private spectrum;
    private uploadError;
    private pastedSpectrum;
    private precursorToleranceUnit;
    private minSimilarity;
    private precursorMZ;
    private precursorMZTolerance;

    constructor(@Inject(NGXLogger) private logger: NGXLogger, @Inject(UploadLibraryService) private uploadLibraryService: UploadLibraryService,
                @Inject(SpectraQueryBuilderService) private spectraQueryBuilderService: SpectraQueryBuilderService,
                @Inject(Location) private location: Location) {}

    ngOnInit(): void {
        this.page = 0;
        this.precursorToleranceUnit = 'PPM';
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

        this.uploadLibraryService.loadSpectraFile(files[0],
            (data, origin) => {
                this.uploadLibraryService.processData(data, (spectrum) => {
                    // Create list of ions
                    this.spectrum = spectrum.spectrum;
                    this.page = 2;
                }, origin);
            },
            (progress) => {
                if (progress == 100) {
                    if (this.spectrum == null) {
                        this.page = 0;
                        this.uploadError = 'Unable to load spectra!';
                    } else {
                        this.page = 2;
                    }
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

        this.logger.info("Submitting similarity request: "+ JSON.stringify(request));

        this.spectraQueryBuilderService.setSimilarityQuery(request);
        this.location.go('/spectra/similaritySearch');
    };

}

angular.module('moaClientApp')
    .directive('similaritySearchForm', downgradeComponent({
        component: SimilaritySearchFormComponent
    }));
