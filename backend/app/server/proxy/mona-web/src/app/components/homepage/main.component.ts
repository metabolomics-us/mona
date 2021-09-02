import {Spectrum} from '../../services/persistence/spectrum.resource';
import {NGXLogger} from 'ngx-logger';
import {Component, OnInit} from '@angular/core';
import {first} from 'rxjs/operators';
import {faSearch, faChartBar, faExclamationTriangle, faSpinner} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'main',
    templateUrl: '../../views/main.html'
})
export class MainComponent implements OnInit{
    public showcaseSpectraIds;
    public showcaseSpectra;
    faSearch = faSearch;
    faChartBar = faChartBar;
    faExclamationTriangle = faExclamationTriangle;
    faSpinner = faSpinner;

    constructor( public spectrum: Spectrum,  public logger: NGXLogger) {}

    /* checkHttpError() {
        while (this.$rootScope.httpError.length !== 0) {
            let curError = this.$rootScope.httpError.pop();

            if (angular.isDefined(curError)) {
                let method = curError.config.method;
                let url = curError.config.url;
                let status = curError.status;

                let message = 'Unable to ' + method + ' from ' + url + ' Status: ' + status;

                this.$log.error(message);
            }
        }
    }*/

    ngOnInit(): void {
        this.showcaseSpectraIds = ['MoNA000001', 'MoNA000002', 'MoNA000003'];
        this.showcaseSpectra = [];

        this.showcaseSpectraIds.forEach((id) => {
            this.spectrum.get(
                id).pipe(first()).subscribe(
                (data) => {
                    this.showcaseSpectra.push(data);
                },
                (error) => {
                    this.logger.error('Failed to obtain spectrum ' + id);
                }
            );
        });
    }
}
