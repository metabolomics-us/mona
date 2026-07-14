/**
 * Created by wohlgemuth on 10/16/14.
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {SpectrumCacheService} from '../../services/cache/spectrum-cache.service';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {faExternalLinkAlt, faTrash, faUser} from '@fortawesome/free-solid-svg-icons';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';
import {faStar as faStarEmpty } from '@fortawesome/free-regular-svg-icons';
import {AuthenticationService} from '../../services/authentication.service';
import {SpectrumModel} from '../../mocks/spectrum.model';
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {ToasterService} from 'angular2-toaster';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DeleteConfirmModalComponent} from '../browser/delete-confirm-modal.component';

@Component({
    selector: 'display-spectra-panel',
    templateUrl: '../../views/spectra/display/panel.html'
})

export class SpectraPanelComponent implements OnInit{
    @Input() spectrum: SpectrumModel;
    @Output() deleted = new EventEmitter<string>();
    currentFeedback;
    IMPORTANT_METADATA;
    importantMetadata;
    secondaryMetadata;
    faExternalLinkAlt = faExternalLinkAlt;
    faTrash = faTrash;
    faUser = faUser;
    faStar = faStar;
    faStarEmpty = faStarEmpty;
    faStarHalf = faStarHalfAlt;

    constructor( public spectrumCache: SpectrumCacheService, public feedbackCache: FeedbackCacheService,
                 public auth: AuthenticationService, public spectrumResource: Spectrum,
                 public toaster: ToasterService, public modalService: NgbModal) {
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

    deleteSpectrum() {
      const modalRef = this.modalService.open(DeleteConfirmModalComponent);
      modalRef.componentInstance.message = 'Are you sure you want to delete spectrum <strong>' + this.spectrum.id + '</strong>?';
      modalRef.result.then(() => this.performDelete(), () => {});
    }

    performDelete() {
      const token = this.auth.getCurrentUser().accessToken;
      this.spectrumResource.delete(this.spectrum.id, token).subscribe(() => {
        this.toaster.pop({
          type: 'success',
          title: 'Spectrum Deleted',
          body: `Spectrum ${this.spectrum.id} was successfully deleted.`
        });
        this.deleted.emit(this.spectrum.id);
      }, (error) => {
        this.toaster.pop({
          type: 'error',
          title: 'Delete Failed',
          body: error.message || 'An error occurred while deleting the spectrum.'
        });
      });
    }

    sameSubmitter(): boolean {
      if (this.auth.isLoggedIn()) {
        if (this.auth.getCurrentUser().emailAddress === this.spectrum.submitter.emailAddress) {
          return true;
        }
      }
      return false;
    }

    canDelete(): boolean {
      return this.sameSubmitter() || this.auth.isAdmin();
    }

  get stars() {
    const result: (0 | 0.5 | 1)[] = [];
    let rounded = Math.round(this.spectrum.score.score * 2) / 2; // round to nearest 0.5
    for (let i = 0; i < 5; i++) {
      if (rounded >= 1) {
        result.push(1);
      } else if (rounded === 0.5) {
        result.push(0.5);
      } else {
        result.push(0);
      }
      rounded -= 1;
    }
    return result;
  }
}
