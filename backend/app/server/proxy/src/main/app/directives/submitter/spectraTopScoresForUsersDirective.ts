/**
 * provides top users scores
 */

import * as angular from 'angular';

class SpectraTopScoresForUsersDirective {
    constructor() {
        return {
            replace: true,
            templateUrl: '../../views/templates/scores/hallOfFame.html',
            scope: {
                limit: '@'
            },
            controller: SpectraTopScoresForUsersController,
            controllerAs: '$ctrl',
            link: ($scope, element, attrs, $ctrl) => {
                $ctrl.StatisticsService.spectraTopScores().then(
                    (data) => {
                        $ctrl.scores = data;
                        $ctrl.scores.forEach((x) => {
                            x.score -= 0.45;
                        });
                    })
            }
        }
    }
}

class SpectraTopScoresForUsersController {
    private static $inject = ['StatisticsService'];
    private StatisticsService;
    private scores;

    constructor(StatisticsService) {
        this.StatisticsService = StatisticsService;
    }
}

angular.module('moaClientApp')
    .directive('spectraTopScoresForUsers', SpectraTopScoresForUsersDirective);
