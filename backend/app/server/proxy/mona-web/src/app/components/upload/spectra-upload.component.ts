/**
 * Created by Gert on 5/28/2014.
 */
import {AuthenticationService} from '../../services/authentication.service';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {NGXLogger} from 'ngx-logger';
import {Component} from '@angular/core';

@Component({
    selector: 'spectra-upload',
    templateUrl: '../../views/spectra/upload/uploadStatus.html'
})
export class SpectraUploadComponent{
    constructor( public authenticationService: AuthenticationService,  public logger: NGXLogger,
                 public uploadLibraryService: UploadLibraryService){}
}
