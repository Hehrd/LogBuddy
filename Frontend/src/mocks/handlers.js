import { http, HttpResponse } from 'msw'
import { mockConfigDrafts } from './mockData.js'

const dataSources = JSON.parse(mockConfigDrafts.ds).dataSources
const rules = JSON.parse(mockConfigDrafts.rule).rules

export const handlers = [
  http.get('/control-panel/health', () => HttpResponse.json({
    spark: true,
    data: true,
  })),
  http.get('/control-panel/status', () => HttpResponse.json({
    spark: {
      service: 'spark-processing',
      sleeping: false,
      queryCount: dataSources.length,
      dataSourceCount: dataSources.length,
    },
    data: {
      service: 'data-processing',
      sleeping: false,
      dataSourceCount: 3,
      ruleCount: 7,
    },
  })),
  http.get('/control-panel/datasources', () => HttpResponse.json(dataSources)),
  http.get('/control-panel/rules', () => HttpResponse.json(rules)),
  http.get('/control-panel/streams/metrics', () => HttpResponse.json({
    metrics: [
      {
        dataSourceName: 'app-logs',
        metricKey: 'log_ingestion_delay',
        metricName: 'Log ingestion delay',
        value: 1820,
        thresholdMillis: 1000,
        publishedAt: '2026-04-25T10:20:00Z',
      },
      {
        dataSourceName: 'audit-logs',
        metricKey: 'log_format_check',
        metricName: 'Log format failures',
        value: 4,
        thresholdMillis: null,
        publishedAt: '2026-04-25T10:20:00Z',
      },
    ],
  })),
  http.post('/control-panel/data-processing/:action', ({ params }) => HttpResponse.json({ ok: true, action: params.action })),
  http.get('/control-panel/queries', () => HttpResponse.json({ queries: ['app-logs', 'audit-logs'] })),
  http.get('/control-panel/queries/:dataSource', ({ params }) => HttpResponse.json({
    dataSource: params.dataSource,
    active: true,
    id: '4f78491d-04c7-4b87-95b6-0c4be31a2f2c',
    status: 'Processing new data',
  })),
  http.post('/control-panel/queries/restart', () => HttpResponse.json({ ok: true, action: 'queries/restart' })),
  http.post('/control-panel/queries/:dataSource/:action', ({ params }) => HttpResponse.json({ ok: true, dataSource: params.dataSource, action: `queries/${params.action}` })),
  http.post('/control-panel/spark/:action', ({ params }) => HttpResponse.json({ ok: true, action: params.action })),
]
