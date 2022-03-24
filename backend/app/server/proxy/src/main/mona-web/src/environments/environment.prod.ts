import {NgxLoggerLevel} from 'ngx-logger';
export const environment = {
  production: true,
  REST_BACKEND_SERVER: '',
  APP_NAME: 'MassBank of North America',
  APP_NAME_ABBR: 'MoNA',
  APP_VERSION: 'v1.1',
  google_analytics: 'UA-87692241-2',
  loggerLevel: NgxLoggerLevel.INFO,
  serverLevel: NgxLoggerLevel.OFF,
  ctsUrl: 'https://cts.fiehnlab.ucdavis.edu'
};
