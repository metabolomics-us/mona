describe('e2e testing routes', function() {
	beforeEach(function() {
		browser().navigateTo('/');
	});

	it('should jump to the /spectra/browse when accessed', function() {
		browser().navigateTo('#/spectra/browse');
		expect(browser().location().path()).toBe("/spectra/browse");
	});

	it('should jump to the /spectra/display/135 when accessed', function() {
		browser().navigateTo('#/spectra/display/135');
		expect(browser().location().path()).toBe("/spectra/display/135");
	});
});