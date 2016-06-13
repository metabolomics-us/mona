'use strict';

describe('factory: Query String Builder - ', function() {
    beforeEach(module('moaClientApp'));

    var rootScope, service;


    beforeEach(inject(function(_queryStringBuilder_) {
        service = _queryStringBuilder_;

        //rootScope = $injector.get('$rootScope');
    }));


    var query = {
        compound: [{name: 'testCompoundName'}],
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

    it('can be inintialized', function() {
        console.log(service);
        expect(service).toBeDefined();
    });

    it('can be inintialized', function() {
        expect(query).toBeDefined();
    });

    /*it('returns a query string for compound object', function() {
        var result = rsqlParser.parseRSQL({compound: query.compound});
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound.names=q=\'name=="testCompoundName"\'');
    });

    it('returns a query string for an inchiKey', function() {
        var result = rsqlParser.parseRSQL({compound: {inchiKey: 1234567}});
        expect(result).toBe('biologicalCompound=q=inchiKey=="1234567"\' or chemicalCompound=q=inchiKey=="1234567"\'');
    });

    it('returns a query string for compound AND inchiKey', function() {
        var result = rsqlParser.parseRSQL({compound: {name: 'testCompoundName', inchiKey: 1234567}});
        expect(result).toBe('biologicalCompound.names=q=\'name=="testCompoundName"\' or biologicalCompound=q=inchiKey=="1234567"\' or chemicalCompound.names=q=\'name=="testCompoundName"\' or chemicalCompound=q=inchiKey=="1234567"\'');
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
*/
});
