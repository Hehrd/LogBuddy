import { fetchJson } from './httpClient.js'

function normalizeHealth(payload) {
  const sparkUp = payload?.spark === true
  const dataUp = payload?.data === true
  return {
    service: 'Control Panel',
    status: sparkUp && dataUp ? 'UP' : 'DEGRADED',
    spark: sparkUp ? 'UP' : 'DOWN',
    dataProcessing: dataUp ? 'UP' : 'DOWN',
  }
}

function pickServiceStatus(payload, service) {
  if (service === 'spark') return payload?.spark ?? null
  if (service === 'data-processing') return payload?.data ?? null
  return payload
}

export async function fetchHealth() {
  const payload = await fetchJson('/control-panel/health')
  return normalizeHealth(payload)
}

export async function fetchControlPlaneStatus(service) {
  const payload = await fetchJson('/control-panel/status')
  return pickServiceStatus(payload, service)
}

export async function fetchControlPlaneConfig(service) {
  const [status, dataSources, rules] = await Promise.all([
    fetchControlPlaneStatus(service),
    fetchControlPlaneDataSources(service),
    fetchControlPlaneRules(service),
  ])

  return {
    note: 'No dedicated config endpoint is exposed by the backend for this service.',
    basedOn: ['status', 'datasources', 'rules'],
    status,
    dataSources,
    rules,
  }
}

export async function fetchControlPlaneDataSources() {
  return fetchJson('/control-panel/datasources')
}

export async function fetchControlPlaneRules() {
  return fetchJson('/control-panel/rules')
}

export async function fetchSparkQueries() {
  return fetchJson('/control-panel/queries')
}

export async function fetchControlPlaneStreamMetrics() {
  return fetchJson('/control-panel/streams/metrics')
}

export async function runControlPlaneAction(service, action) {
  return fetchJson(`/control-panel/${service}/${action}`, {
    method: 'POST',
  })
}

export async function runSparkQueryAction(dataSource, action) {
  return fetchJson(`/control-panel/queries/${encodeURIComponent(dataSource)}/${action}`, {
    method: 'POST',
  })
}

export async function restartSparkQueriesAction() {
  return fetchJson('/control-panel/queries/restart', {
    method: 'POST',
  })
}
