import {SpectraBrowserComponent} from "./spectra-browser.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {Spectrum} from "../../services/persistence/spectrum.resource";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Location} from "@angular/common";
import {SpectrumCacheService} from "../../services/cache/spectrum-cache.service";
import {Metadata} from "../../services/persistence/metadata.resource";
import {CookieMain} from "../../services/cookie/cookie-main.service";
import {ToasterModule} from "angular2-toaster";
import {GoogleAnalyticsService} from "ngx-google-analytics";
import {ActivatedRoute, Router} from "@angular/router";
import {FeedbackCacheService} from "../../services/feedback/feedback-cache.service";
import {AuthenticationService} from "../../services/authentication.service";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {BehaviorSubject, Observable} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {User} from "../../mocks/user.model";

describe('Spectra Browser Component', () => {
  let component: SpectraBrowserComponent;
  let fixture: ComponentFixture<SpectraBrowserComponent>;
  let spectrumServiceStub: Partial<Spectrum>;
  let spectrum: Spectrum;
  let spectraQueryBuilderStub: Partial<SpectraQueryBuilderService>;
  let spectraQueryBuilder: SpectraQueryBuilderService;
  let spectrumCacheStub: Partial<SpectrumCacheService>;
  let spectrumCache: SpectrumCacheService;
  let metadataStub: Partial<Metadata>;
  let metadata: Metadata;
  let cookieStub: Partial<CookieMain>;
  let cookie: CookieMain;
  let feedbackCacheStud: Partial<FeedbackCacheService>;
  let feedback: FeedbackCacheService;
  let authenticationServiceStub: Partial<AuthenticationService>;
  let authenticationService: FeedbackCacheService;

  let fakeIsAdmin$: BehaviorSubject<boolean>;
  let fakeIsLoggedIn$: BehaviorSubject<boolean>;
  let fakeIsAuthenticated$: BehaviorSubject<boolean>;
  let fakeCurrentUser$: BehaviorSubject<User>;
  let fakeModalRequest$: BehaviorSubject<boolean>;
  let fakeLoginPaylod$: BehaviorSubject<any>;
  let fakeSpectrumGetObservable$: BehaviorSubject<any>;
  let fakeSpectrumUpdateObservable$: BehaviorSubject<any>;
  let fakeSpectrumSimilarObservable$: BehaviorSubject<any>;
  let fakeSpectrumCountObservable$: BehaviorSubject<any>;
  let fakeSpectrumSearchObservable$: BehaviorSubject<any>;

  beforeEach(async () => {
    fakeIsAdmin$ = new BehaviorSubject(false);
    fakeIsLoggedIn$ = new BehaviorSubject(false);
    fakeIsAuthenticated$ = new BehaviorSubject(false);
    fakeCurrentUser$ = new BehaviorSubject<User>(null)
    fakeModalRequest$ = new BehaviorSubject<boolean>(false);
    fakeLoginPaylod$ = new BehaviorSubject<any>(false);

    fakeSpectrumGetObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumUpdateObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumSimilarObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumCountObservable$ = new BehaviorSubject<any>(false);

    spectrumServiceStub = {
      get(): Observable<any> {
        return fakeSpectrumGetObservable$.asObservable();
      },
      update(): Observable<any> {
        return fakeSpectrumUpdateObservable$.asObservable();
      },
      searchSimilarSpectra(): Observable<any> {
        return fakeSpectrumSimilarObservable$.asObservable();
      },
      searchSpectraCount(): Observable<any> {
        return fakeSpectrumCountObservable$.asObservable()
      },
      searchSpectra(): Observable<any> {
        return fakeSpectrumSearchObservable$.asObservable();
      }
    };

    authenticationServiceStub = {
      isLoggedIn(): boolean {
        return fakeIsLoggedIn$.value;
      },
      isAdmin(): boolean {
        return fakeIsAdmin$.value;
      },
      isAuthenticated: fakeIsAuthenticated$.asObservable(),
      logout(): void {

      },
      validate(): void {

      },
      currentUser: fakeCurrentUser$.asObservable(),
      modalRequest: fakeModalRequest$.asObservable(),
      login(): Observable<any> {
        return fakeLoginPaylod$.asObservable();
      }
    };

    spectraQueryBuilderStub = {

    }

    spyOn(authenticationServiceStub, 'isLoggedIn').and.callThrough();
    spyOn(authenticationServiceStub, 'isAdmin').and.callThrough();
    spyOn(authenticationServiceStub, 'validate').and.callThrough();
    spyOn(authenticationServiceStub, 'logout').and.callThrough();
    spyOn(authenticationServiceStub, 'login').and.callThrough();

    spyOn(spectrumServiceStub, 'get').and.callThrough();
    spyOn(spectrumServiceStub, 'update').and.callThrough();
    spyOn(spectrumServiceStub, 'searchSimilarSpectra').and.callThrough();
    spyOn(spectrumServiceStub, 'searchSpectra').and.callThrough();
    spyOn(spectrumServiceStub, 'searchSpectraCount').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [SpectraBrowserComponent],
      providers: [{provide: AuthenticationService, useValue: authenticationServiceStub}, Location, GoogleAnalyticsService,
      Router, ActivatedRoute, {provide: Spectrum, useValue: spectrumServiceStub}],
      imports: [
        HttpClientModule, LoggerTestingModule, RouterTestingModule, ToasterModule
      ]
    });
    fixture = TestBed.createComponent(SpectraBrowserComponent);
    component = fixture.componentInstance;
    spectrum = TestBed.inject(Spectrum);

    fixture.detectChanges();
  });

  it('executes queries', () => {

  });
});
