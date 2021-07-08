/**
 * Creates or updates a query with the given submitter information
 */

import {AuthenticationService} from "../../services/authentication.service";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {NGXLogger} from "ngx-logger";
import {Component, Inject, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'spectrumReview',
    templateUrl: '../../views/templates/feedback/spectrumReview.html'
})

export class SpectrumReviewComponent{
    protected submitting;
    protected submitted;
    @Input() private spectrum;

    constructor(@Inject([AuthenticationService, HttpClient, NGXLogger])
            private authenticationService: AuthenticationService, private http: HttpClient,
            private logger: NGXLogger) {}

    $onInit = () => {
        this.submitting = false;
        this.submitted = false;
    }

    rate = (value) => {
        this.authenticationService.getCurrentUser().then((data: any) => {
            let payload = {
                monaID: this.spectrum.id,
                userID: data.username,
                name: 'spectrum_quality',
                value: value
            };

            this.http.post(`${environment.REST_BACKEND_SERVER}rest/feedback`, payload).subscribe((data) => {
                this.submitting = false;
                this.submitted = true;
            });
        });
    };
}

angular.module('moaClientApp')
    .directive('spectrumReview', downgradeComponent({
        component: SpectrumReviewComponent,
        inputs: ['spectrum']
    }));
