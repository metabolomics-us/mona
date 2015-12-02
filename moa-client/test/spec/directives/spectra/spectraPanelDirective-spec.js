'use strict'

describe('Directive: Spectra Panel Directive', function() {
    beforeEach(module('moaClientApp'), function($httpProvider) {
        $httpProvider.interceptors.push('moaClientApp');
    });

    beforeEach(module('views/spectra/display/template/panel.html'));

    var scope,element,httpBackend;

    beforeEach(inject(function($rootScope,$compile,$injector) {
        httpBackend = $injector.get('$httpBackend');
        httpBackend.expectGET('views/main.html').respond(200);
        scope = $rootScope.$new();
        element = angular.element("<display-spectra-panel></display-spectra-panel>");
        $compile(element)(scope);
        httpBackend.flush();
        scope.$digest();

    }));

    it('should load the template', function() {
        expect(element.length).toBe(1);
    });
});
