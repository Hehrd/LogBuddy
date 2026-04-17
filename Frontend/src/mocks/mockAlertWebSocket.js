const mockAlerts = [
  {
    id: 'mock-alert-001',
    alertName: 'alert_data_one',
    sourceName: 'app_json_source',
    data: [
      {
        ruleName: 'error_regex_rule',
        logs: [
          '2026-04-17T12:01:08Z ERROR Payment API returned code 500',
          '2026-04-17T12:01:09Z ERROR Payment API timeout after 30s',
        ],
      },
    ],
  },
  {
    id: 'mock-alert-002',
    alertName: 'alert_data_two',
    sourceName: 'app_logfmt_source',
    data: [
      {
        ruleName: 'warn_level_rule',
        logs: [
          'ts=2026-04-17T12:02:11Z level=WARN msg="worker queue is growing"',
          'ts=2026-04-17T12:02:12Z level=WARN msg="retry budget is almost exhausted"',
        ],
      },
    ],
  },
  {
    id: 'mock-alert-003',
    alertName: 'multi_rule_alert',
    sourceName: 'app_json_source',
    data: [
      {
        ruleName: 'error_regex_rule',
        logs: ['2026-04-17T12:03:14Z ERROR parser failed on input batch'],
      },
      {
        ruleName: 'message_length_rule',
        logs: ['2026-04-17T12:03:15Z WARN oversized ingest payload detected'],
      },
    ],
  },
]

export class MockAlertWebSocket extends EventTarget {
  static CONNECTING = 0
  static OPEN = 1
  static CLOSING = 2
  static CLOSED = 3

  constructor(url) {
    super()
    this.url = url
    this.readyState = MockAlertWebSocket.CONNECTING
    this.sentMessages = []
    this.alertIndex = 0

    this.openTimer = window.setTimeout(() => {
      this.readyState = MockAlertWebSocket.OPEN
      this.dispatchSocketEvent('open')
      this.startAlertLoop()
    }, 350)
  }

  send(message) {
    this.sentMessages.push(message)

    if (message.includes('ping')) {
      this.emitAlert({
        id: `mock-alert-pong-${Date.now()}`,
        sourceName: 'mock_server',
        alertName: 'connection_health',
        data: [
          {
            ruleName: 'heartbeat',
            timestamp: new Date().toISOString(),
            logs: ['Mock alert socket received a heartbeat from the client.'],
          },
        ],
        timestamp: new Date().toISOString(),
      })
    }
  }

  close() {
    if (this.readyState === MockAlertWebSocket.CLOSED) {
      return
    }

    this.readyState = MockAlertWebSocket.CLOSING
    window.clearTimeout(this.openTimer)
    window.clearInterval(this.alertTimer)
    this.readyState = MockAlertWebSocket.CLOSED
    this.dispatchSocketEvent('close')
  }

  startAlertLoop() {
    this.emitNextAlert()
    this.alertTimer = window.setInterval(() => this.emitNextAlert(), 5000)
  }

  emitNextAlert() {
    const template = mockAlerts[this.alertIndex % mockAlerts.length]
    this.alertIndex += 1

    this.emitAlert({
      ...template,
      id: `${template.id}-${Date.now()}`,
      timestamp: new Date().toISOString(),
      data: template.data.map((completion) => ({
        ...completion,
        timestamp: new Date().toISOString(),
      })),
    })
  }

  emitAlert(alert) {
    if (this.readyState !== MockAlertWebSocket.OPEN) {
      return
    }

    const event = new MessageEvent('message', {
      data: JSON.stringify(alert),
    })
    this.dispatchEvent(event)
    this.onmessage?.(event)
  }

  dispatchSocketEvent(type) {
    const event = new Event(type)
    this.dispatchEvent(event)
    this[`on${type}`]?.(event)
  }
}
