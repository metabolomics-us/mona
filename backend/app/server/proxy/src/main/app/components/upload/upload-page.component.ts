import * as angular from 'angular';

class UploadPageController{
    private static $inject = ['AuthenticationService'];
    private AuthenticationService;
    constructor(AuthenticationService) {
        this.AuthenticationService = AuthenticationService;
    }
}

let UploadPageComponent = {
    selector: "uploadPage",
    templateUrl: "../../views/spectra/upload/upload.html",
    bindings: {},
    controller: UploadPageController
}

angular.module('moaClientApp')
    .component(UploadPageComponent.selector, UploadPageComponent);
