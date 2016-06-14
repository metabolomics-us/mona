'use strict';

describe('Controller: Keyword Search Controller', function() {
   beforeEach(module('moaClientApp'));

    var scope, ctrl;

    beforeEach(function() {
        angular.mock.inject(function($injector,$controller,$rootScope) {
            scope = $rootScope.$new();
            ctrl = $controller('KeywordSearchController', {
                $scope: scope,
                $log: log
            });
        });
    });

    it ('can be initialized', function() {
        console.log(ctrl);

        console.log(ctrl);
       expect(ctrl).toBeDefined();
    });

    it ('can be initialized', function() {
        console.log(ctrl);

        console.log(ctrl);
        expect(ctrl).toBeDefined();
    });
});
