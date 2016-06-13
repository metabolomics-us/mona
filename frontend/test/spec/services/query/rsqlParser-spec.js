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
            this.query.operand = ['and', 'and'];
        });

        it('throws a error when operands are not present', function () {
            delete this.query.operand;
            cache.setSpectraQuery(this.query);
            expect(cache.setSpectraQuery).toThrow();
        });

        it('returns default query string when query object is empty', function () {
            delete this.query.compound;
            cache.setSpectraQuery(this.query);
            service.buildQuery();
            expect(cache.getSpectraQuery('string')).toEqual('/rest/spectra');
        });

        it('saves a query to the cache', function () {
            cache.setSpectraQuery(this.query);
            spyOn(cache, 'setSpectraQueryString').and.callThrough();
            service.buildQuery();
            expect(cache.setSpectraQueryString).toHaveBeenCalled();
        });

        it('returns an rsql string for compound name query', function () {
            cache.setSpectraQuery(this.query);
            service.buildQuery();
            expect(cache.getSpectraQuery('string')).toEqual('compound.names=q=\'name=match=".*testCompoundName.*"\'');
        });

        it('returns an rsql string for query object', function () {
            this.query = {
                compound: [{inchiKey: 2342232}, {classification: 'test class'}],
                operand: ['and', 'and'],
                compoundDa: [{'exact mass': 23.5}, {tolerance: 1.5}],
                formula: 'test formula',
                groupMeta: {
                    'instrument type': ['instrument1', 'instrument2', 'instrument3'],
                    'ion mode': ['positive'],
                    'ms type': ['MS1', 'MS2']
                }

            };

            cache.setSpectraQuery(this.query);
            service.buildQuery();
            var qString = cache.getSpectraQuery('string');

            expect(qString).toEqual('compound.inchiKey==2342232" and compound.metaData=q=\'name=="exact mass" and value>="22" or value<="25"\' ' +
                'and compound.metaData=q=\'name=="formula" and value=="test formula"\' and (metaData=q=\'name=="instrument type" and value=="instrument1"\' ' +
                'or metaData=q=\'name=="instrument type" and value=="instrument2"\' or metaData=q=\'name=="instrument type" and value=="instrument3"\') ' +
                'and (metaData=q=\'name=="ion mode" and value=="positive"\') and (metaData=q=\'name=="ms type" and value=="MS1"\' ' +
                'or metaData=q=\'name=="ms type" and value=="MS2"\')');
        });
    });

    describe('buildAdvanceQuery parses query object and returns a RSQL string', function () {
        beforeEach(function () {
            this.query.operand = ['and', 'and', 'and'];
        });


    });

});
