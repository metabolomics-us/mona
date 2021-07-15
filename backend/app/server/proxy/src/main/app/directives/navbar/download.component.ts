/*
 * renders download button for nav bar
 */

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'download-button',
    templateUrl: '../../views/navbar/download.html'
})
export class DownloadComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('downloadButton', downgradeComponent({
        component: DownloadComponent
    }));
