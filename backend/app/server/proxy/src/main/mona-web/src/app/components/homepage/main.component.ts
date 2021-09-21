/**
 * Updated by nolanguzman on 10/31/2021
 */
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

    ngOnInit() {
        this.showcaseSpectraIds = ['BSU00002', 'AU101801', 'UT001119'];
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
