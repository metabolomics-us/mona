/**
 * Created by wohlgemuth on 10/16/14.
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, Input, OnInit} from '@angular/core';
import {SpectrumCacheService} from '../../services/cache/spectrum-cache.service';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {faExternalLinkAlt} from '@fortawesome/free-solid-svg-icons';
import {MassDeletionService} from '../../services/persistence/mass-deletion.service';
import {AuthenticationService} from '../../services/authentication.service';
import {SpectrumModel} from "../../mocks/spectrum.model";

@Component({
    selector: 'display-spectra-panel',
    templateUrl: '../../views/spectra/display/panel.html'
})

export class SpectraPanelComponent implements OnInit{
    @Input() spectrum: SpectrumModel;
    currentFeedback;
    IMPORTANT_METADATA;
    importantMetadata;
    secondaryMetadata;
    faExternalLinkAlt = faExternalLinkAlt;
    deletionMark;

    constructor( public spectrumCache: SpectrumCacheService, public feedbackCache: FeedbackCacheService,
                 public massDelete: MassDeletionService, public auth: AuthenticationService) {
      this.currentFeedback = [];
    }

    ngOnInit() {
        // Top 10 important metadata fields
        this.IMPORTANT_METADATA = [
            'ms level', 'precursor type', 'precursor m/z', 'instrument', 'instrument type',
            'ionization', 'ionization mode', 'collision energy', 'retention time', 'retention index',
            'spectral entropy', 'normalized entropy'
        ];

        this.importantMetadata = [];
        this.secondaryMetadata = [];

        this.deletionMark = this.massDelete.getObject(this.spectrum.id);
        if (typeof this.deletionMark === 'undefined') {
          this.deletionMark = {
            id: this.spectrum.id,
            selected: false
          };
          this.massDelete.addForDeletion(this.deletionMark);
        }

        if (typeof this.spectrum.score === 'undefined') {
          this.spectrum.score = {score: 0, relativeScore: 0, scaledScore: 0, impacts: []};
        }

        this.spectrum.metaData.forEach((metaData, index) => {
            metaData.value = this.truncateDecimal(metaData.value, 4);

            if (this.IMPORTANT_METADATA.indexOf(metaData.name.toLowerCase()) > -1) {
                this.importantMetadata.push(metaData);
            } else {
                this.secondaryMetadata.push(metaData);
            }
        });

        this.importantMetadata = this.importantMetadata.sort((a, b) =>  {
            if (this.IMPORTANT_METADATA.indexOf(b.name.toLowerCase()) < this.IMPORTANT_METADATA.indexOf(a.name.toLowerCase())){
                return -1;
            }
            if (this.IMPORTANT_METADATA.indexOf(b.name.toLowerCase()) > this.IMPORTANT_METADATA.indexOf(a.name.toLowerCase())){
                return 1;
            }
            else{
                return 0;
            }
        });

        this.spectrum.metaData = this.importantMetadata.concat(this.secondaryMetadata).slice(0, 12);

        this.feedbackCache.resolveFeedback(this.spectrum.id).subscribe((res) => {
          this.currentFeedback = res;
        });
    }

    truncateDecimal(s, length) {
        return (typeof(s) === 'number') ?  s.toFixed(length) :  s;
    }

    /**
     * displays the spectrum for the given index
     */
    viewSpectrum(): string {
        this.spectrumCache.setSpectrum(this.spectrum);
        return '/spectra/display/' + this.spectrum.id;
    }

    massDeleteToggle(e: any) {
      this.massDelete.toggleCheckbox(this.spectrum.id);
      this.deletionMark.selected = !this.deletionMark.selected;
    }

    sameSubmitter(): boolean {
      if (this.auth.isLoggedIn()) {
        if (this.auth.getCurrentUser().emailAddress === this.spectrum.submitter.emailAddress) {
          return true;
        }
      }
      return false;
    }
}
