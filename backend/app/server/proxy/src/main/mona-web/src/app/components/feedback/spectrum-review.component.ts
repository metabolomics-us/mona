/**
 * Creates or updates a query with the given submitter information
 * Updated by nolanguzman on 10/31/2021
 */

import {AuthenticationService} from '../../services/authentication.service';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from "@angular/router";
import {NGXLogger} from 'ngx-logger';
import {Component, Input, OnInit} from '@angular/core';
import {faComments, faCheck} from '@fortawesome/free-solid-svg-icons';
import {first} from "rxjs/operators";
import {FeedbackCacheService} from "../../services/feedback/feedback-cache.service";
import {Feedback} from "../../services/persistence/feedback.resource";

@Component({
    selector: 'spectrum-review',
    templateUrl: '../../views/templates/feedback/spectrumReview.html'
})

export class SpectrumReviewComponent implements OnInit{
    @Input() spectrum;
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
      if(!this.hasSubmitted) {
        this.authenticationService.currentUser.subscribe((data: any) => {
          const payload = {
            monaID: this.spectrum.id,
            userID: data.emailAddress,
            name: 'spectrum_quality',
            value
          };
          this.feedback.save(payload).subscribe(() => {
            this.submitting = false;
            this.submitted = true;
          });
        });
      } else {
        let existingSubmission = null;
        this.authenticationService.currentUser.subscribe((data: any) => {
          this.feedbackCache.resolveFeedback(this.spectrum.id).pipe(first()).subscribe((res) => {
            for (let x of res) {
              if (x.userID === data.emailAddress) {
                existingSubmission = x;
              }
            }
            if (existingSubmission !== null) {
              this.logger.info('Replacing existing feedback submission.');
              existingSubmission.value = value;
              this.feedback.save(existingSubmission).subscribe(() => {
                this.submitting = false;
                this.submitted = true;
              });
            }
          });
        });
      }
    }

    checkExistingFeedback() {
      this.authenticationService.currentUser.pipe(first()).subscribe((data) => {
        this.feedbackCache.resolveFeedback(this.spectrum.id).pipe(first()).subscribe((res: any) => {
          for(let x of res) {
            if (x.userID === data.emailAddress) {
              this.hasSubmitted = true;
              this.existingFeedback = x;
            }
          }
        });
      });
    }
}
