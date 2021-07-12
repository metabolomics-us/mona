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
import {CommonModule} from "@angular/common";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {BrowserModule} from '@angular/platform-browser';
import {CtsLibModule} from "angular-cts-service/dist/cts-lib";
import {RouterModule} from "@angular/router";
import{CookieService} from "ngx-cookie-service";
import {UpgradeModule} from '@angular/upgrade/static';
import {HttpClientModule} from "@angular/common/http";
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";

import {CtsService, CtsConstants, ChemifyService} from "angular-cts-service/dist/cts-lib";
import {NgMassSpecPlotterModule} from "@wcmc/ng-mass-spec-plotter";
import {MassbankParserLibService, MassbankParserLibModule} from "angular-massbank-parser/dist/massbank-parser-lib";
import {MgfParserLibModule, MgfParserLibService} from "angular-mgf-parser/dist/mgf-parser-lib";
import {MspParserLibService, MspParserLibModule} from "angular-msp-parser/dist/msp-parser-lib";

import {Download} from "./services/persistence/download.resource";
import {Metadata} from "./services/persistence/metadata.resource";
import {Spectrum} from "./services/persistence/spectrum.resource";
import {SpectrumCacheService} from "./services/cache/spectrum-cache.service";
import {Statistics} from "./services/persistence/statistics.resource";
import {Submitter} from "./services/persistence/submitter.resource";
import {TagService} from "./services/persistence/tag.resource";
import {MetadataOptimization} from "./services/optimization/metadata-optimization.service";
import {SpectraQueryBuilderService} from "./services/query/spectra-query-builder.service";
import {QueryCacheService} from "./services/cache/query-cache.service";
import {QueryStringHelper} from "./services/query/query-string-helper.service";
import {QueryStringBuilder} from "./services/query/query-string-builder.service";
import {CookieMain} from "./services/cookie/cookie-main.service";
import {AsyncService} from "./services/upload/async.service";
import {UploadLibraryService} from "./services/upload/upload-library.service";
import {AuthenticationService} from "./services/authentication.service";
import {CompoundConversionService} from "./services/compound-conversion.service"
import {RegistrationService} from "./services/registration.service";

import{SpectrumReviewComponent} from "./directives/feedback/spectrum-review.component";
import {ErrorHandleComponent} from "./directives/compound/error-handle.component";
import {DisplayCompoundComponent} from "./directives/compound/display-compound.component";
import {GwMetaQueryInputComponent} from "./directives/metadata/gw-meta-query-input.component";
import {MetadataQueryComponent} from "./directives/metadata/metadata-query.component";
import {SplashQueryComponent} from "./directives/metadata/splash-query.component";
import {SubmitterQueryComponent} from "./directives/metadata/submitter-query.component";
import {TagDisplayComponent} from "./directives/metadata/tag-display.component";
import {TagQueryComponent} from "./directives/metadata/tag-query.component";
import {TypeaheadFocusComponent} from "./directives/metadata/typeahead-focus.component";
import {AdminDropDownComponent} from "./directives/navbar/admin-dropdown.component";
import {BrowseDropDownComponent} from "./directives/navbar/browse-dropdown.component";
import {DownloadComponent} from "./directives/navbar/download.component";
import {ResourceDropDownComponent} from "./directives/navbar/resource-dropdown.component";
import {TitleHeaderComponent} from "./directives/navbar/title-header.component";
import {UploadComponent} from "./directives/navbar/upload.component";
import {AdvancedSearchFormComponent} from "./directives/query/advanced-search-form.component";
import {KeywordSearchFormComponent} from "./directives/query/keyword-search-form.component";
import {QueryTreeComponent} from "./directives/query/query-tree.component";
import {DisplayCompoundPipe} from "./filters/display-compound.pipe";

@NgModule({
    imports: [
        BrowserModule,
        CommonModule,
        UpgradeModule,
        HttpClientModule,
        CtsLibModule,
        NgMassSpecPlotterModule,
        MassbankParserLibModule,
        MgfParserLibModule,
        MspParserLibModule,
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
        SpectrumCacheService,
        Statistics,
        Submitter,
        TagService,
        MetadataOptimization,
        SpectraQueryBuilderService,
        QueryCacheService,
        QueryStringHelper,
        QueryStringBuilder,
        AsyncService,
        UploadLibraryService,
        CtsConstants,
        CtsService,
        ChemifyService,
        AuthenticationService,
        RegistrationService,
        CompoundConversionService,
        MassbankParserLibService,
        MgfParserLibService,
        MspParserLibService
    ],

    declarations: [
        DisplayCompoundPipe,
        SpectrumReviewComponent,
        ErrorHandleComponent,
        DisplayCompoundComponent,
        GwMetaQueryInputComponent,
        MetadataQueryComponent,
        SplashQueryComponent,
        SubmitterQueryComponent,
        TagDisplayComponent,
        TagQueryComponent,
        TypeaheadFocusComponent,
        AdminDropDownComponent,
        BrowseDropDownComponent,
        DownloadComponent,
        ResourceDropDownComponent,
        TitleHeaderComponent,
        UploadComponent,
        AdvancedSearchFormComponent,
        KeywordSearchFormComponent,
        QueryTreeComponent
    ],

    entryComponents: [
        SpectrumReviewComponent,
        ErrorHandleComponent,
        DisplayCompoundComponent,
        GwMetaQueryInputComponent,
        MetadataQueryComponent,
        SplashQueryComponent,
        SubmitterQueryComponent,
        TagDisplayComponent,
        TagQueryComponent,
        TypeaheadFocusComponent,
        AdminDropDownComponent,
        BrowseDropDownComponent,
        DownloadComponent,
        ResourceDropDownComponent,
        TitleHeaderComponent,
        UploadComponent,
        AdvancedSearchFormComponent,
        KeywordSearchFormComponent,
        QueryTreeComponent
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


