const DEFAULT_ALERTS_PATH = '/alerts'

export function getDefaultAlertsSocketUrl(serverPort) {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = serverPort ? `${window.location.hostname}:${serverPort}` : window.location.host
  return `${protocol}//${host}${DEFAULT_ALERTS_PATH}`
}

export function createAlertsSocket({
  url,
  WebSocketImpl = window.WebSocket,
  onAlert,
  onStatusChange,
  onError,
}) {
  if (!WebSocketImpl) {
    throw new Error('WebSocket is not available in this browser.')
  }

  const socket = new WebSocketImpl(url)

  const setStatus = (status) => {
    onStatusChange?.(status)
  }

  socket.addEventListener('open', () => setStatus('connected'))

  socket.addEventListener('close', () => setStatus('closed'))

  socket.addEventListener('error', (event) => {
    setStatus('error')
    onError?.(event)
  })

  socket.addEventListener('message', (event) => {
    try {
      onAlert?.(JSON.parse(event.data))
    } catch (error) {
      onError?.(error)
    }
  })

  setStatus('connecting')

  return {
    send: (message) => {
      if (socket.readyState === WebSocketImpl.OPEN) {
        socket.send(JSON.stringify(message))
      }
    },
    close: () => socket.close(),
  }
}
