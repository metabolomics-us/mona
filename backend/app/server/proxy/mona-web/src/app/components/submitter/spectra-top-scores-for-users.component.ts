/**
 * provides top users scores
 */

import {Component, Input, OnInit} from "@angular/core";
import {Statistics} from "../../services/persistence/statistics.resource";

@Component({
    selector: 'spectra-top-scores-for-users',
    templateUrl: '../../views/templates/scores/hallOfFame.html'
})
export class SpectraTopScoresForUsersComponent implements OnInit{
    @Input() public limit;
    public scores;

    constructor( public statistics: Statistics) {}

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
