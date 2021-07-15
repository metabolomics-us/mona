/**
 * provides user score
 */

import {Component, Inject, Input, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {Statistics} from "../../services/persistence/statistics.resource";
import * as angular from 'angular';

@Component({
    selector: 'spectra-score-for-user',
    template: `<ngb-rating [(rate)]="score" [max]="5" [readonly]="true"></ngb-rating>`
})
export class SpectraScoreForUserComponent implements OnInit{
    @Input() private user;
    private score;

    constructor(@Inject(Statistics) private statisticsService: Statistics) {}

    ngOnInit() {
        this.statisticsService.spectraCount({id: this.user.id}).then((data: any) => {
            this.score = data.score;
        });
    }
}

angular.module('moaClientApp')
    .directive('spectraScoreForUser', downgradeComponent({
        component: SpectraScoreForUserComponent,
        inputs: ['user']
    }));
