/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    SpectraUploadController.$inject = ['$scope', '$rootScope', 'AuthenticationService', 'UploadLibraryService', '$log'];
    angular.module('moaClientApp')
        .controller('SpectraUploadController', SpectraUploadController);

    /* @ngInject */
    function SpectraUploadController($scope, $rootScope, AuthenticationService, UploadLibraryService, $log) {

        $scope.errors = [];
        $scope.spectra = [];

        /**
         * Check whether the user is uploading, and open the upload dialog accordingly
         */
        (function() {
            $scope.spectraUploaded = UploadLibraryService.uploadedSpectraCount > 0;


            // if (AuthenticationService.isLoggedIn() && !$scope.spectraUploaded) {
            //     // Not using the old upload wizard anymore
            //     //$scope.uploadSpectraDialog()
            // } else if (!AuthenticationService.isLoggedIn() && !AuthenticationService.isLoggingIn()) {
            //     $rootScope.$broadcast('auth:login');
            // }
        })();


        $scope.$on('spectra:uploadsuccess', function(event, data) {
            $log.info("SUCCESS!");
            $log.info(data);

            $scope.spectra.push(data.data)
        });

        $scope.$on('spectra:uploaderror', function(event, error) {
            $log.info("ERROR!");
            $log.info(error);
        });
    }
})();
