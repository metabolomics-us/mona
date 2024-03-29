/**
 * Created by nolanguzman on 10/31/2021
 */
import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {AuthenticationService} from '../../services/authentication.service';

@Component({
  selector: 'advanced-uploader-modal',
  templateUrl: '../../views/spectra/upload/advancedUploaderModal.html'
})
export class AdvancedUploadModalComponent {
  constructor(public activeModal: NgbActiveModal,
              public authenticationService: AuthenticationService) {}
}
