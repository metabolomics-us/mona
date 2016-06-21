/**
 * provides user score
 */

(function() {
    'use strict';

    spectraScoreForUser.$inject = ['$compile', 'StatisticsService'];
    spectraScoreForUserController.$inject = ['$scope'];
    angular.module('moaClientApp')
        .directive('spectraScoreForUser', spectraScoreForUser);

    /* @ngInject */
    function spectraScoreForUser($compile, StatisticsService) {
        var directive = {
            replace: true,
            templateUrl: '/views/templates/scoreSpectra.html',
            scope: {
                user: '=user'
            },
            link: function($scope, element, attrs, ngModel) {
                StatisticsService.spectraScore({id: $scope.user.id},
                  function(data) {
                      $scope.score = data.score;
                  }
                );
            },
            controller: spectraScoreForUserController
        };

        return directive;
    }

    /* @ngInject */
    function spectraScoreForUserController($scope) {}
})();
