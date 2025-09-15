import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {faUserCircle, faFlask} from '@fortawesome/free-solid-svg-icons';
import {Feedback} from '../../services/persistence/feedback.resource';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {first} from 'rxjs/operators';
import {Router} from '@angular/router';
import {SpectrumModel} from '../../mocks/spectrum.model';

@Component({
  selector: 'spectrum-feedback-results-curation',
  templateUrl: '../../views/templates/feedback/spectrumFeedbackResultsCuration.html'
})

export class SpectrumFeedbackResultsCurationComponent implements OnInit, OnChanges {
  @Input() spectrum: SpectrumModel;
  currentFeedback;
  curatedFeedback;
  communityFeedback;
  faUserCircle = faUserCircle;
  faFlask = faFlask;
  cleanCount;
  noisyCount;
  normalizedEntropy;
  spectralEntropy;

  constructor(public feedback: Feedback, public feedbackCache: FeedbackCacheService, public router: Router) {
    this.currentFeedback = [];
    this.normalizedEntropy = 0.0;
    this.spectralEntropy = 0.0;
  }

  ngOnInit() {
    this.curatedFeedback = null;
    this.communityFeedback = null;
    this.cleanCount = 0;
    this.noisyCount = 0;

    this.feedbackCache.resolveFeedback(this.spectrum.id).pipe(first()).subscribe((res) => {
      this.currentFeedback = res;
      this.calculateCommunity();
      this.calculateCurated();
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    this.calculateCurated();
    this.calculateCommunity();
  }

  calculateCommunity() {
    if (this.currentFeedback.length > 0) {
      for (const x of this.currentFeedback) {
        if (x.value === 'clean') {
          this.cleanCount++;
        } else if (x.value === 'noisy') {
          this.noisyCount++;
        }
      }

      if (this.cleanCount > this.noisyCount) {
        this.communityFeedback = {value: 'clean'};
      } else if (this.cleanCount === this.noisyCount) {
        this.communityFeedback = {value: 'neutral'};
      } else {
        this.communityFeedback = {value: 'noisy'};
      }
    }
  }

  calculateCurated() {
    this.normalizedEntropy = this.spectrum.metaData.filter((metadata) => {
      return metadata.name === 'normalized entropy';
    });

    this.spectralEntropy = this.spectrum.metaData.filter((metadata) => {
      return metadata.name === 'spectral entropy';
    });

    if (this.normalizedEntropy.length > 0 && this.spectralEntropy.length > 0) {
      if (this.normalizedEntropy[0].value >= 0.8 && this.spectralEntropy[0].value >= 3.0 && !this.isGCMS()) {
        this.curatedFeedback = {value: 'noisy'};
      } else {
        this.curatedFeedback = {value: 'clean'};
      }
    } else {
      this.normalizedEntropy = null;
      this.spectralEntropy = null;
    }
  }

  isGCMS(): boolean {
    if (this.spectrum.tags.filter(x => x.text === 'GC-MS').length > 0) {
      return true;
    }
    return false;
  }
}
