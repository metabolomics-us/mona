'use strict'

describe('Directive: Spectra Panel Directive', function() {
    beforeEach(module('moaClientApp'));
    beforeEach(module('templates'));

    var scope, element, controller;

    beforeEach(inject(function($rootScope, $compile) {
        // create our html element
        element = angular.element('<display-spectra-panel></display-spectra-panel>');
        scope = $rootScope.$new();
        scope.spectrum = {id: 'test'};
        controller = element.controller('displaySpectraPanel');

        //compile element and scope
        $compile(element)(scope);
        scope.$digest();

    }));

    it('should load the template', function() {
        var content = element.text();
        console.log(controller);
    });
});
