/**
 * Creates or updates a query with the given submitter information
 */

import * as angular from 'angular';

class SpectrumReviewDirective{
    constructor() {
        return {
            replace: true,
            templateUrl: '../../views/templates/feedback/spectrumReview.html',
            restrict: 'A',
            scope: {
                spectrum: '=spectrum'
            },
            controller: SpectrumReviewController,
            controllerAs: '$ctrl'
        }
    }
}

class SpectrumReviewController{
    private static $inject = ['$scope', '$http', 'AuthenticationService', 'REST_BACKEND_SERVER'];
    private $scope;
    private $http;
    private AuthenticationService;
    private REST_BACKEND_SERVER;
    private submitting;
    private submitted;
    private spectrum;

    constructor($scope, $http, AuthenticationService, REST_BACKEND_SERVER) {
        this.$scope = $scope;
        this.$http = $http;
        this.AuthenticationService = AuthenticationService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
    }

    $onInit = () => {
        this.submitting = false;
        this.submitted = false;
        console.log('Starting Up Boys');
        console.log(this.$scope.spectrum.id);
    }

    rate = (value) => {
        console.log('We in Here');
        console.log(this.$scope.spectrum.id);
        this.AuthenticationService.getCurrentUser().then((data) => {
            let payload = {
                monaID: this.$scope.spectrum.id,
                userID: data.username,
                name: 'spectrum_quality',
                value: value
            };

            this.$http.post(this.REST_BACKEND_SERVER + '/rest/feedback', payload).then((data) => {
                this.submitting = false;
                this.submitted = true;
            });
        });
    };
}

angular.module('moaClientApp')
    .directive('spectrumReview', SpectrumReviewDirective);
