/**
 * Created by sajjan on 12/19/14.
 * Updated by nolanguzman on 10/31/2021
 */

import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {AuthenticationService} from '../../services/authentication.service';
import {faSpinner} from '@fortawesome/free-solid-svg-icons';
import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'spectra-upload-progress-bar',
    template: `<div *ngIf="spectraUploadProgress !== -1">
            <div class="text-center"><i>Processed {{completedSpectraCount}} / {{uploadedSpectraCount}} spectra.</i></div>
            <p>
                <ngb-progressbar [ngClass]="{active: spectraUploadProgress < 100, \'progress-striped\': spectraUploadProgress < 100, \'progress-bar-success\': spectraUploadProgress == 100}" max="100" [value]="spectraUploadProgress">
                    <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" [textContent]="spectraUploadProgressString"></span>
                </ngb-progressbar>
            </p>
            <div class="text-center" *ngIf="showETA">{{etaString}}<fa-icon [icon]="faSpinner" [spin]="true"></fa-icon> </div>
        </div>
        <div class="text-center text-nowrap" *ngIf="spectraUploadProgress === -1"><i>No Upload Started</i></div>`,
})
export class SpectraUploadProgressComponent implements OnInit{
    etaString;
    completedSpectraCount;
    uploadedSpectraCount;
    failedSpectraCount;
    spectraUploadProgress;
    spectraUploadProgressString;
    showETA;
    faSpinner = faSpinner;

    constructor( public uploadLibraryService: UploadLibraryService,
                 public authenticationService: AuthenticationService) {}

    ngOnInit() {
        this.etaString = '';
        this.showETA = true;
        this.spectraUploadProgress = -1;

        this.uploadLibraryService.uploadProcess.subscribe((isUploading: boolean) => {
          if (isUploading) {
            // TODO: undo this?
            // Temporarily counting completed and failed uploads together
            this.completedSpectraCount = this.uploadLibraryService.completedSpectraCount + this.uploadLibraryService.failedSpectraCount;
            this.uploadedSpectraCount = this.uploadLibraryService.uploadedSpectraCount;

            this.spectraUploadProgress = (this.completedSpectraCount / this.uploadedSpectraCount) * 100;
            this.spectraUploadProgressString = this.spectraUploadProgress + '%';
          }
          else if (!isUploading && this.uploadLibraryService.isSTP) {
            this.spectraUploadProgressString = 'Working on Next Batch of Spectra...';
          }
          else if (!isUploading && !this.uploadLibraryService.isSTP) {
            this.completedSpectraCount = this.uploadLibraryService.completedSpectraCount + this.uploadLibraryService.failedSpectraCount;
            this.uploadedSpectraCount = this.uploadLibraryService.uploadedSpectraCount;
            this.showETA = false;
            this.spectraUploadProgressString = 'STP Completed!';
          }
          else {
            this.spectraUploadProgress = -1;
            this.spectraUploadProgressString = 'Still Processing...';
          }
          this.buildEtaString();
        });
    }

    buildEtaString() {
        if (this.uploadLibraryService.isSTP) {
          this.etaString = 'Uploading in batches...';
        }
        else if (this.uploadLibraryService.uploadStartTime === -1) {
            this.etaString = '';
        } else if (this.completedSpectraCount === 0) {
            this.etaString = 'Loading spectra for processing...';

        } else {
            // Calculate estimated time remaining
            const dt = new Date().getTime() - this.uploadLibraryService.uploadStartTime;
            const eta = dt * (this.uploadedSpectraCount - this.completedSpectraCount) / this.completedSpectraCount / 1000;

            const seconds = Math.floor(eta % 60);
            const minutes = Math.floor((eta / 60) % 60);
            const hours = Math.floor(eta / 3600);

            let etaString = '';

            if (hours > 0) { etaString += ' ' + hours + ' hours'; }
            if (minutes > 0 || hours > 0) { etaString += ' ' + minutes + ' minutes'; }
            if (seconds > 0 || minutes > 0 || hours > 0) { etaString += ' ' + seconds + ' seconds'; }

            if (etaString !== '') {
                this.etaString = 'Estimated' + etaString + ' remaining';
            } else {
                this.etaString = '';
            }
        }
    }
}
