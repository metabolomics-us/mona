ddescribe('e2e testing spectrum query', function() {
    var DEFAULT_LOAD_NUMBER = 20;

    beforeEach(function() {
        browser.get('#/spectra/browse');

        // Access query modal
        element(by.buttonText('Open Query Dialog')).click();
    });


    it('should access the query dialog', function() {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/spectra/browse');

        // Verify content
        expect(element(by.css('div.modal-content')).getText()).toContain('Query Spectra');
        expect(element(by.css('div.modal-content')).getText()).toContain('Query by Name');
        expect(element(by.css('div.modal-content')).getText()).toContain('Query by InChIKey');
        expect(element(by.css('div.modal-content')).getText()).toContain('Query by Metadata');
        expect(element(by.css('div.modal-content')).getText()).toContain('Query by Tags');
    });


    var verifyQueryResults = function(expectResults) {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/spectra/browse');

        // Verify content
        expect(element(by.css('#page-wrapper')).getText()).toContain('Name');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Structure');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Accurate Mass');

        // Verify contents
        element.all(by.repeater('spectrum in spectra')).then(function(items) {
            if(expectResults)
                expect(items.length).toBeGreaterThan(0);
            else
                expect(items.length).toBe(0);
        });

        element.all(by.css('canvas')).then(function(items) {
            if(expectResults)
                expect(items.length).toBeGreaterThan(0);
            else
                expect(items.length).toBe(0);
        });
    };

    it('should perform a query by name correctly', function() {
        // Input text and submit query
        element(by.model('query.nameFilter')).sendKeys('alanine');
        element(by.buttonText('Submit query to system')).click();

        // Verify that we receive results
        verifyQueryResults(true);
    });

    it('should perform a query with a fake name correctly', function() {
        // Input text and submit query
        element(by.model('query.nameFilter')).sendKeys('NotAChemicalName');
        element(by.buttonText('Submit query to system')).click();

        // Verify that we receive results
        verifyQueryResults(false);
    });

    it('should perform a query by InChIKey correctly', function() {
        // Open accordion tab
        element(by.cssContainingText('div.ng-scope', 'Query by InChIKey')).click();

        // Input text and submit query
        element(by.model('query.inchiFilter')).sendKeys('AAA');
        element(by.buttonText('Submit query to system')).click();

        // Verify that we receive results
        verifyQueryResults(true);
    });

    it('should perform a query with a fake InChIKey correctly', function() {
        // Open accordion tab
        element(by.cssContainingText('div.ng-scope', 'Query by InChIKey')).click();

        // Input text and submit query
        element(by.model('query.inchiFilter')).sendKeys('123');
        element(by.buttonText('Submit query to system')).click();

        // Verify that we receive results
        verifyQueryResults(false);
    });
});