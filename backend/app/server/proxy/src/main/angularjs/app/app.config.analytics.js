'use strict';

/**
 * Configure Google Analytics
 */
export default function globals(AnalyticsProvider) {
  AnalyticsProvider
    .setAccount('UA-87692241-2')
    .trackPages(true)
    .ignoreFirstPageLoad(true);
}

globals.$inject = ['AnalyticsProvider'];