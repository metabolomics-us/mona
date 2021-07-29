/**
 * Created by Gert on 5/28/2014.
 */

import {
    AfterViewInit,
    Component,
    Inject,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChanges,
    ViewChild
} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {FormGroup, FormControl, NgForm} from "@angular/forms";
import {RegistrationService} from "../../services/registration.service";
import {Output, EventEmitter} from "@angular/core";
import {ElementRef} from "@angular/core";
import * as angular from 'angular';
import {NGXLogger} from "ngx-logger";

@Component({
    selector: 'submitter-form',
    templateUrl: '../../views/submitters/template/createUpdateForm.html'
})
export class SubmitterFormComponent implements AfterViewInit, OnDestroy{
    private formErrors;
    @Output() formStatus = new EventEmitter<boolean>();
    @ViewChild('submitterForm') form: NgForm
    private validStatus: boolean;

    constructor(@Inject(RegistrationService) public registrationService: RegistrationService,
                @Inject(NGXLogger) private logger: NGXLogger) {}

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

angular.module('moaClientApp')
    .directive('submitterForm', downgradeComponent({
        component: SubmitterFormComponent,
        outputs: ['formStatus']
    }))
