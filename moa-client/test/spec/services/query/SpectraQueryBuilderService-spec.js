'use strict';

describe('service: Spectra Query Builder Service', function() {
  beforeEach(module('moaClientApp'));

  var queryCache,mDataService,specQueryBuilder,rootScope;

  beforeEach(function() {
    angular.mock.inject(function($injector,_QueryCache_,_MetadataService_,_SpectraQueryBuilderService_) {
      queryCache = _QueryCache_;
      mDataService = _MetadataService_;
      specQueryBuilder = _SpectraQueryBuilderService_;
      rootScope = $injector.get('$rootScope');

      var query = {
        id: [1],
        compound: {test: 'test'},
        metadata: [],
        tags: ['testTag','testTag2']
      };
      queryCache.setSpectraQuery(query);

    });
  });

  var hash = 'splash10-dz40000000-9ff3eaf3411278ba18fd';
  var spectra = 'test Spectra';

  //mock queries

  it('returns a Query Object', function() {
    var q = specQueryBuilder.getQuery();
    expect(q.compound);
    expect(q.metadata);
    expect(q.tags);
  });

  it('complies a query', function() {
    var query = {
      id: [1],
      compound: {test: 'test'},
      metadata: [],
      tags: ['testTag','testTag2']
    };

    var qToCompile = specQueryBuilder.compileQuery(query,'sampleTag');
    var compiled = queryCache.getSpectraQuery();
    expect(qToCompile).toEqual(compiled);
  });

  it('removes Tags from Queries', function() {
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
    //queryCache.setSpectraQuery(query);
    specQueryBuilder.addSpectraIdToQuery(2);
    expect(queryCache.getSpectraQuery().id[1]).toEqual(2);
  });

  it('creates a Spectra Id array when adding id to Queries with no Spectra Ids', function() {
    var q = {};
    queryCache.setSpectraQuery(q);
    specQueryBuilder.addSpectraIdToQuery(2);
    expect(q.id[0]).toEqual(2);
  });

  it('validates a spectra and add similar spectra to a query', function () {
    var query = {};
    query.match = {};
    query.match.spectra = '';
    queryCache.setSpectraQuery(query);
    rootScope.spectra = spectra;
    specQueryBuilder.addSimilarSpectraToQuery('1234','test Spectra');
    var qWithMatchingSpectra = queryCache.getSpectraQuery().match.spectra;
    expect(qWithMatchingSpectra).toEqual('test Spectra');
  });

  it('adds a matching histogram to query', function() {
    specQueryBuilder.addMatchingHistogramToQuery(hash);
    var res = queryCache.getSpectraQuery();
    expect(res.match.histogram).toEqual('dz40000000');
  });

  it('adds exact spectra search to query', function() {
    specQueryBuilder.addExactSpectraSearchToQuery(hash);
    var res = queryCache.getSpectraQuery();
    expect(res.match.exact).toEqual(hash);
  });

  it('adds top 10 ions search to query', function () {
    specQueryBuilder.addTop10IonsSearchToQuery(hash);
    var res = queryCache.getSpectraQuery();
    expect(res.match.top10).toEqual(hash);
  });

  it('removes a spectra id from query', function() {
    specQueryBuilder.removeSpectraIdFromQuery(1);
    var res = queryCache.getSpectraQuery();
    expect(res.id.length).toEqual(0);
  });

  it('adds a tag to query', function() {
    var tag = 'massbank';
    specQueryBuilder.addTagToQuery(tag);
    var res = queryCache.getSpectraQuery();
    expect(res.tags[2].name.eq).toEqual(tag);
  });

  it('adds a tag that is of a compound type', function() {
    var compoundTag = 'water';
    specQueryBuilder.addTagToQuery(compoundTag,true);
    var res = queryCache.getSpectraQuery();
    //console.log(res);
    //expect(res.compounds.tags[0]).toEqual(compoundTag);


  });





});
