import {Spectrum} from "../services/persistence/spectrum.resource";
import {NGXLogger} from "ngx-logger";
import {Inject, Component, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'main',
    templateUrl: '../views/main.html'
})
export class MainComponent implements OnInit{
    private showcaseSpectraIds;
    private showcaseSpectra;

    constructor(@Inject(Spectrum) private spectrum: Spectrum, @Inject(NGXLogger) private logger: NGXLogger) {}

    /* checkHttpError() {
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
    }*/

    ngOnInit(){
        this.showcaseSpectraIds = ['BSU00002', 'AU101801', 'UT001119'];
        this.showcaseSpectra = [];

        this.showcaseSpectraIds.forEach((id) => {
            this.spectrum.get(
                id).then(
                (data) => {
                    this.showcaseSpectra.push(data);
                },
                (error) => {
                    this.logger.error("Failed to obtain spectrum "+ id)
                }
            );
        });
    }
}

angular.module('moaClientApp')
        .directive('main', downgradeComponent({
            component: MainComponent
        }));

