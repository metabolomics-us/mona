(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('RegistrationModalController', RegistrationModalController)

    /* @ngInject */
    function RegistrationModalController($scope, $rootScope, $uibModalInstance, Submitter) {
        $scope.errors = [];
        $scope.state = 'register';

        $scope.newSubmitter = {};

        $scope.cancelDialog = function() {
            $uibModalInstance.dismiss('cancel');
        };

        /**
         * closes the dialog and finishes and builds the query
         */
        $scope.submitRegistration = function() {
            $scope.errors = [];

            var submitter = new Submitter();
            submitter.firstName = $scope.newSubmitter.firstName;
            submitter.lastName = $scope.newSubmitter.lastName;
            submitter.institution = $scope.newSubmitter.institution;
            submitter.emailAddress = $scope.newSubmitter.emailAddress;
            submitter.password = $scope.newSubmitter.password;

            $scope.state = 'registering';

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