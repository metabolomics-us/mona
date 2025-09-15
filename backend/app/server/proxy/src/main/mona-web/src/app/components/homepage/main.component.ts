/**
 * Updated by nolanguzman on 10/31/2021
 */
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {NGXLogger} from 'ngx-logger';
import {NgcCookieConsentService} from "ngx-cookieconsent";
import {Subscription} from "rxjs";
import {Component, OnDestroy, OnInit} from '@angular/core';
import {first} from 'rxjs/operators';
import {faSearch, faChartBar, faExclamationTriangle, faSpinner} from '@fortawesome/free-solid-svg-icons';
import {SpectrumModel} from "../../mocks/spectrum.model";

@Component({
    selector: 'main',
    templateUrl: '../../views/main.html'
})
export class MainComponent implements OnInit, OnDestroy{
    private popupOpenSubscription!: Subscription;
    showcaseSpectraIds;
    showcaseSpectra: SpectrumModel[];
    totalCount;
    faSearch = faSearch;
    faChartBar = faChartBar;
    faExclamationTriangle = faExclamationTriangle;
    faSpinner = faSpinner;

    constructor( public spectrum: Spectrum,  public logger: NGXLogger, private ccService: NgcCookieConsentService) {
      this.totalCount = 0;
    }

    ngOnInit() {
      this.calculateResultCount();
        this.popupOpenSubscription = this.ccService.popupOpen$.subscribe(() => {

        });

        //this.showcaseSpectraIds = ['MoNA_0000001', 'MoNA_0000004']; // FOR DEV
        this.showcaseSpectraIds = ['BSU00002', 'AU101801', 'UT001119'];
        this.showcaseSpectra = [];

        this.showcaseSpectraIds.forEach((id) => {
            this.spectrum.get(
                id).pipe(first()).subscribe(
                (data: SpectrumModel) => {
                  this.showcaseSpectra.push(data);
                },
                (error) => {
                    this.logger.error('Failed to obtain spectrum ' + id);
                }
            );
        });
    }

    ngOnDestroy() {
      this.popupOpenSubscription.unsubscribe();
    }

    calculateResultCount() {
      this.spectrum.searchSpectraCount({
      }).pipe(first()).subscribe((res: any) => {
        this.totalCount = res.count;
      });
    }
}
