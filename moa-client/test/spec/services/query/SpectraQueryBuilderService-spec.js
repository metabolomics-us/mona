'use strict';

describe('service: Spectra Query Builder Service', function() {
  beforeEach(module('moaClientApp'));

  var queryCache,mDataService,specQueryBuilder;

  beforeEach(function() {
    angular.mock.inject(function(_QueryCache_,_MetadataService_,_SpectraQueryBuilderService_) {
      queryCache = _QueryCache_;
      mDataService = _MetadataService_;
      specQueryBuilder = _SpectraQueryBuilderService_;
    });
  });

  //mock queries
  var query = {
    id: [1],
    compound: {test: 'test'},
    metadata: [],
    tags: ['testTag','testTag2']
  };

  it('returns a Query Object', function() {
    var q = specQueryBuilder.getQuery();
    expect(q.compound);
    expect(q.metadata);
    expect(q.tags);
  });

  it('complies a query', function() {
    var qToCompile = specQueryBuilder.compileQuery(query,'sampleTag');
    var compiled = queryCache.getSpectraQuery();
    expect(qToCompile).toEqual(compiled);
  });

  it('removes Tags from Queries', function() {
    queryCache.setSpectraQuery(query);
    specQueryBuilder.removeTagFromQuery('testTag');
    var qWithCleanedTags = queryCache.getSpectraQuery().tags;
    expect(qWithCleanedTags).toEqual(['testTag2']);
  });

  it('removes Tags from compounds', function() {
    var q = {
        compound: {name: 'test',
                   tags: ['CompoundTag','CompoundTag2']
        },
        tags:[]
    };

    queryCache.setSpectraQuery(q);
    specQueryBuilder.removeTagFromQuery('CompoundTag');
    var cWithCleanedTags = queryCache.getSpectraQuery().compound.tags;
    expect(cWithCleanedTags).toEqual(['CompoundTag2']);
  });

  it('adds Spectra Id to Query', function() {
    queryCache.setSpectraQuery(query);
    specQueryBuilder.addSpectraIdToQuery(2);
    expect(query.id[1]).toEqual(2);
  });

  it('creates a Spectra Id array when adding id to Queries with no Spectra Ids', function() {
    var q = {};
    queryCache.setSpectraQuery(q);
    specQueryBuilder.addSpectraIdToQuery(2);
    expect(q.id[0]).toEqual(2);
  });
});
