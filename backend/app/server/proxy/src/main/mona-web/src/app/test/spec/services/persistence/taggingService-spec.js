'use strict';

 describe('Tagging Service', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
 });

    var _Tagging_,service;

   beforeEach(function() {
   angular.mock.inject(function($injector) {
     _Tagging_ = $injector.get('TaggingService');
   service = new _Tagging_();
   });
   });

   it('returns a $resource object of type Submitter', function() {
   expect(service instanceof _Tagging_);
   });

 });

