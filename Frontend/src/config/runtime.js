export const apiMode = import.meta.env.VITE_API_MODE === 'mock' ? 'mock' : 'real'
export const isMockMode = apiMode === 'mock'

export function resolveBackendUrl(path) {
  const configuredBase = import.meta.env.VITE_BACKEND_BASE_URL
  const baseUrl = configuredBase ? new URL(configuredBase, window.location.origin) : new URL(window.location.origin)
  return new URL(path, baseUrl).toString()
}

export function resolveBackendWebSocketUrl(path) {
  const url = new URL(resolveBackendUrl(path))
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  return url.toString()
}
