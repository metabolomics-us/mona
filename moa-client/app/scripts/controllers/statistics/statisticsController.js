/**
 * Created by Gert on 5/28/2014.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('StatisticsController', statisticsController);

    /* @ngInject */
    function statisticsController($scope, StatisticsService, $uibModal, statistics) {
        $scope.data = statistics;
    }
})();
