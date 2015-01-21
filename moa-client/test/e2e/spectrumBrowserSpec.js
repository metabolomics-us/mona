describe('e2e testing spectrum browser', function() {
    var DEFAULT_LOAD_NUMBER = 20;

    beforeEach(function() {
        browser.get('#/spectra/browse');
    });


    it('should access the correct page when viewing the spectrum browser', function() {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/spectra/browse');

        // Verify content
        expect(element(by.css('#page-wrapper')).getText()).toContain('Name');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Structure');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Accurate Mass');
    });


    var verifyInitialLoad = function() {
        element.all(by.repeater('spectrum in spectra')).then(function(items) {
            expect(items.length).toBe(DEFAULT_LOAD_NUMBER);
        });

        element.all(by.css('canvas')).then(function(items) {
            expect(items.length).toBe(4 * DEFAULT_LOAD_NUMBER);
        });
    };

    it('should load spectra automatically', function() {
        // Verify that spectra load
        verifyInitialLoad();
    });

    it('should load more spectra when scrolling to the bottom of the page', function() {
        // Verify that spectra load
        verifyInitialLoad();

        // Verify that infinite scroll works
        browser.executeScript('window.scrollTo(0, 10000);').then(function () {
            element.all(by.repeater('spectrum in spectra')).then(function(items) {
                expect(items.length).toBeGreaterThan(DEFAULT_LOAD_NUMBER);
            });

            element.all(by.css('canvas')).then(function(items) {
                expect(items.length).toBeGreaterThan(4 * DEFAULT_LOAD_NUMBER);
            });
        });
    });


    it('should jump to the spectrum display when accessed', function() {
        // Click on the first canvas object
        element.all(by.css('canvas')).first().click();

        // Verify that the page has updated to the spectrum viewer
        expect(browser.getCurrentUrl()).toContain('/#/spectra/display/');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Summary for spectrum');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Download');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Mass Spectrum');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Ion Table');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Mass Spectrum Metadata');
        expect(element(by.css('#page-wrapper')).getText()).toContain('Comments');
    });
});