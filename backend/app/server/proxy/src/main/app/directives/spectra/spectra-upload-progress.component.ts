/**
 * Created by sajjan on 12/19/14.
 */

import {UploadLibraryService} from "../../services/upload/upload-library.service";
import {AuthenticationService} from "../../services/authentication.service";
import {Component, Inject, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'spectra-upload-progress-bar',
    template: `<div *ngIf="spectraUploadProgress != -1">
            <div class="text-center"><i>Processed {{completedSpectraCount}} / {{uploadedSpectraCount}} spectra.</i></div>
            <p>
                <ngb-progressbar [ngClass]="{active: spectraUploadProgress < 100, \'progress-striped\': spectraUploadProgress < 100, \'progress-bar-success\': spectraUploadProgress == 100}" max="100" value="spectraUploadProgress">
                    <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" [textContent]="spectraUploadProgressString"></span>
                </ngb-progressbar>
            </p>
            <div class="text-center">{{etaString}}</div>
        </div>
        <div *ngIf="spectraUploadProgress == -1"><i>No Upload Started</i></div>`,
})
export class SpectraUploadProgressComponent implements OnInit{
    private etaString;
    private completedSpectraCount;
    private uploadedSpectraCount;
    private failedSpectraCount;
    private spectraUploadProgress;
    private spectraUploadProgressString;

    constructor(@Inject(UploadLibraryService) private uploadLibraryService: UploadLibraryService,
                @Inject(AuthenticationService) private authenticationService: AuthenticationService) {}

    ngOnInit(): void {
        this.etaString = '';

        /*this.$scope.$on('spectra:uploadprogress', (event, completedSpectraCount, failedSpectraCount, uploadedSpectraCount) => {
            this.completedSpectraCount = completedSpectraCount + failedSpectraCount;
            this.failedSpectraCount = failedSpectraCount;
            this.uploadedSpectraCount = uploadedSpectraCount;

            this.spectraUploadProgress = (this.completedSpectraCount / this.uploadedSpectraCount) * 100;
            this.spectraUploadProgressString = this.spectraUploadProgress + '%';
            this.buildEtaString();
        }); */
        console.log(this.uploadLibraryService);
        if (this.uploadLibraryService.isUploading()) {
            // Temporarily counting completed and failed uploads together
            this.completedSpectraCount = this.uploadLibraryService.completedSpectraCount + this.uploadLibraryService.failedSpectraCount;
            this.uploadedSpectraCount = this.uploadLibraryService.uploadedSpectraCount;

            this.spectraUploadProgress = (this.completedSpectraCount / this.uploadedSpectraCount) * 100;
            this.spectraUploadProgressString = this.spectraUploadProgress + '%';
        } else {
            this.spectraUploadProgress = -1;
            this.spectraUploadProgressString = 'Processing...';
        }

        this.buildEtaString();
    }

    buildEtaString = () => {
        if (this.uploadLibraryService.uploadStartTime === -1 || !this.uploadLibraryService.isUploading()) {
            this.etaString = '';
        } else if (this.completedSpectraCount === 0) {
            this.etaString = 'Loading spectra for processing...'

        } else {
            // Calculate estimated time remaining
            let dt = new Date().getTime() - this.uploadLibraryService.uploadStartTime;
            let eta = dt * (this.uploadedSpectraCount - this.completedSpectraCount) / this.completedSpectraCount / 1000;

            let seconds = Math.floor(eta % 60);
            let minutes = Math.floor((eta / 60) % 60);
            let hours = Math.floor(eta / 3600);

            let etaString = '';

            if (hours > 0) etaString += ' ' + hours + ' hours';
            if (minutes > 0 || hours > 0) etaString += ' ' + minutes + ' minutes';
            if (seconds > 0 || minutes > 0 || hours > 0) etaString += ' ' + seconds + ' seconds';

            if (etaString !== '') {
                this.etaString = 'Estimated' + etaString + ' remaining';
            } else {
                this.etaString = '';
            }
        }
    };
}

angular.module('moaClientApp')
    .directive('spectraUploadProgressBar', downgradeComponent({
        component: SpectraUploadProgressComponent
    }));