ddescribe('e2e testing compounds browser', function() {
    beforeEach(function() {
        browser().navigateTo('/#/compounds');
    });

    it('should access the correct page when viewing the compound browser', function() {
        expect(browser().location().path()).toBe("/compounds");
        expect(element('#page-wrapper').html()).toContain('InChIKey');
        expect(repeater('canvas').count()).toEqual(20);
    });

    it('should jump to an inchi query when a compound is clicked', function() {
        element('canvas').click();

        expect(browser().location().path()).toContain("/spectra/browse/");
        expect(element('#page-wrapper').html()).toContain('Chem. Structure');
    });
});