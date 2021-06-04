import * as angular from 'angular';

class MainController {
    private static $inject = ['$rootScope', 'Spectrum', '$log'];
    private showcaseSpectraIds;
    private showcaseSpectra;
    private $log;
    private $rootScope;
    public Spectrum;

    constructor($rootScope, Spectrum, $log) {
        this.$rootScope = $rootScope;
        this.Spectrum = Spectrum;
        this.$log = $log;
    }

    checkHttpError() {
        while (this.$rootScope.httpError.length !== 0) {
            let curError = this.$rootScope.httpError.pop();

            if (angular.isDefined(curError)) {
                let method = curError.config.method;
                let url = curError.config.url;
                let status = curError.status;

                let message = 'Unable to ' + method + ' from ' + url + ' Status: ' + status;

                this.$log.error(message);
            }
        }
    }

    $onInit(){
        this.showcaseSpectraIds = ['BSU00002', 'AU101801', 'UT001119'];
        this.showcaseSpectra = [];

        this.showcaseSpectraIds.forEach((id) => {
            this.Spectrum.get(
                id).then(
                (data) => {
                    this.showcaseSpectra.push(data);
                },
                (error) => {
                    this.$log.error("Failed to obtain spectrum "+ id)
                }
            );
        });
    }
}

let MainComponent = {
    selector: "main",
    templateUrl: "../views/main.html",
    bindings: {},
    controller: MainController
}

angular.module('moaClientApp')
        .component(MainComponent.selector, MainComponent);

