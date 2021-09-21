'use strict';

describe('Controllers: Submitter Modal', function() {
    beforeEach(module('moaClientApp'));

    var scope, subCtrl, uibModalInstance, newSubmitter, submitter, httpBackend;

    beforeEach(inject(function($controller, $injector, $rootScope) {
        scope = $rootScope.$new();
        httpBackend = $injector.get('$httpBackend');
        newSubmitter = function() {
            return {}
        };
        submitter = $injector.get('Submitter');
        uibModalInstance = {
            dismiss: jasmine.createSpy('uibModalInstance.dismiss'),
            close: jasmine.createSpy('uibModalInstance.close')
        };
        subCtrl = $controller('SubmitterModalController', {
            $scope: scope,
            $uibModalInstance: uibModalInstance,
            newSubmitter: newSubmitter
        });
    }));

    afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it('cancel any dialog for this controller', function() {
        httpBackend.expect('GET', 'views/main.html').respond(200);
        scope.cancelDialog();
        httpBackend.flush();
        expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
    });

    it('creates a submitter and closes the modal', function() {
        httpBackend.expect('POST', 'http://cream.fiehnlab.ucdavis.edu:8080/rest/submitters').respond(200);
        httpBackend.expect('GET', 'views/main.html').respond(200);
        scope.createNewSubmitter();
        httpBackend.flush();
        expect(uibModalInstance.close).toHaveBeenCalled();
    });

    it('handles errors when creating submitter', function() {
        var data = {
            errors: ['not enough information']
        }

        httpBackend.expect('POST', 'http://cream.fiehnlab.ucdavis.edu:8080/rest/submitters').respond(500, data);
        httpBackend.expect('GET', 'views/main.html').respond(200);
        scope.createNewSubmitter();
        httpBackend.flush();
        expect(scope.formErrors).toEqual(['not enough information']);
    });

    it('updates a submitter and closes the modal', function() {
        httpBackend.expect('PUT', 'http://cream.fiehnlab.ucdavis.edu:8080/rest/submitters').respond(200);
        httpBackend.expect('GET', 'views/main.html').respond(200);
        scope.updateSubmitter();
        httpBackend.flush();
        expect(uibModalInstance.close).toHaveBeenCalled();
    });
});