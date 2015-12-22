'use strict';

describe('Application Module: moaClientApp', function() {
    beforeEach(module('moaClientApp'));

    var app;

    beforeEach(inject(function() {
        app = angular.module('moaClientApp');
    }));
    
    it('can be instantiated', function() {
        expect(app).toBeDefined();
    });
});