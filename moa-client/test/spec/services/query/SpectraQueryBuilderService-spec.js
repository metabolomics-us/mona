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
        metadata: [{name: 'author'}],
        tags: ['testTag','testTag2']

      };
      queryCache.setSpectraQuery(query);

    });
  });

  var hash = 'splash10-dz40000000-9ff3eaf3411278ba18fd';
  var spectra = 'test Spectra';
  var metadata = {name: 'molecule'};

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


  it('updates a pre-compiled query', function(){
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: []};
    specQueryBuilder.updateQuery(qToCompile, ['tag1','tag2'], currentQuery);
    var res = queryCache.getSpectraQuery();
    expect(res.tags.length).toBe(4);
  });

  it('handles a query with submitter component', function() {
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: []};
    qToCompile.submitter = 'testuser@fiehnlab.com';
    specQueryBuilder.updateQuery(qToCompile, 'test', currentQuery);
    var res = queryCache.getSpectraQuery();
    expect(res.submitter).toBe('testuser@fiehnlab.com');
  });

  it('handles a query with nameFilter component', function() {
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: [],compound: {}};
    qToCompile.nameFilter = 'test filter';
    specQueryBuilder.updateQuery(qToCompile, 'test', currentQuery);
    var res = queryCache.getSpectraQuery();
    expect(res.compound.name.like).toBe('%test filter%');
  });

  it('handles a query with inchiFilter component', function() {
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: [],compound: {}};
    qToCompile.inchiFilter ='JLKIGFTWXXRPMT-UHFFFAOYSA-N';
    specQueryBuilder.updateQuery(qToCompile, 'test', currentQuery);
    var res = queryCache.getSpectraQuery();
    expect(res.compound.inchiKey.eq).toBe('JLKIGFTWXXRPMT-UHFFFAOYSA-N');
  });

  it('handles a inchiFilter that does not match standard inChiKey format', function() {
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: [],compound: {}};
    qToCompile.inchiFilter ='Not a Standard Formatted Key';
    specQueryBuilder.updateQuery(qToCompile, 'test', currentQuery);
    var res = queryCache.getSpectraQuery();
    expect(res.compound.inchiKey.like).toBe('Not a Standard Formatted Key');
  });

  it('handles all other metadata query components', function() {
    var currentQuery = queryCache.getSpectraQuery();
    var qToCompile = {metadata: [{accession: 'AU101801'}],compound: {}};
    specQueryBuilder.updateQuery(qToCompile, [], currentQuery);
    var res = queryCache.getSpectraQuery();

    mDataService.metadata({name: 'author'});
    expect(res.metadata[1].value.eq[0].accession).toBe('AU101801');
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
    expect(res.tags[res.tags.length-1].name.eq).toEqual(tag);
  });

  it('adds a tag that is of a compound type', function() {
    var compoundTag = 'water';
    specQueryBuilder.addTagToQuery(compoundTag,true);
    var res = queryCache.getSpectraQuery();
    //console.log(res);
    expect(res.compound.tags[0]).toEqual(compoundTag);
  });

    it('includes + when adding tag to query', function() {
      specQueryBuilder.addTagToQuery('water', false, '+');
      var res = queryCache.getSpectraQuery();
      expect(res.tags[res.tags.length-1].name.eq).toEqual('water');
  });

  it('excludes - when adding tag to query', function() {
    specQueryBuilder.addTagToQuery('water', false, '-');
    var res = queryCache.getSpectraQuery();
    expect(res.tags[res.tags.length-1].name.ne).toEqual('water');
  });

  it('clear all tags from query', function() {
    specQueryBuilder.clearTagsFromQuery();
    var res = queryCache.getSpectraQuery();
    expect(res.tags).toEqual([]);
  });

  it('removes a metadata from query', function() {
    var metadata = {name: 'author'};
    specQueryBuilder.removeMetaDataFromQuery(metadata);
    var res = queryCache.getSpectraQuery();
    expect(res.metadata.length).toEqual(0);
  });

  it('removes metadata of compounds from query', function() {
    var query = {compound: {name: 'oxygen', metadata: [{name: 'molecule'}]}};
    queryCache.setSpectraQuery(query);
    specQueryBuilder.removeMetaDataFromQuery(metadata);
    var res = queryCache.getSpectraQuery();
    expect(res.compound.metadata).toEqual([]);
  });

  it('adds metadata to a query', function() {
    specQueryBuilder.addMetaDataToQuery(metadata);
    var res = queryCache.getSpectraQuery();
    expect(res.metadata[1].name).toBe('molecule');
  });

  it('does not add a metadata with an empty name', function() {
    var m = {name: ''};
    specQueryBuilder.addMetaDataToQuery(m);
    var res = queryCache.getSpectraQuery();
    expect(res.metadata.length).toBe(1);
  });

  it('adds a metadata that is of compound', function() {
    var compound = {name: 'oxygen'};
    specQueryBuilder.addMetaDataToQuery(metadata,compound);
    var res = queryCache.getSpectraQuery();
    expect(res.compound.metadata[0].name).toEqual('molecule');
  });

  it('adds metadata unit if given', function() {
    var m = {name: 'molecule', unit: 10};
    specQueryBuilder.addMetaDataToQuery(m);
    var res = queryCache.getSpectraQuery();
    expect(res.metadata[1].unit.eq).toEqual(10);
  });

  it('adds a user to query', function() {
    var user = 'test@fiehnlab.com';
    specQueryBuilder.addUserToQuery(user);
    var res = queryCache.getSpectraQuery();
    expect(res.submitter).toEqual(user);
  });

  it('removes a user from the query', function() {
    specQueryBuilder.removeUserFromQuery();
    var res = queryCache.getSpectraQuery();
    expect(res.submitter).toEqual(null);
  });
});
