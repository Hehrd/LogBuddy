const mockAlerts = [
  {
    alertId: 'alert-8f3c2a11',
    alertName: 'Multiple Failed Logins Detected',
    alertType: 'ALL_RULES_MATCHED',
    dataSourceName: 'auth-service-prod',
    traceId: 'trace-91ab47de',
    timeWindowMillis: 300000,
    requiredRules: [
      'FailedLoginThreshold',
      'RepeatedIpAttempts',
    ],
    completions: [
      {
        ruleName: 'FailedLoginThreshold',
        logs: [
          'User admin failed login from 192.168.1.15',
          'User admin failed login from 192.168.1.15',
          'User admin failed login from 192.168.1.15',
        ],
      },
      {
        ruleName: 'RepeatedIpAttempts',
        logs: [
          'IP 192.168.1.15 attempted login for user admin',
          'IP 192.168.1.15 attempted login for user root',
        ],
      },
    ],
    sampleLogs: [
      '2026-04-25T15:08:10Z WARN auth-service Failed login for admin from 192.168.1.15',
      '2026-04-25T15:09:30Z WARN auth-service Failed login for admin from 192.168.1.15',
      '2026-04-25T15:10:40Z WARN auth-service Failed login for root from 192.168.1.15',
    ],
    aiOverviewEnabled: true,
  },
  {
    alertId: 'alert-1ac4be02',
    alertName: 'Admin Access Pattern',
    alertType: 'ALL_RULES_MATCHED',
    dataSourceName: 'audit-logs',
    traceId: 'trace-6ad1c892',
    timeWindowMillis: 300000,
    requiredRules: [
      'suspicious-admin-access',
      'log_format_check',
    ],
    completions: [
      {
        ruleName: 'suspicious-admin-access',
        logs: [
          'admin access from 203.0.113.7',
          'admin access from 203.0.113.7',
        ],
      },
      {
        ruleName: 'log_format_check',
        logs: [
          'timestamp format mismatch on audit event',
        ],
      },
    ],
    sampleLogs: [
      '2026-04-25T16:01:11Z WARN audit-service admin access from 203.0.113.7',
      '2026-04-25T16:02:19Z ERROR audit-service malformed timestamp',
    ],
    aiOverviewEnabled: false,
  },
  {
    alertId: 'alert-54bf7343',
    alertName: 'Credential Abuse Cluster',
    alertType: 'ALL_RULES_MATCHED',
    dataSourceName: 'auth-service-prod',
    traceId: 'trace-0244ff31',
    timeWindowMillis: 300000,
    requiredRules: [
      'FailedLoginThreshold',
      'RepeatedIpAttempts',
    ],
    completions: [
      {
        ruleName: 'FailedLoginThreshold',
        logs: [
          'User root failed login from 192.168.1.44',
          'User root failed login from 192.168.1.44',
        ],
      },
      {
        ruleName: 'RepeatedIpAttempts',
        logs: [
          'IP 192.168.1.44 attempted login for user root',
          'IP 192.168.1.44 attempted login for user deploy',
        ],
      },
    ],
    sampleLogs: [
      '2026-04-25T17:03:14Z WARN auth-service Failed login for root from 192.168.1.44',
      '2026-04-25T17:04:15Z WARN auth-service Failed login for deploy from 192.168.1.44',
    ],
    aiOverviewEnabled: true,
  },
]

function enrichAlert(template) {
  const now = new Date().toISOString()
  return {
    ...template,
    triggeredAt: now,
    firstMatchedAt: template.firstMatchedAt ?? now,
    lastMatchedAt: template.lastMatchedAt ?? now,
    completions: template.completions.map((completion) => ({
      ...completion,
      timestamp: completion.timestamp ?? now,
    })),
  }
}

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
        alertId: `mock-alert-pong-${Date.now()}`,
        alertName: 'connection_health',
        alertType: 'HEARTBEAT',
        dataSourceName: 'mock_server',
        traceId: `trace-${Date.now()}`,
        timeWindowMillis: 60000,
        requiredRules: ['heartbeat'],
        completions: [
          {
            ruleName: 'heartbeat',
            timestamp: new Date().toISOString(),
            logs: ['Mock alert socket received a heartbeat from the client.'],
          },
        ],
        sampleLogs: ['Mock alert socket received a heartbeat from the client.'],
        aiOverviewEnabled: false,
        triggeredAt: new Date().toISOString(),
        firstMatchedAt: new Date().toISOString(),
        lastMatchedAt: new Date().toISOString(),
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

    this.emitAlert(enrichAlert(template))
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
