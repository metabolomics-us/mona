/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    StatisticsController.$inject = ['$scope', 'statistics'];
    angular.module('moaClientApp')
        .controller('StatisticsController', StatisticsController);

    /* @ngInject */
    function StatisticsController($scope, statistics) {
        $scope.data = statistics;
    }
})();
