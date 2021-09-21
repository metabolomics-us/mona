'use strict';

describe('Controller: Spectra Upload -', function() {
    beforeEach(module('moaClientApp'));

    var scope,specCtrl;

    beforeEach(inject(function($controller,$rootScope) {
        scope = $rootScope.$new();
        specCtrl = $controller('SpectraUploadController', {
            $scope: scope
        });
    }));

    it('checks if the user is uploading', function() {
       expect(scope).toBeDefined();
    });

});