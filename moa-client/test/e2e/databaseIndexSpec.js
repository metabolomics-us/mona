describe('e2e testing database index', function() {
    beforeEach(function() {
        browser.get('#/spectra/dbindex');
    });

    it('should access the correct page', function() {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/spectra/dbindex');

        // Verify content
        expect(element(by.css('#page-wrapper')).getText()).toContain('Database Index');
    });

    it('should load data for at least one metadata type', function() {
        // Check that metadata fields are loaded
        element.all(by.repeater('(name, data) in fieldData')).then(function(items) {
            expect(items.length).toBeGreaterThan(0);
        });

        // Check that values for each metdata field are loaded
        // TODO: Cannot resolve protractor error with nested repeaters
        element.all(by.repeater('meta in data')).then(function(items) {
            expect(items.length).toBeGreaterThan(0);
        });
    });
});