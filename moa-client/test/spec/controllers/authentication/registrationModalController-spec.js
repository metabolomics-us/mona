'use strict';

describe('Controller: Registration Modal Controller', function() {
    beforeEach(module('moaClientApp'), function($httpProvider) {
        $httpProvider.interceptors.push('moaClientApp');
    });
    var scope, rootScope, modalController, uibModalInstance, submitter, httpBackend, REST_SERVER;

    beforeEach(function() {
        angular.mock.inject(function($injector, $controller, $rootScope, _Submitter_, _REST_BACKEND_SERVER_) {
            scope = $rootScope.$new();
            submitter = _Submitter_;
            rootScope = $injector.get('$rootScope');
            httpBackend = $injector.get('$httpBackend');
            REST_SERVER = _REST_BACKEND_SERVER_;
            uibModalInstance = {
                dismiss: jasmine.createSpy('uibModalInstance.dismiss')
            };
            modalController = $controller('RegistrationModalController', {
                $scope: scope,
                $uibModalInstance: uibModalInstance,
                Submitter: submitter
            });
        });
    });


    it('can cancel a registration dialog', function() {
        scope.cancelDialog();
        expect(uibModalInstance.dismiss).toHaveBeenCalledWith('cancel');
    });

    it('register a user that submits all the correct information and is in a registering state', function() {
        scope.newSubmitter.firstName = 'test';
        scope.newSubmitter.lastName = 'user';
        scope.newSubmitter.institution = 'UC Davis';
        scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
        scope.newSubmitter.password = 'super';
        scope.submitRegistration();
        expect(scope.state).toBe('registering');
    });

    it('returns an error message when registration data is not submitted correctly', function() {
        var errorData = {
            status: 422,
            errors: [{message: 'no first name', field: 'First Name'}, {message: 'no last name', field: 'Last Name'}]
        };

        httpBackend.expectPOST(REST_SERVER + '/rest/submitters', {
            "institution": "UC Davis",
            "emailAddress": "testuser@fiehnlab.com",
            "password": "super"
        }).respond(422, errorData);
        httpBackend.expectGET('views/main.html').respond(200);
        scope.newSubmitter.institution = 'UC Davis';
        scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
        scope.newSubmitter.password = 'super';
        scope.submitRegistration();
        httpBackend.flush();
        expect(scope.errors).toEqual(['Error in First Name: no first name', 'Error in Last Name: no last name']);
    });

    it('returns an error if a user registers with an existing email address', function() {
        var duplicateData = {
            status: 422,
            errors: [{message: 'must be unique', field: 'Email'}]
        };

        httpBackend.expectPOST(REST_SERVER + '/rest/submitters', {emailAddress: 'testuser@fiehnlab.com'})
          .respond(422, duplicateData);
        httpBackend.expectGET('views/main.html').respond(200);
        scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
        scope.submitRegistration();
        httpBackend.flush();
        expect(scope.errors).toEqual(['Error in Email: already exists!']);
    });

    it('returns all other error with log of the data submitted', function() {
        var unknownError = {
            status: 400,
            errors: [{message: '', field: ''}]
        };

        httpBackend.expectPOST(REST_SERVER + '/rest/submitters',
          {
              "firstName": "test", "lastName": "user",
              "institution": "UC Davis", "emailAddress": "testuser@fiehnlab.com", "password": "super"
          })
          .respond(400, unknownError);
        httpBackend.expectGET('views/main.html').respond(200);
        scope.newSubmitter.firstName = 'test';
        scope.newSubmitter.lastName = 'user';
        scope.newSubmitter.institution = 'UC Davis';
        scope.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
        scope.newSubmitter.password = 'super';
        scope.submitRegistration();
        httpBackend.flush();
        expect(scope.errors).toEqual(['An unknown error has occurred: ' +
        '{"data":{"status":400,"errors":[{"message":"","field":""}]},' +
        '"status":400,"config":{"method":"POST","transformRequest":[null],' +
        '"transformResponse":[null],"data":{"firstName":"test","lastName":"user",' +
        '"institution":"UC Davis","emailAddress":"testuser@fiehnlab.com","password":"super"},' +
        '"url":"http://cream.fiehnlab.ucdavis.edu:9292/mona.fiehnlab.ucdavis.edu/rest/submitters",' +
        '"headers":{"Accept":"application/json, text/plain, */*","Content-Type":"application/json;charset=utf-8"}},' +
        '"statusText":""}']);
    });
});