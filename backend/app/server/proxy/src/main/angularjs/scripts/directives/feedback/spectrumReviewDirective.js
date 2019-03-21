/**
 * Creates or updates a query with the given submitter information
 */

(function() {
    'use strict';

    spectrumReviewController.$inject = ['$scope', '$http', 'AuthenticationService', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .directive('spectrumReview', spectrumReview);

    function spectrumReview() {
        return {
            replace: true,
            templateUrl: '/views/templates/feedback/spectrumReview.html',
            restrict: 'A',
            scope: {
                spectrum: '=spectrum'
            },
            controller: spectrumReviewController
        };
    }

    /* @ngInject */
    function spectrumReviewController($scope, $http, AuthenticationService, REST_BACKEND_SERVER) {
        $scope.submitting = false;
        $scope.submitted = false;

        $scope.rate = function(value) {
            AuthenticationService.getCurrentUser().then(function(data) {
                var payload = {
                    monaID: $scope.spectrum.id,
                    userID: data.username,
                    name: 'spectrum_quality',
                    value: value
                };

                $http.post(REST_BACKEND_SERVER + '/rest/feedback', payload).then(function(data) {
                    $scope.submitting = false;
                    $scope.submitted = true;
                });
            });
        };
    }
})();