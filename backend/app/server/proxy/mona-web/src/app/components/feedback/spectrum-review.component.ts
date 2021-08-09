/**
 * Creates or updates a query with the given submitter information
 */

import {AuthenticationService} from "../../services/authentication.service";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {NGXLogger} from "ngx-logger";
import {Component, Input, OnInit} from "@angular/core";

@Component({
    selector: 'spectrum-review',
    templateUrl: '../../views/templates/feedback/spectrumReview.html'
})

export class SpectrumReviewComponent implements OnInit{
    public submitting;
    public submitted;
    @Input() public spectrum;

    constructor( public authenticationService: AuthenticationService,  public http: HttpClient,
                 public logger: NGXLogger) {}

    ngOnInit(){
        this.submitting = false;
        this.submitted = false;
    }

    rate = (value) => {
        this.authenticationService.currentUser.subscribe((data: any) => {
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
