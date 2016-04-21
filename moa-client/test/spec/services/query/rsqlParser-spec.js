'use strict';

describe('factory: RSQL Parser Factory', function() {
    beforeEach(module('moaClientApp'));

    var rootScope, rsqlParser, query;

    beforeEach(inject(function($injector) {
        rsqlParser = $injector.get('rsqlParser');
        rootScope = $injector.get('$rootScope');
    }));

    query = {
        compound: {name: 'testCompoundName'},
        metadata: [
            {name: 'meta-1', value: {eq: 'meta-1-value'}},
            {name: 'meta-2', value: {ne: 'meta-2-value'}},
            {name: 'meta-3', value: {eq: 'meta-3-value'}}
        ],
        tags: [
            {name: {eq: 'massbank'}},
            {name: {eq: 'GNPS'}},
            {name: {eq: 'LCMS'}}
        ]
    };

    it('returns a query string for compound object', function() {
        var result = rsqlParser.parseRSQL({compound: query.compound});
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound.names=q=\'name=="testCompoundName"\'');
    });

    it('returns a query string for metadata object', function() {
        var result =  rsqlParser.parseRSQL({metadata: query.metadata});
        expect(result).toBe('metaData=q=\'name=="meta-1-value and metaData=q=\'name!="undefined and metaData=q=\'name=="meta-3-value');
    });

    it('returns a query string for tags object', function() {
        var result = rsqlParser.parseRSQL({tags: query.tags});
        expect(result).toBe('tags=q=\'name.eq==massbank"\' and tags=q=\'name.eq==GNPS"\' and tags=q=\'name.eq==LCMS"\'');
    });

    it ('returns a query string for compound AND metadata object', function() {
        var result = rsqlParser.parseRSQL({compound: query.compound, metadata: query.metadata});
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound.names=q=\'name=="testCompoundName"\' and metaData=q=\'name=="meta-1-value and metaData=q=\'name!="undefined and metaData=q=\'name=="meta-3-value');
    });

    it('returns a query string for compound AND tags object', function() {
        var result = rsqlParser.parseRSQL({compound: query.compound, tags: query.tags});
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound.names=q=\'name=="testCompoundName"\' and tags=q=\'name.eq==massbank"\' and tags=q=\'name.eq==GNPS"\' and tags=q=\'name.eq==LCMS"\'');
    });

    it('returns a query string for metadata AND tags object', function() {
        var result = rsqlParser.parseRSQL({metadata: query.metadata, tags: query.tags});
        expect(result).toBe('metaData=q=\'name=="meta-1-value and metaData=q=\'name!="undefined and metaData=q=\'name=="meta-3-value and tags=q=\'name.eq==massbank"\' and tags=q=\'name.eq==GNPS"\' and tags=q=\'name.eq==LCMS"\'');
    });

    it('returns a query string for compound AND metadata AND tags object', function() {
        var result = rsqlParser.parseRSQL(query);
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound.names=q=\'name=="testCompoundName"\' and metaData=q=\'name=="meta-1-value and metaData=q=\'name!="undefined and metaData=q=\'name=="meta-3-value and tags=q=\'name.eq==massbank"\' and tags=q=\'name.eq==GNPS"\' and tags=q=\'name.eq==LCMS"\'');
    });

});