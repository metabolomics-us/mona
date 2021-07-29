/**
 * displays our spectra
 * @param $scope
 * @param spectrum
 * @param massSpec
 * @constructor
 */
import {Location} from "@angular/common";
import {CookieMain} from "../../services/cookie/cookie-main.service";
import {Spectrum} from "../../services/persistence/spectrum.resource";
import {AuthenticationService} from "../../services/authentication.service";
import {NGXLogger} from "ngx-logger";
import {Component, Inject, Input, OnInit} from "@angular/core";
import * as angular from 'angular';
import {first} from "rxjs/operators";
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'spectrum-viewer',
    templateUrl: '../../views/spectra/display/viewSpectrum.html'
})
export class SpectrumViewerComponent implements OnInit{
    @Input() public delayedspectrum;
    private spectrum;
    private score;
    private massSpec;
    private accordionStatus;
    ionTableSort;
    ionTableSortReverse;
    private loadingSimilarSpectra;
    private similarSpectra;
    private massRegex;
    private truncateDecimal;
    private truncateMass;
    private ionRegex;
    private match;
    private intensity;
    private showScore;

    constructor(@Inject(NGXLogger) private logger: NGXLogger, @Inject(CookieMain) private cookie: CookieMain,
                @Inject(Spectrum) private spectrumService: Spectrum, @Inject(AuthenticationService) private authenticationService: AuthenticationService,
                @Inject(Location) private location: Location){}

    ngOnInit() {
        /**
         * Sort order for the ion table - default m/z ascending
         */
        this.ionTableSort = 'ion';
        this.ionTableSortReverse = false;

        /**
         * quality score of our spectrum
         * @type {number}
         */
        this.score = 0;

        this.massSpec = [];

        this.showScore = false;

        this.massRegex = /^\s*(\d+\.\d{4})\d*\s*$/;

        /**
         * Decimal truncation routines
         */
        this.truncateDecimal = (s, length) => {
            return (typeof(s) === 'number') ? s.toFixed(length) : s;
        };

        /**
         * Truncate the
         */
        this.truncateMass = (mass) => {
            return this.truncateDecimal(mass, 4);
        };


        // truncate metadata
        if (typeof this.delayedspectrum.metaData !== 'undefined') {
            for (let i = 0; i < this.delayedspectrum.metaData.length; i++) {
                let curMeta = this.delayedspectrum.metaData[i];

                let name = curMeta.name.toLowerCase();

                if (name.indexOf('mass accuracy') > -1) {
                    curMeta.value = this.truncateDecimal(curMeta.value, 1);
                } else if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                    curMeta.value = this.truncateMass(curMeta.value);
                } else if (name.indexOf('retention') > -1) {
                    curMeta.value = this.truncateDecimal(curMeta.value, 1);
                }
            }
        }

        // truncate compounds
        if (typeof this.delayedspectrum.compound !== 'undefined') {
            for (let i = 0; i < this.delayedspectrum.compound.length; i++) {
                let compoundMeta = this.delayedspectrum.compound[i].metaData;
                for (let j = 0, m = compoundMeta.length; j < m; j++) {
                    let metadata = compoundMeta[j];
                    let name = metadata.name.toLowerCase();

                    if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                        metadata.value = this.truncateMass(metadata.value);
                    }
                }
            }
        }


        // Regular expression to extract ions
        this.ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;


        // Parse spectrum string to generate ion list
        let match;

        while ((match = this.ionRegex.exec(this.delayedspectrum.spectrum)) !== null) {
            // Find annotation
            let annotation = '';
            let computed = false;

            if (typeof this.delayedspectrum.annotations !== 'undefined') {
                for (let i = 0; i < this.delayedspectrum.annotations.length; i++) {
                    if (this.delayedspectrum.annotations[i].value === parseFloat(match[1])) {
                        annotation = this.delayedspectrum.annotations[i].name;
                        computed = this.delayedspectrum.annotations[i].computed;
                    }
                }
            }

            // Truncate decimal values of m/z
            match[1] = this.truncateMass(match[1]);

            // Store ion
            let intensity = parseFloat(match[2]);

            if (intensity > 0) {
                this.massSpec.push({
                    ion: parseFloat(match[1]),
                    intensity: intensity,
                    annotation: annotation,
                    computed: computed
                });
            }
        }

        /**
         * Mass spectrum obtained from cache if it exists, otherwise from REST api
         */
        this.spectrum = this.delayedspectrum;

        /**
         * status of our accordion
         * @type {{isBiologicalCompoundOpen: boolean, isChemicalCompoundOpen: boolean, isDerivatizedCompoundOpen: boolean}}
         */
        this.accordionStatus = {
            isSpectraOpen: true,
            isIonTableOpen: false,
            isSimilarSpectraOpen: false,
            isCompoundOpen: []
        };

        if (typeof this.spectrum.compound !== 'undefined') {
            for (let i = 0; i < this.spectrum.compound.length; i++) {
                this.accordionStatus.isCompoundOpen.push(i === 0);
            }
        }

        /**
         * watch the accordion status and updates related cookies
         */
        this.accordionStatus.valueChanges.subscribe((value) => {
           this.accordionStatus.forEach((value, key) => {
               if(key === 'isCompoundOpen') {
                   for (let i = 0; i < this.spectrum.compound.length; i++) {
                       this.cookie.update('DisplayCompound' + i, value[i]);
                   }
               }
               else {
                   this.cookie.update("DisplaySpectra" + key, value);
               }
           })
        });

        /**
         * Loading of similar spectra
         */
        this.loadingSimilarSpectra = true;
        this.similarSpectra = [];
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
        else if (column === 'annotation') {
            this.ionTableSortReverse = (this.ionTableSort === '-annotation') ? !this.ionTableSortReverse : false;
            this.ionTableSort = '-annotation';
        }
    };




    loadSimilarSpectra = () => {
        if (!this.loadingSimilarSpectra)
            return;

        this.spectrumService.searchSimilarSpectra({spectrum: this.spectrum.spectrum, minSimilarity: 0.5}).pipe(first()).subscribe(
            (res: any) => {
                let data = res;
                this.similarSpectra = data.filter((x) => { return x.id !== this.spectrum.id; });
                this.loadingSimilarSpectra = false;
            }, (res) => {
                this.loadingSimilarSpectra = false;
            }
        );
    };

    /**
     * @Deprecated
     * displays the spectrum for the given index
     * @param id
     * @param index
     */
    viewSpectrum = (id) => {
        this.location.go('/spectra/display/' + id);
    };

    checkNumber = (check: any) => {
        if(typeof check === 'number'){
            return true;
        }
        return false;
    }

}

angular.module('moaClientApp')
    .directive('spectrumViewer', downgradeComponent({
        component: SpectrumViewerComponent
    }));




