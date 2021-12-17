import {Component, Input, OnInit} from "@angular/core";
import {faUserCircle, faFlask} from "@fortawesome/free-solid-svg-icons";
import {Feedback} from "../../services/persistence/feedback.resource";
import {FeedbackCacheService} from "../../services/feedback/feedback-cache.service";
import {first} from "rxjs/operators";

@Component({
  selector: 'spectrum-feedback-results-curation',
  templateUrl: '../../views/templates/feedback/spectrumFeedbackResultsCuration.html'
})

export class SpectrumFeedbackResultsCuration implements OnInit {
  @Input() spectrumID;
  currentFeedback;
  curated_feedback;
  community_feedback;
  faUserCircle = faUserCircle;
  faFlask = faFlask;
  isCurated;
  clean_count;
  noisy_count;

  constructor(public feedback: Feedback, public feedbackCache: FeedbackCacheService) {
    this.currentFeedback = [];
  }

  ngOnInit() {
    this.feedbackCache.resolveFeedback(this.spectrumID).pipe(first()).subscribe((res) => {
      this.currentFeedback = res;
    });
    this.curated_feedback = null;
    this.community_feedback = null;
    this.isCurated = false;
    this.clean_count = 0;
    this.noisy_count = 0;

    if(this.currentFeedback.length > 0) {
      for(let x of this.currentFeedback) {
        if(x.userID === 'curationAdmin') {
          this.curated_feedback = x;
          this.isCurated = true;
          break;
        } else {
          if(x.value === 'clean') {
            this.clean_count++;
          } else if (x.value === 'noisy') {
            this.noisy_count++;
          }
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
}
