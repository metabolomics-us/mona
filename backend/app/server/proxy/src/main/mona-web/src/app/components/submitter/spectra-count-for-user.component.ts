/**
 * Updated by nolanguzman on 10/31/2021
 * provides us with some feedback how many spectra a certain person uploaded
 */

import {Statistics} from '../../services/persistence/statistics.resource';
import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'spectra-count-for-user',
    template: '<span>{{spectraCount}}</span>'
})
export class SpectraCountForUserComponent implements OnInit{
    @Input() user;
    spectraCount;
    constructor( public statisticsService: Statistics) {
        this.spectraCount = 'loading...';
    }

    ngOnInit() {
        this.statisticsService.spectraCount({id: this.user.id}).subscribe((data: any) => {
            this.spectraCount = data.count;
        });
    }

}
