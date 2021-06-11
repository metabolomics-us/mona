/**
 * provides user score
 */

import * as angular from 'angular';

class SpectraScoreForUserDirective {
    constructor() {
        return {
            replace: true,
            template: '<span uib-rating ng-model="$ctrl.$scope.score" max="5" data-readonly="true"></span>',
            scope: {
                user: '=user'
            },
            controller: SpectraScoreForUserController,
            controllerAs: '$ctrl',
            link: ($scope, element, attrs, $ctrl) => {
                $ctrl.StatisticsService.spectraScore(
                    {id: $scope.user.id},
                    (data) => {
                        $scope.score = data.score;
                    }
                );
            }
        }

    }
}

class SpectraScoreForUserController {
    private static $inject = ['$scope', '$compile', 'StatisticsService'];
    private $scope;
    private $compile;
    private StatisticsService;

    constructor($scope, $compile, StatisticsService) {
        this.$scope = $scope;
        this.$compile = $compile;
        this.StatisticsService = StatisticsService;
    }
}

angular.module('moaClientApp')
    .directive('spectraScoreForUser', SpectraScoreForUserDirective);
