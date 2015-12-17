/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('SpectraUploadController', SpectraUploadController);

    /* @ngInject */
    function SpectraUploadController($scope, $rootScope, $uibModal, AuthenticationService, UploadLibraryService) {
        /**
         * Check whether the user is uploading, and open the upload dialog accordingly
         */
        (function() {
            $scope.spectraUploaded = UploadLibraryService.uploadedSpectraCount > 0;

            if (AuthenticationService.isLoggedIn() && !$scope.spectraUploaded) {
                // Not using the old upload wizard anymore
                //$scope.uploadSpectraDialog()
            } else if (!AuthenticationService.isLoggedIn() && !AuthenticationService.isLoggingIn()) {
                $rootScope.$broadcast('auth:login');
            }
        })();
    }
})();