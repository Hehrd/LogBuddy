export function normalizeAlert(rawAlert) {
  if (Array.isArray(rawAlert.data)) {
    const rules = rawAlert.data
      .filter(Boolean)
      .map((completion) => ({
        name: completion.ruleName,
        timestamp: completion.timestamp,
        logs: completion.logs ?? [],
      }))

    return {
      id: rawAlert.id ?? `alert-${rawAlert.timestamp ?? Date.now()}`,
      severity: rules.some((rule) => rule.name?.toLowerCase().includes('error')) ? 'critical' : 'warning',
      alertName: rawAlert.alertName ?? 'Backend alert',
      sourceName: rawAlert.sourceName ?? 'DataProcessing',
      ruleNames: rules.map((rule) => rule.name).filter(Boolean),
      message: `${rules.length} rule${rules.length === 1 ? '' : 's'} completed for this alert.`,
      matchedCount: rules.reduce((total, rule) => total + rule.logs.length, 0),
      occurredAt: rawAlert.timestamp ?? new Date().toISOString(),
      rawAlert,
    }
  }

  return {
    ...rawAlert,
    ruleNames: rawAlert.ruleNames ?? [rawAlert.ruleName].filter(Boolean),
    occurredAt: rawAlert.occurredAt ?? rawAlert.timestamp ?? new Date().toISOString(),
    matchedCount: rawAlert.matchedCount ?? rawAlert.logs?.length ?? 0,
    rawAlert,
  }
}
