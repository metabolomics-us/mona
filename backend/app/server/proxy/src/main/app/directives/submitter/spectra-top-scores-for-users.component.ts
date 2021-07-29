/**
 * provides top users scores
 */

import {Component, Inject, Input, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {Statistics} from "../../services/persistence/statistics.resource";
import * as angular from 'angular';

@Component({
    selector: 'spectra-top-scores-for-users',
    templateUrl: '../../views/templates/scores/hallOfFame.html'
})
export class SpectraTopScoresForUsersComponent implements OnInit{
    @Input() private limit;
    private scores;

    constructor(@Inject(Statistics) private statistics: Statistics) {}

    ngOnInit() {
        this.statistics.spectraTopScores().then((data) => {
            this.scores = data;
            this.scores.forEach((x) => {
                x.score -= 0.45;
            })
        });

        console.log(this.scores);
    }
}

angular.module('moaClientApp')
    .directive('spectraTopScoresForUsers', downgradeComponent({
        component: SpectraTopScoresForUsersComponent,
        inputs: ['limit']
    }));
