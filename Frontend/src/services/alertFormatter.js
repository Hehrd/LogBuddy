export function normalizeAlert(rawAlert) {
  const completions = Array.isArray(rawAlert.completions)
    ? rawAlert.completions
    : Array.isArray(rawAlert.data)
      ? rawAlert.data
      : []

  if (completions.length) {
    const rules = completions
      .filter(Boolean)
      .map((completion) => ({
        name: completion.ruleName,
        timestamp: completion.timestamp,
        logs: completion.logs ?? [],
      }))

    return {
      id: rawAlert.alertId ?? rawAlert.id ?? `alert-${rawAlert.triggeredAt ?? rawAlert.timestamp ?? Date.now()}`,
      severity: rules.some((rule) => rule.name?.toLowerCase().includes('error')) ? 'critical' : 'warning',
      alertName: rawAlert.alertName ?? 'Backend alert',
      alertType: rawAlert.alertType ?? 'UNKNOWN',
      sourceName: rawAlert.dataSourceName ?? rawAlert.sourceName ?? 'DataProcessing',
      traceId: rawAlert.traceId ?? '',
      ruleNames: rawAlert.requiredRules?.length ? rawAlert.requiredRules : rules.map((rule) => rule.name).filter(Boolean),
      message: `${rules.length} rule${rules.length === 1 ? '' : 's'} completed for this alert.`,
      matchedCount: rules.reduce((total, rule) => total + rule.logs.length, 0),
      occurredAt: rawAlert.triggeredAt ?? rawAlert.timestamp ?? new Date().toISOString(),
      firstMatchedAt: rawAlert.firstMatchedAt ?? '',
      lastMatchedAt: rawAlert.lastMatchedAt ?? '',
      timeWindowMillis: rawAlert.timeWindowMillis ?? null,
      aiOverviewEnabled: Boolean(rawAlert.aiOverviewEnabled),
      sampleLogs: rawAlert.sampleLogs ?? [],
      completions: rules,
      rawAlert,
    }
  }

  return {
    ...rawAlert,
    ruleNames: rawAlert.ruleNames ?? [rawAlert.ruleName].filter(Boolean),
    occurredAt: rawAlert.occurredAt ?? rawAlert.triggeredAt ?? rawAlert.timestamp ?? new Date().toISOString(),
    matchedCount: rawAlert.matchedCount ?? rawAlert.logs?.length ?? 0,
    rawAlert,
  }
}
