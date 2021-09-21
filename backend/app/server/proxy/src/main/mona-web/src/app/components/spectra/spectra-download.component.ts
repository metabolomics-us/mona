/**
 * Created by wohlgemuth on 6/16/15.
 * Updated by nolanguzman on 10/31/2021
 */

import {Component, Input} from '@angular/core';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {environment} from '../../../environments/environment';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DownloadNotifModalComponent} from './download-notif-modal.component';
import {DownloadNotifErrorModalComponent} from './download-notif-error-modal.component';
import {faCloudDownloadAlt} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'spectra-download',
    templateUrl: '../../views/templates/spectra/download.html'
})
export class SpectraDownloadComponent {
    @Input() public spectrum;
    faCloudDownloadAlt = faCloudDownloadAlt;

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService,
                public modalService: NgbModal,  public http: HttpClient,
                public logger: NGXLogger) {}

    /**
     * Emulate the downloading of a file given its contents and name
     * @param data object
     * @param filename string
     * @param mimetype string
     */
    downloadData(data, filename, mimetype) {
        const blob = new Blob([data], {type: mimetype});

        if (window.navigator.msSaveOrOpenBlob) {
            // IE 10 Hack
            window.navigator.msSaveBlob(blob, filename);
        } else {
            const hiddenElement = document.createElement('a');
            hiddenElement.href = (window.URL || window.webkitURL).createObjectURL(blob);
            // hiddenElement.target = '_blank';
            hiddenElement.download = filename;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        }
    }

    /**
     * attempts to download a msp file
     */
    downloadAsMSP() {
        if (typeof this.spectrum !== 'undefined') {
          const options = { headers: {Accept: 'text/msp'}, responseType: 'text' as 'text'};
          this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${this.spectrum.id}`, options)
                .subscribe((returnData) => {
                    this.downloadData(returnData, this.spectrum.id + '.msp', 'text/msp');
             }, (error) => {
                  this.logger.warn(error);
                });
            } else {
                const query = this.spectraQueryBuilderService.getQuery();
                query.format = 'msp';

                this.submitQueryExportRequest(query);
             }
    }

    /**
     * attempts to download as a mona file
     */
    downloadAsJSON() {
        if (typeof this.spectrum !== 'undefined') {
            this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${this.spectrum.id}`,
                    {headers: {Accept: 'application/json'}}).subscribe((response) => {
                this.downloadData(JSON.stringify(response), this.spectrum.id + '.json', 'application/json' );
            });
        } else {
            const query = this.spectraQueryBuilderService.getQuery();
            query.format = 'json';

            this.submitQueryExportRequest(query);
        }
    }

    /**
     * submit query for exporting and show modal dialog response
     */
    submitQueryExportRequest(query) {
        const uri = `${environment.REST_BACKEND_SERVER}/rest/spectra/search/export`;

        this.http.post<any>(uri, query).subscribe(
            () => {
                const modalRef = this.modalService.open(DownloadNotifModalComponent);
            },
            (response) => {
                if (response.status === 403) {
                    const modalErrorRef = this.modalService.open(DownloadNotifErrorModalComponent);
                    modalErrorRef.componentInstance.message = 'You must be logged in to request a query export.';
                } else {
                    const modalErrorRef = this.modalService.open(DownloadNotifErrorModalComponent);
                    modalErrorRef.componentInstance.message = 'Could not reach MoNA server!';
                }
            }
        );
    }
}
