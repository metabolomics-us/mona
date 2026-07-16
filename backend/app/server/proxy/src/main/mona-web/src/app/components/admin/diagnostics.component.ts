import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../services/authentication.service';
import {AdminService} from '../../services/persistence/admin.resource';
import {NGXLogger} from 'ngx-logger';
import {ToasterService} from 'angular2-toaster';
import {faSyncAlt, faBomb} from '@fortawesome/free-solid-svg-icons';
import {Subscription} from 'rxjs';
import * as d3 from 'd3';
import 'nvd3';

@Component({
  selector: 'diagnostics',
  templateUrl: '../../views/admin/diagnostics.html'
})
export class DiagnosticsComponent implements OnInit, OnDestroy {
  faSyncAlt = faSyncAlt;
  faBomb = faBomb;
  services: { [key: string]: string };
  serviceKeys: string[];
  selectedService: string;
  hours: number;
  logs: any[];
  serviceExists: boolean;
  truncated: boolean;
  loading: boolean;
  authSubscription: Subscription;
  chartOptions: any;
  chartData: any[];
  private servicesLoaded: boolean;

  constructor(public auth: AuthenticationService, public adminService: AdminService,
              public logger: NGXLogger, public toaster: ToasterService) {}

  ngOnInit() {
    this.services = {};
    this.serviceKeys = [];
    this.selectedService = null;
    this.hours = 1;
    this.logs = [];
    this.serviceExists = true;
    this.truncated = false;
    this.loading = false;
    this.servicesLoaded = false;
    this.chartOptions = null;
    this.chartData = [];

    // Wait for isAuthenticated to actually settle before loading services
    this.authSubscription = this.auth.isAuthenticated.subscribe(() => {
      if (!this.servicesLoaded && this.auth.isAdmin()) {
        this.servicesLoaded = true;
        this.loadServices();
      }
    });
  }

  ngOnDestroy() {
    this.authSubscription.unsubscribe();
  }

  loadServices() {
    this.adminService.getDiagnosticServices(this.auth.getCurrentUser().accessToken).subscribe((services: any) => {
      this.services = services || {};
      this.serviceKeys = Object.keys(this.services).sort();
      if (this.serviceKeys.length > 0) {
        this.selectedService = this.serviceKeys[0];
        this.refresh();
      }
    }, (error) => {
      this.logger.error('Failed to load diagnostic services: ' + error);
      this.toaster.pop({
        type: 'error',
        title: 'Failed to load services',
        body: `${error.message}`
      });
    });
  }

  refresh() {
    if (!this.auth.isAdmin() || !this.selectedService) {
      return;
    }

    this.loading = true;
    this.adminService.getDiagnosticLogs(this.auth.getCurrentUser().accessToken, this.selectedService, this.hours).subscribe((result: any) => {
      const entries = (result && result.entries) || [];
      this.logs = entries.map((entry: any) => ({...entry, expanded: false, trace: null, traceLoading: false}));
      this.serviceExists = !result || result.exists !== false;
      this.truncated = !!(result && result.truncated);
      this.buildChart(entries);
      this.loading = false;
    }, (error) => {
      this.loading = false;
      this.logger.error('Failed to load diagnostic logs: ' + error);
      this.toaster.pop({
        type: 'error',
        title: 'Failed to load logs',
        body: `${error.message}`
      });
    });
  }

  // Buckets entries into ~24 evenly-sized time slots across the selected window and builds
  // an nvd3 line chart from them, so peaks of errors are visible at a glance above the table
  buildChart(entries: any[]) {
    const bucketCount = 24;
    const now = Date.now();
    const windowMs = this.hours * 3600000;
    const bucketMs = windowMs / bucketCount;
    const start = now - windowMs;

    const buckets = Array.from({length: bucketCount}, (_, i) => ({start: start + i * bucketMs, count: 0}));
    entries.forEach((entry) => {
      const index = Math.min(bucketCount - 1, Math.max(0, Math.floor((entry.timestamp - start) / bucketMs)));
      buckets[index].count++;
    });

    const tickPattern = this.hours > 24 ? '%m/%d %H:%M' : '%H:%M';
    this.chartOptions = {
      chart: {
        type: 'lineChart',
        height: 220,
        margin: {left: 60, right: 40, top: 20, bottom: 40},
        x: (d) => d.x,
        y: (d) => d.y,
        useInteractiveGuideline: true,
        showLegend: false,
        duration: 250,
        xAxis: {
          tickFormat: (d) => d3.time.format(tickPattern)(new Date(d)),
          showMaxMin: false
        },
        yAxis: {
          axisLabel: 'Errors',
          tickFormat: d3.format('d'),
          showMaxMin: false
        },
        yDomain: [0, Math.max(1, ...buckets.map(b => b.count))],
        interpolate: 'monotone'
      }
    };
    this.chartData = [{
      key: 'Errors',
      color: '#337ab7',
      values: buckets.map(b => ({x: b.start + bucketMs / 2, y: b.count}))
    }];
  }

  toggleExpand(entry: any) {
    entry.expanded = !entry.expanded;
    if (entry.expanded && entry.trace === null && !entry.traceLoading) {
      entry.traceLoading = true;
      this.adminService.getDiagnosticLogTrace(this.auth.getCurrentUser().accessToken, this.selectedService, entry.timestamp).subscribe((trace: any) => {
        entry.trace = trace || '';
        entry.traceLoading = false;
      }, (error) => {
        entry.traceLoading = false;
        this.logger.error('Failed to load stack trace: ' + error);
      });
    }
  }
}
