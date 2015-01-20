describe('e2e testing main page', function() {
    beforeEach(function() {
        browser.get('#/');
    });

    it('should access the correct page', function() {
        // Verify url
        expect(browser.getCurrentUrl()).toContain('/#/');

        // Verify content
        expect(element(by.css('#page-wrapper')).getText()).toContain('Here should be a description what we got to offer....');
    });
});