/**
 * provides us with some feedback how many spectra a certain person uploaded
 */

import * as angular from 'angular';

class SpectraCountForUserDirective {


    constructor() {
        return {
            replace: true,
            template: '<span>{{$ctrl.$scope.spectraCount}}</span>',
            scope: {
                user: '=user'
            },
            controller: SpectraCountForUserController,
            controllerAs: '$ctrl',
            link: ($scope, element, attrs, $ctrl) => {
                $ctrl.StatisticsService.spectraCount({id: $scope.user.id},
                    (data) => {
                        $scope.spectraCount = data.count;
                    })
            }
        }
    }
}

class SpectraCountForUserController {
    private static $inject = ['$scope', '$compile', 'StatisticsService'];
    private $scope;
    private $compile;
    private StatisticsService;
    constructor($scope, $compile, StatisticsService) {
        this.$scope = $scope;
        this.$compile = $compile;
        this.StatisticsService = StatisticsService;
    }

    $onInit = () => {
        this.$scope.spectraCount = "loading...";
    }

}

angular.module('moaClientApp')
    .directive('spectraCountForUser', SpectraCountForUserDirective);
