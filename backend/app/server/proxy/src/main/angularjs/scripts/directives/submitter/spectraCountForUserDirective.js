/**
 * provides us with some feedback how many spectra a certain person uploaded
 */

(function() {
    'use strict';

    spectraCountForUser.$inject = ['$compile', 'StatisticsService'];
    spectraCountForUserController.$inject = ['$scope'];
    angular.module('moaClientApp')
      .directive('spectraCountForUser', spectraCountForUser);

    /* @ngInject */
    function spectraCountForUser($compile, StatisticsService) {
        var directive = {
            replace: true,
            template: '<span>{{spectraCount}}</span>',
            scope: {
                user: '=user'
            },
            link: function($scope, element, attrs, ngModel) {
                StatisticsService.spectraCount({id: $scope.user.id},
                    function(data) {
                        $scope.spectraCount = data.count;
                    }
                );
            },
            controller: spectraCountForUserController
        };

        return directive;
    }

    /* @ngInject */
    function spectraCountForUserController($scope) {
        $scope.spectraCount = "loading...";
    }
})();
