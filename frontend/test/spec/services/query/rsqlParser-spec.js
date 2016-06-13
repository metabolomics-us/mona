'use strict';

describe('factory: Query String Builder - ', function () {
    beforeEach(module('moaClientApp'));

    var rootScope, service, cache;

    beforeEach(inject(function (_queryStringBuilder_, $rootScope, _QueryCache_) {
        service = _queryStringBuilder_;
        rootScope = $rootScope;
        cache = _QueryCache_;
        this.query = {};
    }));


    var query = {
        compound: [{name: 'testCompoundName'}],
        metadata: [
            {name: 'meta-1', value: {eq: 'meta-1-value'}},
            {name: 'meta-2', value: {ne: 'meta-2-value'}},
            {name: 'meta-3', value: {eq: 'meta-3-value'}}
        ],
        operand: ['and', 'and'],
        tags: [
            {name: {eq: 'massbank'}},
            {name: {eq: 'GNPS'}},
            {name: {eq: 'LCMS'}}
        ]
    };


    describe('it has 2 methods', function () {
        it('has a build Query method', function () {
            expect(service.buildQuery).toBeDefined();
        });

        it('has a build Advanced Query method', function () {
            expect(service.buildAdvanceQuery).toBeDefined();
        });
    });

    describe('buildQuery parses query object and returns a RSQL string', function () {
        beforeEach(function () {
            this.query.compound = [{name: 'testCompoundName'}];
            this.query.operand = ['and','and'];
        });

        it('throws a error when operands are not present', function() {
            delete this.query.operand;
            cache.setSpectraQuery(this.query);
            expect(cache.setSpectraQuery).toThrow();
        });

        it('saves a query to the cache', function () {
            cache.setSpectraQuery(this.query);
            spyOn(cache, 'setSpectraQueryString').and.callThrough();
            service.buildQuery();
            expect(cache.setSpectraQueryString).toHaveBeenCalled();
        });
    });

});
