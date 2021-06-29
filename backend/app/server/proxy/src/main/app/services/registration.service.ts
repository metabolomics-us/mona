import * as angular from 'angular';
import {downgradeInjectable} from "@angular/upgrade/static";

export class RegistrationService {
    private newSubmitter;

    constructor() {
    };
}


angular.module('moaClientApp')
    .factory('RegistrationService', downgradeInjectable(RegistrationService));