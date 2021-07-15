/*
 * renders upload button for nav bar
 */

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'upload-button',
    templateUrl: '../../views/navbar/upload.html'
})
export class UploadComponent {
    constructor() {}

}

angular.module('moaClientApp')
    .directive('uploadButton', downgradeComponent({
        component: UploadComponent
    }));

