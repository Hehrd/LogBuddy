import { Client } from '@stomp/stompjs'
import { isMockMode, resolveBackendWebSocketUrl } from '../config/runtime.js'
import { mockAlerts } from '../mocks/mockData.js'
import { MockAlertWebSocket } from '../mocks/mockAlertWebSocket.js'

export async function loadAlertHistory() {
  if (!isMockMode) {
    return []
  }
  return mockAlerts
}

export function createAlertsConnection({ onAlert, onStateChange, onError }) {
  if (isMockMode) {
    let socket = null

    return {
      async connect() {
        onError('')
        onStateChange('Connecting')

        const alerts = await loadAlertHistory()
        alerts.slice().reverse().forEach((alert, index) => {
          window.setTimeout(() => onAlert(alert), index * 1200)
        })

        socket = new MockAlertWebSocket('ws://mock.local/ws/alerts')
        socket.addEventListener('open', () => onStateChange('Connected'))
        socket.addEventListener('close', () => onStateChange('Disconnected'))
        socket.addEventListener('error', () => {
          onStateChange('Error')
          onError('Mock alerts websocket failed.')
        })
        socket.addEventListener('message', (event) => {
          onAlert(parseAlert(event.data))
        })
      },
      disconnect() {
        socket?.close()
        socket = null
        onStateChange('Disconnected')
      },
    }
  }

  let stompClient = null

  return {
    async connect() {
      onError('')
      onStateChange('Connecting')

      stompClient = new Client({
        brokerURL: import.meta.env.VITE_ALERTS_WS_URL ?? resolveBackendWebSocketUrl('/ws/alerts'),
        reconnectDelay: 5000,
        debug: (message) => console.debug('[stomp]', message),
        onConnect: () => {
          console.info('[stomp] connected to alerts broker')
          onStateChange('Connected')
          stompClient.subscribe('/topic/alerts', (message) => {
            console.info('[stomp] message received', {
              destination: '/topic/alerts',
              headers: message.headers,
              body: message.body,
            })
            onAlert(parseAlert(message.body))
          })
        },
        onWebSocketClose: () => {
          console.warn('[stomp] websocket closed')
          onStateChange('Disconnected')
        },
        onStompError: (frame) => {
          console.error('[stomp] broker error', { headers: frame.headers, body: frame.body })
          onStateChange('Error')
          onError(frame.headers?.message ?? 'STOMP broker error')
        },
        onWebSocketError: (event) => {
          console.error('[stomp] websocket transport error', event)
          onStateChange('Error')
          onError('Could not connect to the alerts websocket.')
        },
      })

      stompClient.activate()
    },
    disconnect() {
      if (stompClient) {
        stompClient.deactivate()
        stompClient = null
      }
    },
  }
}

function parseAlert(body) {
  try {
    return JSON.parse(body)
  } catch {
    return {
      timestamp: new Date().toISOString(),
      data: [],
      raw: body,
    }
  }
}
