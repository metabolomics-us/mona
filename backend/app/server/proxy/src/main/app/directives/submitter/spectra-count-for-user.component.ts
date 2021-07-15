/**
 * provides us with some feedback how many spectra a certain person uploaded
 */

import {Statistics} from "../../services/persistence/statistics.resource";
import {Component, Inject, Input, OnInit} from "@angular/core";
import * as angular from 'angular';
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'spectra-count-for-user',
    template: '<span>{{spectraCount}}</span>'
})
export class SpectraCountForUserComponent implements OnInit{
    @Input() private user;
    private spectraCount;
    constructor(@Inject(Statistics) private statisticsService: Statistics) {
        this.spectraCount = "loading...";
    }

    ngOnInit() {
        this.statisticsService.spectraCount({id: this.user.id}).then((data: any) => {
            this.spectraCount = data.count;
        })
    }

}

angular.module('moaClientApp')
    .directive('spectraCountForUser', downgradeComponent({
        component: SpectraCountForUserComponent,
        inputs: ['user']
    }));
