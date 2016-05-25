describe('e2e testing compounds browser', function() {
    // TODO: 15 is INCORRECT - should be 20, but there is an error in the REST API
    var DEFAULT_LOAD_NUMBER = 15;

    beforeEach(function() {
        browser.get('#/compounds');
    });


    it('should access the correct page when viewing the compound browser', function() {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/compounds');

        // Verify content
        expect(element(by.css('#page-wrapper')).getText()).toContain('Name');
        expect(element(by.css('#page-wrapper')).getText()).toContain('InChIKey');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Structure');
    });


    var verifyInitialLoad = function() {
        element.all(by.repeater('compound in compounds')).then(function(items) {
            expect(items.length).toBe(DEFAULT_LOAD_NUMBER);
        });

        element.all(by.css('canvas')).then(function(items) {
            expect(items.length).toBe(DEFAULT_LOAD_NUMBER);
        });
    };

    it('should load compounds automatically', function() {
        // Verify that compounds load
        verifyInitialLoad();
    });

    it('should load more compounds when scrolling to the bottom of the page', function() {
        // Verify that compounds load
        verifyInitialLoad();

        // Verify that infinite scroll works
        browser.executeScript('window.scrollTo(0, 10000);').then(function () {
            element.all(by.repeater('compound in compounds')).then(function(items) {
                expect(items.length).toBeGreaterThan(DEFAULT_LOAD_NUMBER);
            });

            element.all(by.css('canvas')).then(function(items) {
                expect(items.length).toBeGreaterThan(DEFAULT_LOAD_NUMBER);
            });
        });
    });


    it('should jump to an inchi query when a compound is clicked', function() {
        // Click on the first canvas object
        element.all(by.css('canvas')).first().click();

        // Verify that the page has updated to the spectra browser
        expect(browser.getCurrentUrl()).toContain('/#/spectra/browse');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Name');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Structure');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Accurate Mass');
    });
});
