/**
 * Created by Gert on 5/28/2014.
 */
import {AuthenticationService} from "../../services/authentication.service";
import {UploadLibraryService} from "../../services/upload/upload-library.service";
import {NGXLogger} from "ngx-logger";
import * as angular from 'angular';
import {Component, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'spectra-upload',
    templateUrl: '../../views/spectra/upload/uploadStatus.html'
})
export class SpectraUploadComponent{
    private AuthenticationService;
    private UploadLibraryService;
    private $log;

    constructor(@Inject(AuthenticationService) private authenticationService: AuthenticationService, @Inject(NGXLogger) private logger: NGXLogger,
                @Inject(UploadLibraryService) private uploadLibraryService: UploadLibraryService){}
}

angular.module('moaClientApp')
    .directive('spectraUpload', downgradeComponent({
        component: SpectraUploadComponent
    }));
