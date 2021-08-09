import {AuthenticationService} from "../../services/authentication.service";
import {Component} from "@angular/core";

@Component({
    selector: 'upload-page',
    templateUrl: '../../views/spectra/upload/upload.html'
})
export class UploadPageComponent {
    constructor( public authenticationService: AuthenticationService) {}

}
