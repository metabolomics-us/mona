/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';

class SpectraUploadController{
    private static $inject = ['$scope', '$rootScope', 'AuthenticationService', 'UploadLibraryService', '$log'];
    private $scope;
    private $rootScope;
    private AuthenticationService;
    private UploadLibraryService;
    private $log;

    constructor($scope, $rootScope, AuthenticationService, UploadLibraryService, $log){
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.AuthenticationService = AuthenticationService;
        this.UploadLibraryService = UploadLibraryService;
        this.$log = $log;
    }
}

let SpectraUploadComponent = {
    selector: "spectraUpload",
    templateUrl: '../../views/spectra/upload/uploadStatus.html',
    bindings: {},
    controller: SpectraUploadController,
    controllerAs: '$ctrl'
}

angular.module('moaClientApp')
    .component(SpectraUploadComponent.selector, SpectraUploadComponent);
