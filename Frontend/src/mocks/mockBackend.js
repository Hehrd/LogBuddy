const mockConfig = {
  ds: {
    dataSources: {
      app_json_source: {
        name: 'app_json_source',
        pathInfo: {
          location: 'hdfs://localhost:9000/test',
          platform: 'FILE_TEXT',
          options: {},
        },
        logFormat: {
          logType: 'JSON',
          defaultFields: {
            timestamp: 'ts',
            timestampFormat: "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            level: 'lvl',
            message: 'msg',
            source: 'src',
            data: 'data',
            logger: 'logger',
          },
          customFields: {},
        },
        requiredRules: ['error_regex_rule', 'warn_level_rule'],
        alertData: {
          alert_data_one: {
            alertName: 'alert_data_one',
            requiredRules: ['error_regex_rule'],
            timeWindowMillis: 300000,
            alertEndpoints: ['slack://ops-alerts'],
          },
          alert_data_two: {
            alertName: 'alert_data_two',
            requiredRules: ['warn_level_rule'],
            timeWindowMillis: 600000,
            alertEndpoints: ['email://oncall@example.com'],
          },
        },
        schedule: {
          delayAfterStartUpMillis: 5000,
        },
      },
    },
  },
  rule: {
    rules: {
      error_regex_rule: {
        ruleName: 'error_regex_rule',
        check: {
          type: 'data_regex_match_check',
          metricName: 'error_pattern_count',
          pattern: '15',
        },
        logTargetCount: 1,
        maxCompletionsPerAlert: 2,
      },
      warn_level_rule: {
        ruleName: 'warn_level_rule',
        check: {
          type: 'log_level_check',
          metricName: 'warn_level_count',
          level: 'WARN',
        },
        logTargetCount: 1,
        maxCompletionsPerAlert: 3,
      },
      message_length_rule: {
        ruleName: 'message_length_rule',
        check: {
          type: 'message_length_check',
          metricName: 'long_message_count',
          shorterThan: 200,
          longerThan: 10,
        },
        logTargetCount: 1,
        maxCompletionsPerAlert: 1,
      },
    },
  },
  app: {
    serverPort: 6969,
    grpcSettings: {
      serverHost: 'localhost',
      serverPort: 9090,
      maxLinesPerReq: 1000,
    },
  },
}

const mockHealth = {
  status: 'UP',
  service: 'DataProcessing',
  version: 'mock',
  websocket: {
    endpoint: '/ws/alerts',
    topic: '/topic/alerts',
  },
  checkedAt: new Date().toISOString(),
}

function jsonResponse(body, init = {}) {
  return new Response(JSON.stringify(body), {
    status: init.status ?? 200,
    headers: {
      'Content-Type': 'application/json',
      ...init.headers,
    },
  })
}

function getRequestPath(input) {
  const rawUrl = typeof input === 'string' ? input : input.url
  return new URL(rawUrl, window.location.origin).pathname
}

export function installMockBackend() {
  if (window.__logBuddyMockBackendInstalled) {
    return
  }

  const realFetch = window.fetch.bind(window)

  window.fetch = async (input, init) => {
    const path = getRequestPath(input)

    if (path === '/api/config') {
      return jsonResponse(mockConfig)
    }

    if (path === '/health') {
      return jsonResponse({
        ...mockHealth,
        checkedAt: new Date().toISOString(),
      })
    }

    return realFetch(input, init)
  }

  window.__logBuddyMockBackendInstalled = true
}
