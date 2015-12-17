/**
 * provides top users scores
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('spectraTopScoresForUsers', spectraTopScoresForUsers);

    /* @ngInject */
    function spectraTopScoresForUsers($compile, StatisticsService, Submitter) {
        var directive = {
            replace: true,
            templateUrl: '/views/templates/scores/hallOfFame.html',
            scope: {
                limit: '@'
            },
            link: function($scope, element, attrs, ngModel) {
                StatisticsService.spectraTopScores({max: $scope.limit},
                  function(data) {
                      var scores = data;

                      angular.forEach(scores, function(score) {
                          score.submitter = Submitter.get({id: score.submitter});
                      });
                      $scope.scores = scores;

                  }
                );
            },
            controller: spectraTopScoresForUsersController
        };

        return directive;
    }

    /* @ngInject */
    function spectraTopScoresForUsersController($scope) {

    }
})();