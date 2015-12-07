/**
 * Created by Gert on 5/28/2014.
 */
(function() {
    'use strict';

    moaControllers.StatisticsController = ['$scope', 'StatisticsService', '$uibModal', 'statistics',
        function($scope, StatisticsService, $uibModal, statistics) {
            $scope.data = statistics;
        }];
})();
