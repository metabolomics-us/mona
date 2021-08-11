/**
 * Created by sajjan on 12/19/14.
 */

import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {AuthenticationService} from '../../services/authentication.service';
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
            <div class="text-center">{{etaString}}</div>
        </div>
        <div *ngIf="spectraUploadProgress == -1"><i>No Upload Started</i></div>`,
})
export class SpectraUploadProgressComponent implements OnInit{
    public etaString;
    public completedSpectraCount;
    public uploadedSpectraCount;
    public failedSpectraCount;
    public spectraUploadProgress;
    public spectraUploadProgressString;

    constructor( public uploadLibraryService: UploadLibraryService,
                 public authenticationService: AuthenticationService) {}

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
        this.uploadLibraryService.uploadProcess.subscribe((isUploading: boolean) => {
          console.log(isUploading);
          if (isUploading) {
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
        });
    }

    buildEtaString = () => {
        if (this.uploadLibraryService.uploadStartTime === -1 || !this.uploadLibraryService.isUploading()) {
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
