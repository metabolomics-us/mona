/**
 * provides user score
 */

import {Component, Input, OnInit} from "@angular/core";
import {Statistics} from "../../services/persistence/statistics.resource";

@Component({
    selector: 'spectra-score-for-user',
    template: `<ngb-rating [(rate)]="score" [max]="5" [readonly]="true"></ngb-rating>`
})
export class SpectraScoreForUserComponent implements OnInit{
    @Input() public user;
    public score;

    constructor( public statisticsService: Statistics) {}

    ngOnInit() {
        /* Well apparently this doesn't exist anymore, may have happened when backend stuff was rewritten? Would have to dig through
        some commits to see when or why this changed but currently no replacement. Only used on admin page though so not a hard stop.
        this.statisticsService.spectraCount({id: this.user.id}).then((data: any) => {
            this.score = data.score;
        }); */
    }
}
