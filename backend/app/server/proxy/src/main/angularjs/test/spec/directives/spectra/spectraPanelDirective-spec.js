'use strict';

describe('Directive: Spectra Panel Directive', function() {
    beforeEach(angular.mock.module('moaClientApp'));
    beforeEach(module('templates'));

    var scope, element, controller;
    var $compile, $scope;

    beforeEach(inject(function(_$compile_, _$rootScope_) {
        $compile = _$compile_;
        $scope = _$rootScope_.$new();
    }));

    it('should load the template', inject(function() {
        $scope.spectrum = {id: 'test'};
        var template = $compile(angular.element('<div display-spectra-panel spectrum="spectrum"></div>'))($scope);

        $scope.$digest();

        var templateAsHtml = template.html();
        console.log(templateAsHtml);
        console.log($scope);
    }));

    // beforeEach(inject(function($rootScope, $compile) {
    //     // create our html element
    //     element = angular.element('<display-spectra-panel></display-spectra-panel>');
    //     scope = $rootScope.$new();
    //     scope.spectrum = {id: 'test'};
    //     controller = element.controller('displaySpectraPanel');
    //
    //     //compile element and scope
    //     $compile(element)(scope);
    //     scope.$digest();
    //
    // }));
    //
    // it('should load the template', function() {
    //     var content = element.text();
    //     console.log(content);
    //     console.log(controller);
    // });
});
