/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('StatisticsController', StatisticsController);

    function StatisticsController($scope, StatisticsService, $uibModal, statistics) {
        $scope.data = statistics;
    }
})();
