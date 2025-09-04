/**
 * Updated by nolanguzman on 10/31/2021
 * displays our spectra
 * @param $scope
 * @param spectrum
 * @param massSpec
 * @constructor
 */
import {Location} from '@angular/common';
import {CookieMain} from '../../services/cookie/cookie-main.service';
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {AuthenticationService} from '../../services/authentication.service';
import {NGXLogger} from 'ngx-logger';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {first} from 'rxjs/operators';
import {SpectrumCacheService} from '../../services/cache/spectrum-cache.service';
import {OrderbyPipe} from '../../filters/orderby.pipe';
import {ActivatedRoute, Router} from '@angular/router';
import {faAngleRight, faAngleDown} from '@fortawesome/free-solid-svg-icons';
import {faQuestionCircle, faFlask} from '@fortawesome/free-solid-svg-icons';
import {faSpinner} from '@fortawesome/free-solid-svg-icons';
import {NgbAccordion} from '@ng-bootstrap/ng-bootstrap';
import {SpectrumModel} from "../../mocks/spectrum.model";

@Component({
    selector: 'spectrum-viewer',
    templateUrl: '../../views/spectra/display/viewSpectrum.html'
})
export class SpectrumViewerComponent implements OnInit, AfterViewInit{
    @ViewChild('acc') accordion: NgbAccordion;
    delayedspectrum: SpectrumModel;
    spectrum: SpectrumModel;
    score;
    massSpec;
    accordionStatus;
    ionTableSort;
    loadingSimilarSpectra;
    similarSpectra;
    massRegex;
    truncateDecimal;
    truncateMass;
    ionRegex;
    match;
    intensity;
    showScore;
    id;
    faAngleRight = faAngleRight;
    faAngleDown = faAngleDown;
    faQuestionCircle = faQuestionCircle;
    faFlask = faFlask;
    faSpinner = faSpinner;
    currentFeedback;

    constructor( public logger: NGXLogger,  public cookie: CookieMain,
                 public spectrumService: Spectrum,  public authenticationService: AuthenticationService,
                 public location: Location,  public spectrumCache: SpectrumCacheService,
                 public route: ActivatedRoute,  public router: Router, public orderbyPipe: OrderbyPipe,
                 public feedbackCache: FeedbackCacheService){
      this.currentFeedback = [];
    }

    ngOnInit() {
      this.route.params.subscribe((data) => {
        this.delayedspectrum = this.route.snapshot.data.spectrumResult;
        this.feedbackCache.resolveFeedback(this.delayedspectrum.id).subscribe((res) => {
          this.currentFeedback = res;
        });
        this.accordionStatus = {
          isSpectraOpen: false,
          isIonTableOpen: false,
          isSimilarSpectraOpen: false,
          isCompoundOpen: []
        };
        /**
         * Sort order for the ion table - default m/z ascending
         */
        this.ionTableSort = '-ion';

        /**
         * quality score of our spectrum
         * number
         */
        this.score = 0;

        this.massSpec = [];

        this.showScore = false;

        this.massRegex = /^\s*(\d+\.\d{4})\d*\s*$/;
        /**
         * Loading of similar spectra
         */
        this.loadingSimilarSpectra = true;
        this.similarSpectra = [];

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
        this.setSpectrum();
      });
    }

    ngAfterViewInit() {
      // Have to use timeout timer since canvas won't draw fast enough on first load for masspecPanel
      // Commented out so that it does not automatically open by itself 9/3/25
      // setTimeout(() => {
      //   this.setAccordionStatus();
      // }, 100);
    }

  setAccordionStatus() {
      this.accordion.expand('masspecPanel');
    }

    setSpectrum() {
      // truncate metadata
      if (typeof this.delayedspectrum.metaData !== 'undefined') {
        for (let i = 0; i < this.delayedspectrum.metaData.length; i++) {
          const curMeta = this.delayedspectrum.metaData[i];

          const name = curMeta.name.toLowerCase();

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
          const compoundMeta = this.delayedspectrum.compound[i].metaData;
          for (let j = 0, m = compoundMeta.length; j < m; j++) {
            const metadata = compoundMeta[j];
            const name = metadata.name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
              metadata.value = this.truncateMass(metadata.value);
            }
          }
        }
      }


      // Regular expression to extract ions
      this.ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

      // Parse spectrum string to generate ion list
      let match = this.ionRegex.exec(this.delayedspectrum.spectrum);

      while (match !== null) {
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
        /**
         * Mass spectrum obtained from cache if it exists, otherwise from REST api
         */
        this.spectrum = this.delayedspectrum;

        // Truncate decimal values of m/z
        match[1] = this.truncateMass(match[1]);

        // Store ion
        const intensity = parseFloat(match[2]);

        if (intensity > 0) {
          this.massSpec.push({
            ion: parseFloat(match[1]),
            intensity,
            annotation,
            computed
          });
        }

        match = this.ionRegex.exec(this.delayedspectrum.spectrum);
      }

      if (typeof this.spectrum.compound !== 'undefined') {
        for (let i = 0; i < this.spectrum.compound.length; i++) {
          this.accordionStatus.isCompoundOpen.push(i === 0);
        }
      }
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
    }




    loadSimilarSpectra() {
        if (!this.loadingSimilarSpectra) {
          return;
        }

        this.spectrumService.searchSimilarSpectra({spectrum: this.spectrum.spectrum, minSimilarity: 0.5}).pipe(first()).subscribe(
            (res: any) => {
                const data = res;
                this.similarSpectra = data.filter((x) => x.id !== this.spectrum.id);
                this.loadingSimilarSpectra = false;
            }, (res) => {
                this.loadingSimilarSpectra = false;
            }
        );
    }

    /**
     * @Deprecated
     * displays the spectrum for the given index
     * @param id string containing spectrum id
     */
    viewSpectrum(id) {
        this.accordion.collapse('similarityPanel');
        this.router.navigate([`/spectra/display/${id}`]).then();
    }

    checkNumber(check: any) {
        if (typeof check === 'number'){
            return true;
        }
        return false;
    }

}
