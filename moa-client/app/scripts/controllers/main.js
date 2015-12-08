(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('MainCtrl', MainCtrl);

    MainCtrl.$inject = ['$scope'];

    function MainCtrl($scope) {
        $scope.slides = [
            {image: 'images/spectrum-1.png', id: '252', name: 'Cyclopamine'},
            {image: 'images/spectrum-2.png', id: '931', name: 'Ro-42130'}
        ];
    }
})();

