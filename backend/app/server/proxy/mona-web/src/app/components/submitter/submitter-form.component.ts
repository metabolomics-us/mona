/**
 * Created by Gert on 5/28/2014.
 */

import {
    AfterViewInit,
    Component,
    OnDestroy,
    ViewChild
} from "@angular/core";
import {RegistrationService} from "../../services/registration.service";
import {Output, EventEmitter} from "@angular/core";
import {NGXLogger} from "ngx-logger";
import {NgForm} from "@angular/forms";

@Component({
    selector: 'submitter-form',
    templateUrl: '../../views/submitters/template/createUpdateForm.html'
})
export class SubmitterFormComponent implements AfterViewInit, OnDestroy{
    public formErrors;
    @Output() formStatus = new EventEmitter<boolean>();
    @ViewChild('submitterForm') form: NgForm
    public validStatus: boolean;

    constructor( public registrationService: RegistrationService,
                 public logger: NGXLogger) {}

    ngAfterViewInit() {
        if(!this.form) return;
        this.form.valueChanges.subscribe(_ => {
            if(this.validStatus !== this.form.valid) {
                this.validStatus = this.form.valid;
                this.formStatus.emit(this.form.valid);
            }
        });
    }

    ngOnDestroy() {
        this.form.resetForm();
    }
}
