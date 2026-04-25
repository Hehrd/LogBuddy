export const apiMode = import.meta.env.VITE_API_MODE === 'mock' ? 'mock' : 'real'
export const isMockMode = apiMode === 'mock'

export function resolveBackendUrl(path) {
  const configuredBase = import.meta.env.VITE_BACKEND_BASE_URL
  const baseUrl = configuredBase ? new URL(configuredBase, window.location.origin) : new URL(window.location.origin)
  return new URL(path, baseUrl).toString()
}

export function resolveBackendWebSocketUrl(path) {
  const configuredWebSocketBase = import.meta.env.VITE_ALERTS_WS_URL
  if (configuredWebSocketBase) {
    return new URL(configuredWebSocketBase, window.location.origin).toString()
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = `${window.location.hostname}:6969`
  return new URL(path, `${protocol}//${host}`).toString()
}
