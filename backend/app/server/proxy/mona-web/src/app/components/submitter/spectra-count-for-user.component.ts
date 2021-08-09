/**
 * provides us with some feedback how many spectra a certain person uploaded
 */

import {Statistics} from "../../services/persistence/statistics.resource";
import {Component, Input, OnInit} from "@angular/core";

@Component({
    selector: 'spectra-count-for-user',
    template: '<span>{{spectraCount}}</span>'
})
export class SpectraCountForUserComponent implements OnInit{
    @Input() public user;
    public spectraCount;
    constructor( public statisticsService: Statistics) {
        this.spectraCount = "loading...";
    }

    ngOnInit() {
        this.statisticsService.spectraCount({id: this.user.id}).then((data: any) => {
            this.spectraCount = data.count;
        })
    }

}