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
import {ReactiveFormsModule} from "@angular/forms";
import {TagInputModule} from "ngx-chips";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {BrowserModule} from '@angular/platform-browser';
import {CtsLibModule} from "angular-cts-service/dist/cts-lib";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ToasterModule, ToasterService} from "angular2-toaster";
import {NgxGoogleAnalyticsModule} from "ngx-google-analytics";
import {RouterModule} from "@angular/router";
import{CookieService} from "ngx-cookie-service";
import {UpgradeModule} from '@angular/upgrade/static';
import {HttpClientModule} from "@angular/common/http";
import {NgChemdoodleModule} from "@wcmc/ng-chemdoodle";
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";
import {FormsModule} from "@angular/forms";

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
import {KeywordSearchFormComponent} from "./directives/query/keyword-search-form.component";
import {QueryTreeViewComponent} from "./directives/query/query-tree-view.component";
import {SpectraUploadProgressComponent} from "./directives/spectra/spectra-upload-progress.component";
import {SimilaritySearchFormComponent} from "./directives/query/similarity-search-form.component";
import {DownloadNotifErrorModalComponent} from "./directives/spectra/download-notif-error-modal.component";
import {DownloadNotifModalComponent} from "./directives/spectra/download-notif-modal.component";
import {SpectraDownloadComponent} from "./directives/spectra/spectra-download.component";
import {SpectraLibraryComponent} from "./directives/spectra/spectra-library.component";
import {SpectraPanelComponent} from "./directives/spectra/spectra-panel.component";
import {SpectraCountForUserComponent} from "./directives/submitter/spectra-count-for-user.component";
import {SpectraScoreForUserComponent} from "./directives/submitter/spectra-score-for-user.component";
import {SpectraTopScoresForUsersComponent} from "./directives/submitter/spectra-top-scores-for-users.component";
import {SubmitterFormComponent} from "./directives/submitter/submitter-form.component";
import {AuthenticationComponent} from "./components/authentication/authentication.component";
import {AuthenticationModalComponent} from "./components/authentication/authentication-modal.component";
import {RegistrationModalComponent} from "./components/authentication/registration-modal.component";
import {QueryTreeComponent} from "./components/browser/query-tree.component";
import {DocumentationTermComponent} from "./components/documentation/documentation-term.component";
import {SubmitterComponent} from "./components/submitter/submitter.component";
import {SubmitterModalComponent} from "./components/submitter/submitter-modal.component";
import {SubmitterProfileComponent} from "./components/submitter/submitter-profile.component";
import {SpectrumViewerComponent} from "./components/browser/spectrum-viewer.component";
import {AdvancedUploaderComponent} from "./components/upload/advanced-uploader.component";
import {BasicUploaderComponent} from "./components/upload/basic-uploader.component";
import {SpectraUploadComponent} from "./components/upload/spectra-upload.component";
import {UploadPageComponent} from "./components/upload/upload-page.component";
import {MainComponent} from "./components/main.component";
import {SearchBoxComponent} from "./components/search-box.component";
import {FilterPipe} from "./filters/filter.pipe";
import {SlicePipe} from "@angular/common";

@NgModule({
    imports: [
        BrowserModule,
        CommonModule,
        UpgradeModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        CtsLibModule,
        NgMassSpecPlotterModule,
        MassbankParserLibModule,
        MgfParserLibModule,
        MspParserLibModule,
        NgChemdoodleModule,
        NgbModule,
        LoggerModule.forRoot({
            level: NgxLoggerLevel.DEBUG,
            serverLogLevel: NgxLoggerLevel.OFF
        }),
        BrowserAnimationsModule,
        ToasterModule.forRoot(),
        NgxGoogleAnalyticsModule.forRoot('UA-87692241-2'),
        RouterModule.forRoot([]),
        TagInputModule

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
        MspParserLibService,
        FilterPipe,
        SlicePipe
    ],

    declarations: [
        FilterPipe,
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
        KeywordSearchFormComponent,
        QueryTreeViewComponent,
        QueryTreeComponent,
        SpectraUploadProgressComponent,
        SimilaritySearchFormComponent,
        DownloadNotifErrorModalComponent,
        DownloadNotifModalComponent,
        SpectraDownloadComponent,
        SpectraLibraryComponent,
        SpectraPanelComponent,
        SpectraCountForUserComponent,
        SpectraScoreForUserComponent,
        SpectraTopScoresForUsersComponent,
        SubmitterFormComponent,
        AuthenticationComponent,
        AuthenticationModalComponent,
        RegistrationModalComponent,
        DocumentationTermComponent,
        SubmitterComponent,
        SubmitterModalComponent,
        SubmitterProfileComponent,
        SpectrumViewerComponent,
        AdvancedUploaderComponent,
        BasicUploaderComponent,
        SpectraUploadComponent,
        UploadPageComponent,
        MainComponent,
        SearchBoxComponent
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
        KeywordSearchFormComponent,
        QueryTreeViewComponent,
        QueryTreeComponent,
        SpectraUploadProgressComponent,
        SimilaritySearchFormComponent,
        DownloadNotifErrorModalComponent,
        DownloadNotifModalComponent,
        SpectraDownloadComponent,
        SpectraLibraryComponent,
        SpectraPanelComponent,
        SpectraCountForUserComponent,
        SpectraScoreForUserComponent,
        SpectraTopScoresForUsersComponent,
        SubmitterFormComponent,
        AuthenticationComponent,
        AuthenticationModalComponent,
        RegistrationModalComponent,
        DocumentationTermComponent,
        SubmitterComponent,
        SubmitterModalComponent,
        SubmitterProfileComponent,
        SpectrumViewerComponent,
        AdvancedUploaderComponent,
        BasicUploaderComponent,
        SpectraUploadComponent,
        UploadPageComponent,
        MainComponent,
        SearchBoxComponent
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


