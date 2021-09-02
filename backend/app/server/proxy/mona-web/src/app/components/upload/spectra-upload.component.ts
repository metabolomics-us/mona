/**
 * Created by Gert on 5/28/2014.
 */
import {AuthenticationService} from '../../services/authentication.service';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {faSpinner, faExclamationTriangle, faMinusSquare, faPlusSquare, faSave} from '@fortawesome/free-solid-svg-icons';
import {NGXLogger} from 'ngx-logger';
import {Component} from '@angular/core';

@Component({
    selector: 'spectra-upload',
    templateUrl: '../../views/spectra/upload/uploadStatus.html'
})
export class SpectraUploadComponent{
  faSpinner = faSpinner;
    constructor( public authenticationService: AuthenticationService,  public logger: NGXLogger,
                 public uploadLibraryService: UploadLibraryService){}
}
