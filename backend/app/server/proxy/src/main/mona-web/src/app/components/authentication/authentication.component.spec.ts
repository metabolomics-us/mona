import {AuthenticationComponent} from "./authentication.component";
import {AuthenticationModalComponent} from "./authentication-modal.component";
import {RegistrationModalComponent} from "./registration-modal.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthenticationService} from "../../services/authentication.service";
import {BehaviorSubject} from "rxjs";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {User} from "../../mocks/user.model";

describe('Authentication Component', () => {
  let component: AuthenticationComponent;
  let ngbModal: NgbModal;
  let fixture: ComponentFixture<AuthenticationComponent>;
  let authenticationServiceStub: Partial<AuthenticationService>;
  let authenticationService: AuthenticationService;
  let fakeIsAdmin$: BehaviorSubject<boolean>;
  let fakeIsLoggedIn$: BehaviorSubject<boolean>;
  let fakeIsAuthenticated$: BehaviorSubject<boolean>;
  let fakeCurrentUser$: BehaviorSubject<User>;
  let fakeModalRequest$: BehaviorSubject<boolean>;

  beforeEach(async () => {
    fakeIsAdmin$ = new BehaviorSubject(false);
    fakeIsLoggedIn$ = new BehaviorSubject(false);
    fakeIsAuthenticated$ = new BehaviorSubject(false);
    fakeCurrentUser$ = new BehaviorSubject<User>(null)
    fakeModalRequest$ = new BehaviorSubject<boolean>(false);

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
      modalRequest: fakeModalRequest$.asObservable()
    };

    spyOn(authenticationServiceStub, 'isLoggedIn').and.callThrough();
    spyOn(authenticationServiceStub, 'isAdmin').and.callThrough();
    spyOn(authenticationServiceStub, 'validate').and.callThrough();
    spyOn(authenticationServiceStub, 'logout').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [AuthenticationComponent],
      providers: [{provide: AuthenticationService, useValue: authenticationServiceStub}],
      imports: [
        NgbModule, HttpClientModule, LoggerTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuthenticationComponent);
    component = fixture.componentInstance;
    ngbModal = TestBed.inject(NgbModal);

    authenticationService = TestBed.inject(AuthenticationService);
    fixture.detectChanges();
  });

  it('opens a authentication dialog for user to log in', () => {
    spyOn(ngbModal, 'open');
    component.handleLogin();
    fakeIsLoggedIn$.next(true);
    fakeIsAdmin$.next(false);
    expect(ngbModal.open).toHaveBeenCalledWith(AuthenticationModalComponent, {size: 'sm', backdrop: true});
  });

  it('opens a registration uibModal when a user is not logged in', () => {
    spyOn(ngbModal, 'open');
    fakeIsLoggedIn$.next(false);
    component.handleRegistration();
    expect(ngbModal.open).toHaveBeenCalledWith(RegistrationModalComponent, {size: 'md', backdrop: 'static'});
  });

  it('logs out a user that is currently logged in', () => {
    fakeIsLoggedIn$.next(true);
    fakeIsAdmin$.next(false);
    component.handleLogin();
    expect(authenticationServiceStub.logout).toHaveBeenCalled();
  });

  it('return true for a user that has admin rights', () => {
    fakeIsAdmin$.next(true);
    const isAdmin = component.isAdmin();
    expect(isAdmin).toBe(true);
  });

  it('returns false for a user that does not have admin rights', () => {
    fakeIsAdmin$.next(false);
    const isAdmin = component.isAdmin();
    expect(isAdmin).toBe(false);
  });

  it('removes the welcome message on logout', () => {
    fakeIsAuthenticated$.next(false);
    expect(component.welcomeMessage).toBe('Login/Register');
  });
});
