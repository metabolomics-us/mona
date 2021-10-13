import {AuthenticationService} from '../../services/authentication.service';
import {Component} from '@angular/core';
import {faCloudUploadAlt, faUser, faFile, faDatabase} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'upload-page',
    templateUrl: '../../views/spectra/upload/upload.html'
})
export class UploadPageComponent {
    faCloudUploadAlt = faCloudUploadAlt;
    faUser = faUser;
    faFile = faFile;
    faDatabase = faDatabase;
    constructor( public authenticationService: AuthenticationService) {}

}
