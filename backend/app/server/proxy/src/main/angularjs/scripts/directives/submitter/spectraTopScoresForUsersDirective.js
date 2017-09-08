/**
 * provides top users scores
 */

(function() {
    'use strict';

    spectraTopScoresForUsers.$inject = ['StatisticsService'];
    angular.module('moaClientApp')
        .directive('spectraTopScoresForUsers', spectraTopScoresForUsers);

    /* @ngInject */
    function spectraTopScoresForUsers(StatisticsService) {
        return {
            replace: true,
            templateUrl: '/views/templates/scores/hallOfFame.html',
            scope: {
                limit: '@'
            },
            link: function($scope, element, attrs, ngModel) {
                $scope.scores = [];
                
                StatisticsService.spectraTopScores({},
                    function(data) {
                        $scope.scores = data;

                        $scope.scores.forEach(function(x) {
                            x.score -= 0.45;
                        });
                    }
                );
            }
        };
    }
})();
