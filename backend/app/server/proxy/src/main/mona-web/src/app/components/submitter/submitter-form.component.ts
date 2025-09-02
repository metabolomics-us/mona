/**
 * Created by Gert on 5/28/2014.
 * Updated by nolanguzman on 10/31/2021
 */

import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {RegistrationService} from '../../services/registration.service';
import {Output, EventEmitter} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {NgForm} from '@angular/forms';

@Component({
    selector: 'submitter-form',
    templateUrl: '../../views/submitters/template/createUpdateForm.html'
})
export class SubmitterFormComponent implements AfterViewInit, OnDestroy, OnInit{
    formErrors;
    @Input() submitter;
    @Output() formStatus = new EventEmitter<boolean>();
    @ViewChild('submitterForm') form: NgForm;
    validStatus: boolean;
    @Input() isAdminEdit: false;

    constructor( public registrationService: RegistrationService,
                 public logger: NGXLogger) {}

    ngOnInit() {
      if (this.submitter) {
        this.registrationService.newSubmitter.firstName = this.submitter.firstName;
        this.registrationService.newSubmitter.lastName = this.submitter.lastName;
        this.registrationService.newSubmitter.institution = this.submitter.institution;
        this.registrationService.newSubmitter.emailAddress = this.submitter.emailAddress;
        this.registrationService.newSubmitter.id = this.submitter.id;
      } else {
        this.registrationService.resetSubmitter();
      }
    }

  ngAfterViewInit() {
        if (!this.form) { return; }
        this.form.valueChanges.subscribe(_ => {
            if (this.validStatus !== this.form.valid) {
                this.validStatus = this.form.valid;
                this.formStatus.emit(this.form.valid);
            }
        });
    }

    ngOnDestroy() {
        this.form.resetForm();
    }
}
