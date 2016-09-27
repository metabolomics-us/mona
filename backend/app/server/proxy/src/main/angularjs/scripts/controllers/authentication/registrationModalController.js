(function() {
    'use strict';
    RegistrationModalController.$inject = ['$scope', '$rootScope', '$uibModalInstance', '$http', 'AuthenticationService', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
      .controller('RegistrationModalController', RegistrationModalController);

    /* @ngInject */
    function RegistrationModalController($scope, $rootScope, $uibModalInstance, $http, AuthenticationService, REST_BACKEND_SERVER) {
        $scope.errors = [];
        $scope.state = 'register';

        $scope.newSubmitter = {'emailAddress': 'a@a', firstName: 'a', lastName: 'a', institution: 'a', password: 'a'};

        $scope.cancelDialog = function() {
            $uibModalInstance.dismiss('cancel');
        };

        /**
         * closes the dialog and finishes and builds the query
         */
        $scope.submitRegistration = function() {
            $scope.errors = [];

            // var submitter = new Submitter();
            // submitter.firstName = $scope.newSubmitter.firstName;
            // submitter.lastName = $scope.newSubmitter.lastName;
            // submitter.institution = $scope.newSubmitter.institution;
            // submitter.emailAddress = $scope.newSubmitter.emailAddress;
            // submitter.password = $scope.newSubmitter.password;

            $scope.state = 'registering';


            $http({
                method: 'POST',
                url: REST_BACKEND_SERVER + '/rest/users',
                data: {
                    username: $scope.newSubmitter.emailAddress,
                    password: $scope.newSubmitter.password
                },
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(
                function(response) {
                    $http({
                        method: 'POST',
                        url: REST_BACKEND_SERVER + '/rest/auth/login',
                        data: {
                            username: response.data.username,
                            password: response.data.password
                        },
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).then(
                        function(response) {
                            $http({
                                method: 'POST',
                                url: REST_BACKEND_SERVER + '/rest/submitters',
                                data: {
                                    emailAddress: $scope.newSubmitter.emailAddress,
                                    firstName: $scope.newSubmitter.firstName,
                                    lastName: $scope.newSubmitter.lastName,
                                    institution: $scope.newSubmitter.institution
                                },
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Bearer ' + response.data.token
                                }
                            }).then(
                                function(response) {
                                    $scope.state = 'success';
                                },
                                function(response) {
                                    $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
                                }
                            );
                        },
                        function(response) {
                            $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
                        }
                    );
                },
                function(response) {
                    $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(response));
                }
            );


            /*
            Submitter.save(submitter,
                function() {
                    $scope.state = 'success';
                },
                function(data) {
                    $scope.state = 'register';

                    if (data.status === 422) {
                        for (var i = 0; i < data.data.errors.length; i++) {
                            var message = 'Error in ' + data.data.errors[i].field + ': ';

                            if (data.data.errors[i].message.indexOf('must be unique') > -1) {
                                $scope.errors.push(message + 'already exists!');
                            } else {
                                $scope.errors.push(message + data.data.errors[i].message);
                            }
                        }
                    } else {
                        $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(data));
                    }
                }
            );
            */
        };

        /**
         * Close dialog and open login modal
         */
        $scope.logIn = function() {
            $uibModalInstance.dismiss('cancel');
            $rootScope.$broadcast('auth:login');
        };
    }
})();
