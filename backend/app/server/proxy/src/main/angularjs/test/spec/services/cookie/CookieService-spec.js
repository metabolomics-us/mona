'use strict';

describe('service: Cookie Service', function() {
  beforeEach(module('moaClientApp'));

  var cookieService,cookieStore;

  beforeEach(function() {
    angular.mock.inject(function($injector,_CookieService_) {
      cookieService = _CookieService_;
      cookieStore = $injector.get('$cookieStore');
      cookieStore.put('aValidCookieName','testCookie');
    });
  });

  it('returns true for a string of true, yes, or 1', function() {
    expect(cookieService.stringToBoolean('true')).toEqual(true);
    expect(cookieService.stringToBoolean('yes')).toEqual(true);
    expect(cookieService.stringToBoolean('1')).toEqual(true);
  });

  it('returns false for a string of false, no or 1', function() {
    expect(cookieService.stringToBoolean('false')).toEqual(false);
    expect(cookieService.stringToBoolean('no')).toEqual(false);
    expect(cookieService.stringToBoolean('0')).toEqual(false);
  });

  it('cast any non-matching strings to a Boolean', function() {
    expect(cookieService.stringToBoolean('test') == Boolean);
  });

  it('returns false for invalid cookie', function() {
    expect(cookieService.getBooleanValue('myFakeCookie')).toEqual(false);
  });

  it('returns true for a valid cookie', function() {
    expect(cookieService.getBooleanValue('aValidCookieName')).toEqual(true);
  });
});
