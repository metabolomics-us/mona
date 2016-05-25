'use strict';

describe('Metadata Services', function() {
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var _MetadataService_,_MetaData_,service,service2;

  beforeEach(function() {
    angular.mock.inject(function($injector) {
      _MetadataService_ = $injector.get('MetadataService');
      _MetaData_ = $injector.get('MetaData');
      service = new _MetadataService_();
      service2 = new _MetaData_();
    });
  });

  it('MetadataService returns a $resource object', function() {
    expect(service instanceof _MetadataService_).toEqual(true);
  });

  it('MetaData returns a $resource object', function() {
    expect(service2 instanceof _MetaData_);
  });
});


