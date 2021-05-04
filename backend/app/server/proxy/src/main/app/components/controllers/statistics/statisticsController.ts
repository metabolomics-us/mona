/**
 * Created by Gert on 5/28/2014.
 */

import * as angular from 'angular';

    StatisticsController.$inject = ['$scope', 'statistics'];
    angular.module('moaClientApp')
        .controller('StatisticsController', StatisticsController);

    /* @ngInject */
    function StatisticsController($scope, statistics) {
        $scope.data = statistics;
    }
