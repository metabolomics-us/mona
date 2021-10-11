/**
 * Creates or updates a query with the given submitter information
 * Updated by nolanguzman on 10/31/2021
 */

import {AuthenticationService} from '../../services/authentication.service';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Component, Input, OnInit} from '@angular/core';
import {faComments, faCheck} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'spectrum-review',
    templateUrl: '../../views/templates/feedback/spectrumReview.html'
})

export class SpectrumReviewComponent implements OnInit{
    submitting;
    submitted;
    @Input() spectrum;
    faComments = faComments;
    faCheck = faCheck;

    constructor( public authenticationService: AuthenticationService,  public http: HttpClient,
                 public logger: NGXLogger) {}

    ngOnInit() {
        this.submitting = false;
        this.submitted = false;
    }

    rate(value) {
        this.authenticationService.currentUser.subscribe((data: any) => {
            const payload = {
                monaID: this.spectrum.id,
                userID: data.emailAddress,
                name: 'spectrum_quality',
                value
            };
            this.http.post(`${environment.REST_BACKEND_SERVER}/rest/feedback`, payload).subscribe(() => {
                this.submitting = false;
                this.submitted = true;
            });
        });
    }
}
