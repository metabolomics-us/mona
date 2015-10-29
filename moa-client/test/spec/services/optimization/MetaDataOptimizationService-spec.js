'use strict';

describe('service: MetaData Optimization Service', function(){
  beforeEach(module('moaClientApp'));

  var log, q,timeout,filter,metaService;

  beforeEach(function() {
    angular.mock.inject(function($injector,_MetaDataOptimizationService_) {
      log = $injector.get('$log');
      q = $injector.get('$q');
      filter = $injector.get('$filter');
      metaService =  _MetaDataOptimizationService_;
    });
  });

  // mock MetaData
  var mockData = [{name: 'Chem Report', category: 'Report'},
                  {name: 'Compound Report', category: 'Report'}];

  it('returns a promise on a metadata array', function() {
    expect(metaService.optimizeMetaData(mockData) instanceof q);
  })
});
