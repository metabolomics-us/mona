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
import {Component, Input, OnInit} from "@angular/core";
import {first, map} from "rxjs/operators";
import {SpectrumCacheService} from "../../services/cache/spectrum-cache.service";
import {OrderbyPipe} from "../../filters/orderby.pipe";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {BehaviorSubject} from "rxjs";
import {faAngleRight, faAngleDown} from "@fortawesome/free-solid-svg-icons";
import {faQuestionCircle, faFlask} from "@fortawesome/free-solid-svg-icons";
import {faSpinner} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'spectrum-viewer',
    templateUrl: '../../views/spectra/display/viewSpectrum.html'
})
export class SpectrumViewerComponent implements OnInit{
    public delayedspectrum;
    public spectrum;
    public score;
    public massSpec;
    public accordionStatus;
    ionTableSort;
    ionTableSortReverse;
    public loadingSimilarSpectra;
    public similarSpectra;
    public massRegex;
    public truncateDecimal;
    public truncateMass;
    public ionRegex;
    public match;
    public intensity;
    public showScore;
    public id;
    public accordionStatusSubject;
    faAngleRight = faAngleRight;
    faAngleDown = faAngleDown;
    faQuestionCircle = faQuestionCircle;
    faFlask = faFlask;
    faSpinner = faSpinner;

    constructor( public logger: NGXLogger,  public cookie: CookieMain,
                 public spectrumService: Spectrum,  public authenticationService: AuthenticationService,
                 public location: Location,  public spectrumCache: SpectrumCacheService,
                 public route: ActivatedRoute,  public router: Router, public orderbyPipe: OrderbyPipe){

    }

    ngOnInit() {
      this.delayedspectrum = this.route.snapshot.data.spectrum;
      console.log(this.route.snapshot.data);
      console.log(this.delayedspectrum);
      this.accordionStatus = {
        isSpectraOpen: true,
        isIonTableOpen: false,
        isSimilarSpectraOpen: false,
        isCompoundOpen: []
      };
      /**
       * Sort order for the ion table - default m/z ascending
       */
      this.ionTableSort = '-ion';
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
      this.setAccordionStatus();
      console.log(this.spectrum);
    }

    setAccordionStatus() {
      /**
       * status of our accordion
       * @type {{isBiologicalCompoundOpen: boolean, isChemicalCompoundOpen: boolean, isDerivatizedCompoundOpen: boolean}}
       */

      this.accordionStatusSubject = new BehaviorSubject<object>(this.accordionStatus);
      /**
       * watch the accordion status and updates related cookies
       */
      this.accordionStatusSubject.subscribe(() => {
        for (let key in this.accordionStatus) {
          if (key === 'isCompoundOpen') {
            console.log(this.spectrum);
            for (let i = 0; i < this.spectrum.compound.length; i++) {
              console.log(this.accordionStatus[key][i])
              this.cookie.update('DisplayCompound' + i, this.accordionStatus[key][i]);
            }
          } else {
            console.log(this.accordionStatus[key]);
            this.cookie.update("DisplaySpectra" + key, this.accordionStatus[key]);
          }
        }
      });
    }

    setSpectrum(): void {
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
        /**
         * Mass spectrum obtained from cache if it exists, otherwise from REST api
         * */
        this.spectrum = this.delayedspectrum;

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

      if (typeof this.spectrum.compound !== 'undefined') {
        for (let i = 0; i < this.spectrum.compound.length; i++) {
          this.accordionStatus.isCompoundOpen.push(i === 0);
        }
      }
    }

    sortIonTable(column) {
        if (column === 'ion') {
            if (this.ionTableSort === '+ion') this.ionTableSort = '-ion'; else this.ionTableSort = '+ion';
        }
        else if (column === 'intensity') {
            if (this.ionTableSort === '+intensity') this.ionTableSort = '-intensity'; else this.ionTableSort = '+intensity';
        }
        else if (column === 'annotation') {
          if (this.ionTableSort === '+annotation') this.ionTableSort = '-annotation'; else this.ionTableSort = '+annotation'
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