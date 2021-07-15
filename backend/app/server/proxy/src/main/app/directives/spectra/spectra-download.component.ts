/**
 * Created by wohlgemuth on 6/16/15.
 */

import {Component, Inject, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {HttpClient} from "@angular/common/http";
import {NGXLogger} from "ngx-logger";
import {environment} from "../../environments/environment";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DownloadNotifModalComponent} from "./download-notif-modal.component";
import {DownloadNotifErrorModalComponent} from "./download-notif-error-modal.component";
import * as angular from 'angular';

@Component({
    selector: 'spectra-download',
    templateUrl: '../../views/templates/spectra/download.html'
})
export class SpectraDownloadComponent {
    @Input() private spectrum;

    constructor(@Inject(SpectraQueryBuilderService) private spectraQueryBuilderService: SpectraQueryBuilderService,
                @Inject(NgbModal) private modalService: NgbModal, @Inject(HttpClient) private http: HttpClient,
                @Inject(NGXLogger) private logger: NGXLogger) {}

    /**
     * Emulate the downloading of a file given its contents and name
     * @param data
     * @param filename
     * @param mimetype
     */
    downloadData = (data, filename, mimetype) => {
        let blob = new Blob([data], {type: mimetype});

        if (window.navigator.msSaveOrOpenBlob) {
            // IE 10 Hack
            window.navigator.msSaveBlob(blob, filename);
        } else {
            let hiddenElement = document.createElement('a');
            hiddenElement.href = (window.URL || window.webkitURL).createObjectURL(blob);
            // hiddenElement.target = '_blank';
            hiddenElement.download = filename;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        }
    };

    /**
     * attempts to download a msp file
     */
    downloadAsMSP = () => {
        if (angular.isDefined(this.spectrum)) {
            this.http.get<any>(`${environment.REST_BACKEND_SERVER}/rest/spectra/${this.spectrum.id}`, {headers: {Accept: 'text/msp'}})
                .subscribe((returnData) => {
                    this.downloadData(returnData, this.spectrum.id + '.msp', 'text/msp');
             });
            } else {
                let query = angular.copy(this.spectraQueryBuilderService.getQuery());
                query.format = 'msp';

                this.submitQueryExportRequest(query);
             }
    };

    /**
     * attempts to download as a mona file
     */
    downloadAsJSON = () => {
        if (angular.isDefined(this.spectrum)) {
            this.http.get<any>(`${environment.REST_BACKEND_SERVER}/rest/spectra/${this.spectrum.id}`,
                    {'headers': {'Accept': 'application/json'}}).subscribe((response) => {
                this.downloadData(JSON.stringify(response), this.spectrum.id+'.json', 'application/json' );
            });
        } else {
            let query = angular.copy(this.spectraQueryBuilderService.getQuery());
            query.format = 'json';

            this.submitQueryExportRequest(query);
        }
    };

    /**
     * submit query for exporting and show modal dialog response
     */
    submitQueryExportRequest = (query) => {
        let uri = `${environment.REST_BACKEND_SERVER}/rest/spectra/search/export`;

        this.http.post<any>(uri, query).subscribe(
            (response) => {
                const modalRef = this.modalService.open(DownloadNotifModalComponent);
            },
            (response) => {
                if(response.status === 403) {
                    const modalErrorRef = this.modalService.open(DownloadNotifErrorModalComponent);
                    modalErrorRef.componentInstance.message = "You must be logged in to request a query export.";
                } else {
                    const modalErrorRef = this.modalService.open(DownloadNotifErrorModalComponent);
                    modalErrorRef.componentInstance.message = "Could not reach MoNA server!";
                }
            }
        );
    }
}

angular.module('moaClientApp')
    .directive('spectraDownload', downgradeComponent({
        component: SpectraDownloadComponent,
        inputs: ['spectrum']
    }));
