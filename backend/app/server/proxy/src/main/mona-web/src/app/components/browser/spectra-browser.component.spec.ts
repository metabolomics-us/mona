import {SpectraBrowserComponent} from "./spectra-browser.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {Spectrum} from "../../services/persistence/spectrum.resource";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Location} from "@angular/common";
import {SpectrumCacheService} from "../../services/cache/spectrum-cache.service";
import {Metadata} from "../../services/persistence/metadata.resource";
import {CookieMain} from "../../services/cookie/cookie-main.service";
import {ToasterModule, ToasterService} from "angular2-toaster";
import {GoogleAnalyticsService} from "ngx-google-analytics";
import {ActivatedRoute, Router} from "@angular/router";
import {FeedbackCacheService} from "../../services/feedback/feedback-cache.service";
import {AuthenticationService} from "../../services/authentication.service";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {BehaviorSubject, Observable, of} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {User} from "../../mocks/user.model";
import {Feedback} from "../../services/persistence/feedback.resource";
import {NgbAccordionModule} from "@ng-bootstrap/ng-bootstrap";

describe('Spectra Browser Component', () => {
  let component: SpectraBrowserComponent;
  let fixture: ComponentFixture<SpectraBrowserComponent>;
  let spectrumServiceStub: Partial<Spectrum>;
  let spectrum: Spectrum;
  let spectrumCache: SpectrumCacheService;
  let feedbackCacheStud: Partial<FeedbackCacheService>;
  let feedbackCache: FeedbackCacheService;
  let feedback: Feedback;
  let feedbackServiceStub: Partial<Feedback>;
  let authenticationServiceStub: Partial<AuthenticationService>;
  let authenticationService: AuthenticationService;
  let router: Router;
  let route: ActivatedRoute;
  let spectraQueryBuilder: SpectraQueryBuilderService;

  let fakeIsAdmin$: BehaviorSubject<boolean>;
  let fakeIsLoggedIn$: BehaviorSubject<boolean>;
  let fakeIsAuthenticated$: BehaviorSubject<boolean>;
  let fakeCurrentUser$: BehaviorSubject<User>;
  let fakeModalRequest$: BehaviorSubject<boolean>;
  let fakeLoginPayload$: BehaviorSubject<any>;
  let fakeSpectrumGetObservable$: BehaviorSubject<any>;
  let fakeSpectrumUpdateObservable$: BehaviorSubject<any>;
  let fakeSpectrumSimilarObservable$: BehaviorSubject<any>;
  let fakeSpectrumCountObservable$: BehaviorSubject<any>;
  let fakeSpectrumSearchObservable$: BehaviorSubject<any>;
  let fakeFeedbackCacheObservable$: BehaviorSubject<any>;
  let fakeFeedbackGetObservable$: BehaviorSubject<any>;
  let fakeFeedbackSaveObservable$: BehaviorSubject<any>;
  let queryParams: Observable<any>;

  beforeEach(async () => {
    fakeIsAdmin$ = new BehaviorSubject(false);
    fakeIsLoggedIn$ = new BehaviorSubject(false);
    fakeIsAuthenticated$ = new BehaviorSubject(false);
    fakeCurrentUser$ = new BehaviorSubject<User>(null)
    fakeModalRequest$ = new BehaviorSubject<boolean>(false);
    fakeLoginPayload$ = new BehaviorSubject<any>(false);

    fakeSpectrumGetObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumUpdateObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumSimilarObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumCountObservable$ = new BehaviorSubject<any>(false);
    fakeFeedbackCacheObservable$ = new BehaviorSubject<any>(false);
    fakeFeedbackGetObservable$ = new BehaviorSubject<any>(false);
    fakeFeedbackSaveObservable$ = new BehaviorSubject<any>(false);
    fakeSpectrumSearchObservable$ = new BehaviorSubject<any>(false);

    queryParams = new Observable<any>(observer => {
      const urlParams = {
        inchikey: undefined,
        splash: undefined,
        query: undefined,
        text: undefined,
        size: 10,
        pageParam: undefined,
        tableParam: undefined
      }
      observer.next(urlParams);
      observer.complete();
    });
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

    feedbackServiceStub = {
      get(): Observable<any> {
        return fakeFeedbackGetObservable$.asObservable();
      },
      save(): Observable<any> {
        return fakeFeedbackSaveObservable$.asObservable();
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
        return fakeLoginPayload$.asObservable();
      }
    };

    feedbackCacheStud = {
      resolveFeedback(): Observable<any> {
        return fakeFeedbackCacheObservable$.asObservable();
      }
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
      {provide: Spectrum, useValue: spectrumServiceStub}, SpectraQueryBuilderService, SpectrumCacheService,
      ToasterService, FeedbackCacheService, {provide: FeedbackCacheService, useValue: feedbackCacheStud}, Metadata, CookieMain,
      Feedback, {provide: ActivatedRoute, useValue: {queryParams: queryParams}}],
      imports: [
        HttpClientModule, LoggerTestingModule, RouterTestingModule.withRoutes([{
          path:'spectra/browse', component: SpectraBrowserComponent
        }]), ToasterModule,
        NgbAccordionModule
      ]
    });
    fixture = TestBed.createComponent(SpectraBrowserComponent);
    component = fixture.componentInstance;
    spectrum = TestBed.inject(Spectrum);
    spectrumCache = TestBed.inject(SpectrumCacheService);
    authenticationService = TestBed.inject(AuthenticationService);
    feedback = TestBed.inject(Feedback);
    feedbackCache = TestBed.inject(FeedbackCacheService);
    router = TestBed.inject(Router);
    route = TestBed.inject(ActivatedRoute);
    spectraQueryBuilder = TestBed.inject(SpectraQueryBuilderService)

    fixture.detectChanges();
  });

  it('reset queries', () => {
    spyOn(spectraQueryBuilder, 'executeQuery').and.callThrough();
    component.resetQuery();
    expect(spectraQueryBuilder.executeQuery).toHaveBeenCalled();
  });
});
