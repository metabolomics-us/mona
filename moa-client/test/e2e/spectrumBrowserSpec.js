describe('e2e testing spectrum browser', function() {
    beforeEach(function() {
        browser().navigateTo('/#/spectra/browse');
    });

    it('should access the correct page when viewing the spectrum browser', function() {
        expect(browser().location().path()).toBe("/spectra/browse");
        expect(element('#page-wrapper').html()).toContain('Chem. Structure');
        expect(repeater('canvas').count()).toEqual(80);
    });

    it('should jump to the spectrum displayer when accessed', function() {
        browser().navigateTo('#/spectra/display/135');

        expect(browser().location().path()).toBe("/spectra/display/135");
        expect(element('#page-wrapper').html()).toContain('Mass Spectrum');
        expect(element('#page-wrapper').html()).toContain('Table of Ions');
        expect(repeater('canvas').count()).toEqual(4);
    });

    it('should jump to a spectrum displayer when a spectrum is clicked', function() {
        element('canvas').click();

        expect(browser().location().path()).toContain("/spectra/display/");
        expect(element('#page-wrapper').html()).toContain('Mass Spectrum');
        expect(element('#page-wrapper').html()).toContain('Table of Ions');
        expect(repeater('canvas').count()).toEqual(4);
    });

    it('should perform a query correctly', function() {
        element('#page-wrapper button').click();
        sleep(2);

        input('query.nameFilter').enter('alanine');
        element('.modal-footer button').click();
        sleep(3);

        // Need to verify query
    });
});