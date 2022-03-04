import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NGXLogger} from 'ngx-logger';
import {MassDeletionService} from '../../services/persistence/mass-deletion.service';

@Component({
  selector: 'mass-delete-modal',
  templateUrl: '../../views/spectra/browse/massDeleteModal.html'
})
export class MassDeleteModalComponent{
  constructor(public activeModal: NgbActiveModal, public logger: NGXLogger,
              public massDelete: MassDeletionService) {}
}
