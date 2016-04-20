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

    it('returns a query string', function() {
    //    console.log(query);
        console.log(rsqlParser);
    });

});