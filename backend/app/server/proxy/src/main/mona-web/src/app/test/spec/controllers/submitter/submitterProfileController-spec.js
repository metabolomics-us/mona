'use strict';

describe('Controllers: Submitter Profile', function() {
    beforeEach(module('moaClientApp'));

    var scope, subCtrl, authService, currentUser, location;

    beforeEach(inject(function($rootScope, $controller, $injector, $q, $location) {

        // mock current user
        currentUser = {
            name: 'test',
            access_token: 1235
        }

        // mock promise
        authService = {
            getCurrentUser: function() {
                var d = $q.defer();
                d.resolve(currentUser);
                return d.promise;
            }
        };
        scope = $rootScope.$new();
        location = $injector.get('$location');
        subCtrl = $controller('SubmitterProfileController', {
            $scope: scope,
            AuthenticationService: authService
        });
    }));

    it('set the current user profile', function() {
        scope.$broadcast('auth:login-success');
        scope.$digest();
        expect(scope.user).toEqual(currentUser);
    });

    it('compiles a query with the submitters email address', function() {
        scope.user = {emailAddress: 'test@test.com'};
        scope.queryUserSpectra();
        expect(location.path()).toBe('/spectra/browse/');
    });
});