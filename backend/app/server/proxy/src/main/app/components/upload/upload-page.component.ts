import * as angular from 'angular';
import {AuthenticationService} from "../../services/authentication.service";
import {Component, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'upload-page',
    templateUrl: '../../views/spectra/upload/upload.html'
})
export class UploadPageComponent {
    constructor(@Inject(AuthenticationService) private authenticationService: AuthenticationService) {}

}

angular.module('moaClientApp')
    .directive('uploadPage', downgradeComponent({
        component: UploadPageComponent
    }));
