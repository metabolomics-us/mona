'use strict';

describe('Splash Service', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var splash, service, http, httpBackend;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      http = $injector.get('$http');
      httpBackend = $injector.get('$httpBackend');
      splash = $injector.get('SplashService');
      service = new splash();
    });
  });

  var req = {
    method: 'POST',
    url: 'http://cream.fiehnlab.ucdavis.edu:9292/splash.fiehnlab.ucdavis.edu/splash/it',
    headers: {
      'Content-Type': undefined
    },
    data: { test: 'test' }
  }


  it ('returns a $resource object of type Splash', function() {
    expect(service instanceof splash);
  });

  // TODO: complete test for transformResponse
  it('returns splash data', function() {
      httpBackend.expectPOST('http://cream.fiehnlab.ucdavis.edu:9292/splash.fiehnlab.ucdavis.edu/splash/it',{})
        .respond(200);

      //httpBackend.flush();
  });

});
