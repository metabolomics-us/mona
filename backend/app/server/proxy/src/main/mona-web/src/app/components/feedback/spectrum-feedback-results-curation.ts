import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {faUserCircle, faFlask} from "@fortawesome/free-solid-svg-icons";
import {Feedback} from "../../services/persistence/feedback.resource";
import {FeedbackCacheService} from "../../services/feedback/feedback-cache.service";
import {first} from "rxjs/operators";

@Component({
  selector: 'spectrum-feedback-results-curation',
  templateUrl: '../../views/templates/feedback/spectrumFeedbackResultsCuration.html'
})

export class SpectrumFeedbackResultsCuration implements OnInit, OnChanges {
  @Input() spectrum;
  currentFeedback;
  curated_feedback;
  community_feedback;
  faUserCircle = faUserCircle;
  faFlask = faFlask;
  clean_count;
  noisy_count;
  normalized_entropy;
  spectral_entropy;

  constructor(public feedback: Feedback, public feedbackCache: FeedbackCacheService) {
    this.currentFeedback = [];
    this.normalized_entropy = 0.0;
    this.spectral_entropy = 0.0;
  }

  ngOnInit() {
    this.curated_feedback = null;
    this.community_feedback = null;
    this.clean_count = 0;
    this.noisy_count = 0;

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
    if(this.currentFeedback.length > 0) {
      for(let x of this.currentFeedback) {
        if(x.value === 'clean') {
          this.clean_count++;
        } else if (x.value === 'noisy') {
          this.noisy_count++;
        }
      }

      if (this.clean_count > this.noisy_count) {
        this.community_feedback = {value: 'clean'};
      } else if (this.clean_count === this.noisy_count) {
        this.community_feedback = {value: 'neutral'};
      } else {
        this.community_feedback = {value: 'noisy'};
      }
    }
  }

  calculateCurated() {
    this.normalized_entropy = this.spectrum.metaData.filter((metadata) => {
      return metadata.name === 'normalized entropy';
    });

    this.spectral_entropy = this.spectrum.metaData.filter((metadata) => {
      return metadata.name === 'spectral entropy';
    });

    if(this.normalized_entropy[0].value >= 0.8 && this.spectral_entropy[0].value >= 3.0) {
      this.curated_feedback = {value: 'noisy'};
    } else {
      this.curated_feedback = {value: 'clean'};
    }
  }
}
