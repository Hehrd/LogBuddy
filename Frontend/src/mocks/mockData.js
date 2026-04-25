export const mockConfigDrafts = {
  ds: `{
  "dataSources": {
    "app-logs": {
      "name": "app-logs",
      "pathInfo": {
        "location": "/var/log/app.log",
        "platform": "FILE",
        "options": {
          "maxFilesPerTrigger": "10"
        }
      },
      "logFormat": {
        "logType": "LOGFMT",
        "keyValuePairRegex": "(\\\\w+)=([^ ]+)",
        "fullLogEntryRegex": ".*",
        "logEntryStartRegex": "^\\\\d{4}-\\\\d{2}-\\\\d{2}",
        "defaultFields": {
          "timestamp": "ts",
          "timestampFormat": "yyyy-MM-dd HH:mm:ss",
          "level": "level",
          "message": "msg",
          "source": "service",
          "data": "payload",
          "logger": "logger"
        },
        "customFields": {
          "userId": "user_id",
          "ip": "client_ip"
        }
      },
      "globalRequiredRules": [
        "log_format_check"
      ],
      "traceRequiredRules": [
        "duplicate_event_check"
      ],
      "globalAlertConditions": {
        "security-alert": {
          "alertName": "security-alert",
          "requiredRules": [
            "log_format_check",
            "suspicious-admin-access"
          ],
          "timeWindowMillis": 300000,
          "alertEndpoints": [
            "http://alerts.internal/security"
          ],
          "alertConditionType": "GLOBAL",
          "aiOverviewEnabled": true
        }
      },
      "traceAlertConditions": {},
      "schedule": {
        "delayAfterStartUpMillis": 10000,
        "intervalsMillis": [
          60000,
          300000
        ]
      }
    }
  }
}`,
  rule: `{
  "rules": {
    "suspicious-admin-access": {
      "ruleName": "suspicious-admin-access",
      "checks": [
        {
          "type": "string_value_check"
        },
        {
          "type": "regex_match_value_check"
        }
      ],
      "logTargetCount": 2,
      "maxCompletionsPerAlert": 10
    },
    "log_format_check": {
      "ruleName": "log_format_check",
      "checks": [
        {
          "type": "timestamp_check"
        }
      ],
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 100
    }
  }
}`,
  app: `{
  "controlPanelServerPort": 8080,
  "grpcSettings": {
    "serverHost": "localhost",
    "serverPort": 9090,
    "maxLinesPerReq": 1000
  }
}`,
}

export const mockAlerts = [
  {
    alertId: 'alert-8f3c2a11',
    alertName: 'Multiple Failed Logins Detected',
    alertType: 'ALL_RULES_MATCHED',
    dataSourceName: 'auth-service-prod',
    traceId: 'trace-91ab47de',
    triggeredAt: '2026-04-25T15:10:45Z',
    firstMatchedAt: '2026-04-25T15:08:10Z',
    lastMatchedAt: '2026-04-25T15:10:40Z',
    timeWindowMillis: 300000,
    requiredRules: [
      'FailedLoginThreshold',
      'RepeatedIpAttempts',
    ],
    completions: [
      {
        ruleName: 'FailedLoginThreshold',
        timestamp: '2026-04-25T15:09:30Z',
        logs: [
          'User admin failed login from 192.168.1.15',
          'User admin failed login from 192.168.1.15',
          'User admin failed login from 192.168.1.15',
        ],
      },
      {
        ruleName: 'RepeatedIpAttempts',
        timestamp: '2026-04-25T15:10:40Z',
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
]
