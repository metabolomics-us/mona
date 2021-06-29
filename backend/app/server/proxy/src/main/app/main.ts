import "angular";
import "angular-resource";
import "angularjs-toaster";
import "es5-shim";
import "json3";
import "jquery";
import "bootstrap/dist/js/bootstrap.min"
import "bootstrap-multiselect/dist/js/bootstrap-multiselect";
import "angular-cookies";
import "angular-sanitize";
import "angular-animate";
import "angular-ui-bootstrap";
import "bootstrap";
import "angular-dialog-service";
import "flot";
import "ng-tags-input";
//import "angular-cts-service";
import "angular-msp-parser";
import "angular-mgf-parser";
import "angular-massbank-parser";
import "angular-masspec-plotter";
import "ng-file-upload";
import "angular-filter";
import "d3/d3";
import "nvd3";
import "angular-nvd3";
import "./lib/chemdoodle-test/ChemDoodleWeb";

import "./app";
import "./polyfills"
import "./services";
import "./errors";
import "./events";
import "./routes";
import "./components";
import "./directives";
import "./filters";

import {NgModule} from '@angular/core';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {BrowserModule} from '@angular/platform-browser';
import {CtsLibModule} from "angular-cts-service/dist/cts-lib";
import {RouterModule} from "@angular/router";
import{CookieService} from "ngx-cookie-service";
import {UpgradeModule} from '@angular/upgrade/static';
import {HttpClientModule} from "@angular/common/http";
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";

import {CtsService} from "angular-cts-service/dist/cts-lib";
import {CtsConstants} from "angular-cts-service/dist/cts-lib";
import {ChemifyService} from "angular-cts-service/dist/cts-lib";

import {Download} from "./services/persistence/download.resource";
import {Metadata} from "./services/persistence/metadata.resource";
import {Spectrum} from "./services/persistence/spectrum.resource";
import {Statistics} from "./services/persistence/statistics.resource";
import {Submitter} from "./services/persistence/submitter.resource";
import {Tag} from "./services/persistence/tag.resource";
import {MetadataOptimization} from "./services/optimization/metadata-optimization.service";
import {SpectraQueryBuilderService} from "./services/query/spectra-query-builder.service";
import {QueryCacheService} from "./services/cache/query-cache.service";
import {QueryStringHelper} from "./services/query/query-string-helper.service";
import {QueryStringBuilder} from "./services/query/query-string-builder.service";
import {CookieMain} from "./services/cookie/cookie-main.service";
import {AsyncService} from "./services/upload/async.service";
import {AuthenticationService} from "./services/authentication.service";
import {CompoundConversionService} from "./services/compound-conversion.service"
import {RegistrationService} from "./services/registration.service";

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule,
        HttpClientModule,
        CtsLibModule,
        NgbModule,
        LoggerModule.forRoot({
            level: NgxLoggerLevel.DEBUG,
            serverLogLevel: NgxLoggerLevel.OFF
        }),
        RouterModule.forRoot([])

    ],
    providers: [
        CookieService,
        CookieMain,
        Download,
        Metadata,
        Spectrum,
        Statistics,
        Submitter,
        Tag,
        MetadataOptimization,
        SpectraQueryBuilderService,
        QueryCacheService,
        QueryStringHelper,
        QueryStringBuilder,
        AsyncService,
        CtsConstants,
        CtsService,
        ChemifyService,
        AuthenticationService,
        RegistrationService,
        CompoundConversionService
    ]
})

export class AppModule {
    ngDoBootstrap(){

    }
}

platformBrowserDynamic().bootstrapModule(AppModule).then(platformRef => {
    console.log("Bootstrapping in Hybrid Mode");
    const upgrade = platformRef.injector.get(UpgradeModule) as UpgradeModule;
    upgrade.bootstrap(document.body, ['moaClientApp']);
})


