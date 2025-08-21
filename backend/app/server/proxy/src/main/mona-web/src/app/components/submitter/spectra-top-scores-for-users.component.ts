/**
 * Updated by nolanguzman on 10/31/2021
 * provides top users scores
 */

import {Component, Input, OnInit} from '@angular/core';
import {Statistics} from '../../services/persistence/statistics.resource';
import {faTrophy, faSpinner} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'spectra-top-scores-for-users',
    templateUrl: '../../views/templates/scores/hallOfFame.html'
})
export class SpectraTopScoresForUsersComponent implements OnInit{
    @Input() limit;
    scores;
    faTrophy = faTrophy;
    faSpinner = faSpinner;

    constructor( public statistics: Statistics) {}

    ngOnInit() {
        this.statistics.spectraTopScores().subscribe((data) => {
            this.scores = data;
            // this.scores.forEach((x) => {
            //     x.score -= 0.45;
            // });
        });
    }
}
