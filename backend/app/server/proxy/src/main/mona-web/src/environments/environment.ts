// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
import {NgxLoggerLevel} from 'ngx-logger';

export const environment = {
  production: false,
  // REST_BACKEND_SERVER: 'http://0.0.0.0:8080',
  // REST_BACKEND_SERVER: 'http://127.0.0.1:8080',
  REST_BACKEND_SERVER: 'http://127.0.0.1:8010/127.0.0.1:8080', // WORKS FOR DEV WHEN USING LOCAL CORSPROXY SCRIPT
  APP_NAME: 'MassBank of North America',
  APP_NAME_ABBR: 'MoNA',
  APP_VERSION: 'v1.1',
  google_analytics: '',
  loggerLevel: NgxLoggerLevel.DEBUG,
  serverLevel: NgxLoggerLevel.OFF,
  ctsUrl: 'http://cts.fiehnlab.ucdavis.edu'
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
