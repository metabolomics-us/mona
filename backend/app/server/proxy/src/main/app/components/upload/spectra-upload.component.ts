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
    private errors;
    private spectra;
    private spectraUploaded;

    constructor($scope, $rootScope, AuthenticationService, UploadLibraryService, $log){
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.AuthenticationService = AuthenticationService;
        this.UploadLibraryService = UploadLibraryService;
        this.$log = $log;
    }

    $onInit = () => {
        this.errors = [];
        this.spectra = [];
        this.spectraUploaded = this.UploadLibraryService.uploadedSpectraCount > 0;
        this.$scope.$on('spectra:uploadsuccess', (event, data) => {
            this.$log.info("SUCCESS!");
            this.$log.info(data);

            this.$scope.spectra.push(data.data)
        });

        this.$scope.$on('spectra:uploaderror', (event, error) => {
            this.$log.info("ERROR!");
            this.$log.info(error);
        });
    }
}

let SpectraUploadComponent = {
    selector: "spectraUpload",
    templateUrl: '../../views/spectra/upload/uploadStatus.html',
    bindings: {},
    controller: SpectraUploadController
}

angular.module('moaClientApp')
    .component(SpectraUploadComponent.selector, SpectraUploadComponent);
