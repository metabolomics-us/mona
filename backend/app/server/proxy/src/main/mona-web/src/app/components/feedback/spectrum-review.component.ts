/**
 * Creates or updates a query with the given submitter information
 * Updated by nolanguzman on 10/31/2021
 */

import {AuthenticationService} from '../../services/authentication.service';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {Component, Input, OnInit} from '@angular/core';
import {faComments, faCheck} from '@fortawesome/free-solid-svg-icons';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {Feedback} from '../../services/persistence/feedback.resource';
import {SpectrumModel} from "../../mocks/spectrum.model";

@Component({
    selector: 'spectrum-review',
    templateUrl: '../../views/templates/feedback/spectrumReview.html'
})

export class SpectrumReviewComponent implements OnInit{
    @Input() spectrum: SpectrumModel;
    submitting;
    submitted;
    faComments = faComments;
    faCheck = faCheck;
    hasSubmitted;
    existingFeedback;

    constructor( public authenticationService: AuthenticationService,  public http: HttpClient,
                 public logger: NGXLogger, public feedbackCache: FeedbackCacheService, public feedback: Feedback) {
      this.existingFeedback = null;
    }

    ngOnInit() {
        this.submitting = false;
        this.submitted = false;
        this.hasSubmitted = false;
        this.checkExistingFeedback();
    }

    rate(value) {
      if (!this.hasSubmitted) {
        this.authenticationService.currentUser.subscribe((data: any) => {
          const payload = {
            id: this.spectrum.id,
            emailAddress: data.emailAddress,
            name: 'spectrum_quality',
            value
          };
          this.feedback.save(payload).subscribe(() => {
            this.submitting = false;
            this.submitted = true;
            this.feedbackCache.updateFeedback(this.spectrum.id);
          });
        });
      } else {
        let existingSubmission = null;
        this.authenticationService.currentUser.subscribe((data: any) => {
          this.feedbackCache.resolveFeedback(this.spectrum.id).subscribe((res) => {
            for (const x of res) {
              if (x.emailAddress === data.emailAddress) {
                existingSubmission = x;
              }
            }
            if (existingSubmission !== null) {
              this.logger.info('Replacing existing feedback submission.');
              existingSubmission.value = value;
              this.feedback.save(existingSubmission).subscribe(() => {
                this.submitting = false;
                this.submitted = true;
                this.feedbackCache.updateFeedback(this.spectrum.id);
              });
            }
          });
        });
      }
    }

    checkExistingFeedback() {
      this.authenticationService.currentUser.subscribe((data) => {
        this.feedbackCache.resolveFeedback(this.spectrum.id).subscribe((res: any) => {
          for (const x of res) {
            if (x.emailAddress === data.emailAddress) {
              this.hasSubmitted = true;
              this.existingFeedback = x;
            }
          }
        });
      });
    }
}
