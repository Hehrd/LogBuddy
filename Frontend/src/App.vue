<script setup>
import { computed, reactive, ref, watch } from 'vue'
import AppHeader from './components/AppHeader.vue'
import ConfigInspector from './components/ConfigInspector.vue'
import ConfigNav from './components/ConfigNav.vue'
import ConfigStatusCards from './components/ConfigStatusCards.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import EditorToolbar from './components/EditorToolbar.vue'
import AppSettingsEditor from './components/index/AppSettingsEditor.vue'
import DataSourceEditor from './components/index/DataSourceEditor.vue'
import RuleEditor from './components/index/RuleEditor.vue'
const starterConfigs = {
  ds: `{
  "dataSources": {
    "app_json_source": {
      "name": "app_json_source",
      "pathInfo": {
        "location": "hdfs://localhost:9000/test",
        "platform": "FILE_TEXT",
        "options": {}
      },
      "logFormat": {
        "logType": "JSON",
        "defaultFields": {
          "timestamp": "ts",
          "timestampFormat": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
          "level": "lvl",
          "message": "msg",
          "source": "src",
          "data": "data",
          "logger": "logger"
        },
        "customFields": {}
      },
      "requiredRules": [
        "error_regex_rule",
        "warn_level_rule"
      ],
      "alertData": {
        "alert_data_one": {
          "alertName": "alert_data_one",
          "requiredRules": ["error_regex_rule"],
          "timeWindowMillis": 300000,
          "alertEndpoints": ["slack://ops-alerts"]
        },
        "alert_data_two": {
          "alertName": "alert_data_two",
          "requiredRules": ["warn_level_rule"],
          "timeWindowMillis": 600000,
          "alertEndpoints": ["email://oncall@example.com"]
        }
      },
      "schedule": {
        "delayAfterStartUpMillis": 5000
      }
    },
    "app_logfmt_source": {
      "name": "app_logfmt_source",
      "pathInfo": {
        "location": "hdfs://localhost:9000/test2",
        "platform": "FILE_TEXT",
        "options": {}
      },
      "logFormat": {
        "logType": "LOGFMT",
        "defaultFields": {
          "timestamp": "ts",
          "timestampFormat": "yyyy-MM-dd'T'HH:mm:ss.SSSX",
          "level": "lvl",
          "message": "msg",
          "source": "src",
          "data": "data",
          "logger": "logger"
        },
        "customFields": {}
      },
      "requiredRules": [
        "error_regex_rule",
        "warn_level_rule"
      ],
      "alertData": {
        "alert_data_one": {
          "alertName": "alert_data_one",
          "requiredRules": ["error_regex_rule"],
          "timeWindowMillis": 300000,
          "alertEndpoints": ["slack://ops-alerts"]
        },
        "alert_data_two": {
          "alertName": "alert_data_two",
          "requiredRules": ["warn_level_rule"],
          "timeWindowMillis": 600000,
          "alertEndpoints": ["email://oncall@example.com"]
        }
      },
      "schedule": {
        "delayAfterStartUpMillis": 5000
      }
    }
  }
}`,
  rule: `{
  "rules": {
    "error_regex_rule": {
      "ruleName": "error_regex_rule",
      "check": {
        "type": "data_regex_match_check",
        "metricName": "error_pattern_count",
        "pattern": "15"
      },
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 2
    },
    "warn_level_rule": {
      "ruleName": "warn_level_rule",
      "check": {
        "type": "log_level_check",
        "metricName": "warn_level_count",
        "level": "WARN"
      },
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 3
    },
    "message_length_rule": {
      "ruleName": "message_length_rule",
      "check": {
        "type": "message_length_check",
        "metricName": "long_message_count",
        "shorterThan": 200,
        "longerThan": 10
      },
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 1
    }
  }
}`,
  app: `{
  "serverPort": 6969,
  "grpcSettings": {
    "serverHost": "localhost",
    "serverPort": 9090,
    "maxLinesPerReq": 1000
  }
}`,
}

const emptyConfigs = {
  ds: `{
  "dataSources": {}
}`,
  rule: `{
  "rules": {}
}`,
  app: `{
  "serverPort": 0,
  "grpcSettings": {
    "serverHost": "",
    "serverPort": 0,
    "maxLinesPerReq": 0
  }
}`,
}

const checkTypeOptions = [
  'data_regex_match_check',
  'timestamp_check',
  'log_level_check',
  'message_length_check',
]

const platformOptions = [
  'KAFKA',
  'PULSAR',
  'FILE_TEXT',
  'DELTA',
  'ICEBERG',
  'HUDI',
  'SOCKET',
  'RATE',
]

const logTypeOptions = ['JSON', 'LOGFMT', 'CUSTOM']
const storageKey = 'logbuddy.configDrafts.v1'

const fieldHelp = {
  sourceName: 'Unique data source name. Rules and sessions refer to this source by name.',
  platform: 'Where logs come from: streams, files, lakehouse tables, or testing utilities.',
  location: 'Location of the cloud storage for this data source.',
  options: 'Connection options for Apache spark',
  logType: 'Parser type for incoming log records: JSON, LOGFMT, or CUSTOM.',
  requiredRules: 'Rules are created in the Rules tab. Select them on alerts to decide which checks run against this data source.',
  startupDelay: 'Milliseconds to wait after startup before this source begins processing.',
  alertName: 'Unique alert group name under this data source.',
  windowMillis: 'Time window in milliseconds for grouping rule matches into this alert.',
  alertEndpoints: 'Endpoint to be notified when there is an alert.',
  ruleName: 'Unique rule name. Data sources and alerts attach to this name.',
  checkType: 'Backend check implementation type for this rule.',
  metricName: 'Metric key emitted for matches from this rule.',
  pattern: 'Regex or value pattern used by data_regex_match_check.',
  level: 'Log level to match for log_level_check, such as WARN or ERROR.',
  shorterThan: 'Upper message length bound for message_length_check.',
  longerThan: 'Lower message length bound for message_length_check.',
  logTargetCount: 'Number of matching log entries required before the rule completes.',
  maxCompletions: 'Maximum rule completions allowed per alert session.',
  serverPort: 'HTTP server port used by the app.',
  grpcHost: 'Hostname for the gRPC connection.',
  grpcPort: 'gRPC port used for ingest communication.',
  maxLinesPerReq: 'Maximum log lines sent per gRPC request.',
}

const configMeta = {
  ds: {
    label: 'Data sources',
    rootKey: 'dataSources',
    description: 'Define source paths, log formats, schedules, and alert bundles for each stream.',
    filename: 'ds.conf',
  },
  rule: {
    label: 'Rules',
    rootKey: 'rules',
    description: 'Describe checks, metrics, completion thresholds, and matching behavior.',
    filename: 'rule.conf',
  },
  app: {
    label: 'App settings',
    rootKey: null,
    description: 'Keep runtime port and gRPC settings in one small config.',
    filename: 'app.conf',
  },
}

const activeConfig = ref('ds')
const editorMode = ref('form')
const linuxConfigPathAvailable = ref(false)
const restoredDrafts = loadSavedDrafts()
const confirmDialog = reactive({
  open: false,
  title: '',
  message: '',
  confirmLabel: '',
  action: null,
})
const contextMenu = reactive({
  open: false,
  type: null,
  key: null,
  x: 0,
  y: 0,
})
const editorState = reactive(
  Object.fromEntries(
    Object.entries(starterConfigs).map(([key, value]) => [
      key,
      {
        text: restoredDrafts?.[key]?.text ?? emptyConfigs[key],
        lastImportedName: restoredDrafts?.[key]?.lastImportedName ?? null,
      },
    ]),
  ),
)

const selectedKeys = reactive({
  dsSource: 'app_json_source',
  dsAlert: 'alert_data_one',
  rule: 'error_regex_rule',
})

function safeParse(text) {
  try {
    return { value: JSON.parse(text), error: null }
  } catch (error) {
    return { value: null, error }
  }
}

const parsedConfigs = computed(() =>
  Object.fromEntries(
    Object.entries(editorState).map(([key, state]) => [key, safeParse(state.text)]),
  ),
)

const activeText = computed({
  get: () => editorState[activeConfig.value].text,
  set: (value) => {
    editorState[activeConfig.value].text = value
  },
})

const activeParsed = computed(() => parsedConfigs.value[activeConfig.value])
const activeMeta = computed(() => configMeta[activeConfig.value])

const activeJsonValue = computed(() => activeParsed.value.value)

const configStats = computed(() => {
  return Object.entries(parsedConfigs.value).map(([key, parsed]) => {
    const meta = configMeta[key]
    const json = parsed.value
    const collection = meta.rootKey && json ? json[meta.rootKey] : json
    const itemCount =
      collection && typeof collection === 'object' && !Array.isArray(collection)
        ? Object.keys(collection).length
        : parsed.value
          ? 1
          : 0

    return {
      key,
      label: meta.label,
      valid: !parsed.error,
      itemCount,
    }
  })
})

const activeSummary = computed(() => {
  const parsed = activeParsed.value
  if (parsed.error || !parsed.value) {
    return []
  }

  if (activeConfig.value === 'ds') {
    return Object.values(parsed.value.dataSources ?? {}).map((source) => ({
      title: source.name,
      subtitle: `${source.logFormat?.logType ?? 'Unknown'} via ${source.pathInfo?.platform ?? 'Unknown'}`,
      detail: `${Object.keys(source.alertData ?? {}).length} alerts, ${source.requiredRules?.length ?? 0} required rules`,
    }))
  }

  if (activeConfig.value === 'rule') {
    return Object.values(parsed.value.rules ?? {}).map((rule) => ({
      title: rule.ruleName,
      subtitle: rule.check?.type ?? 'Unknown check',
      detail: `Target ${rule.logTargetCount ?? 0}, max completions ${rule.maxCompletionsPerAlert ?? 0}`,
    }))
  }

  return [
    {
      title: `HTTP ${parsed.value.serverPort ?? 'Unknown'}`,
      subtitle: `gRPC ${parsed.value.grpcSettings?.serverHost ?? 'Unknown'}:${parsed.value.grpcSettings?.serverPort ?? 'Unknown'}`,
      detail: `Max lines ${parsed.value.grpcSettings?.maxLinesPerReq ?? 'Unknown'}`,
    },
  ]
})

const activeError = computed(() => {
  const error = activeParsed.value.error
  if (!error) {
    return null
  }

  const message = error.message ?? 'Invalid JSON'
  const positionMatch = message.match(/position\s+(\d+)/i)
  return {
    message,
    position: positionMatch ? Number(positionMatch[1]) : null,
  }
})

const dsSources = computed(() => {
  return Object.entries(activeJsonValue.value?.dataSources ?? {})
})

const activeDsSourceKey = computed(() => {
  const entries = dsSources.value
  if (!entries.length) {
    return null
  }
  if (!entries.some(([key]) => key === selectedKeys.dsSource)) {
    selectedKeys.dsSource = entries[0][0]
  }
  return selectedKeys.dsSource
})

const activeDsSource = computed(() => {
  const key = activeDsSourceKey.value
  return key ? activeJsonValue.value.dataSources[key] : null
})

const activeAlerts = computed(() => {
  return Object.entries(activeDsSource.value?.alertData ?? {})
})

const activeDsAlertKey = computed(() => {
  const entries = activeAlerts.value
  if (!entries.length) {
    return null
  }
  if (!entries.some(([key]) => key === selectedKeys.dsAlert)) {
    selectedKeys.dsAlert = entries[0][0]
  }
  return selectedKeys.dsAlert
})

const activeDsAlert = computed(() => {
  const key = activeDsAlertKey.value
  return key ? activeDsSource.value.alertData[key] : null
})

const rulesList = computed(() => Object.entries(activeJsonValue.value?.rules ?? {}))
const configuredRules = computed(() => Object.entries(parsedConfigs.value.rule.value?.rules ?? {}))

const activeRuleKey = computed(() => {
  const entries = rulesList.value
  if (!entries.length) {
    return null
  }
  if (!entries.some(([key]) => key === selectedKeys.rule)) {
    selectedKeys.rule = entries[0][0]
  }
  return selectedKeys.rule
})

const activeRule = computed(() => {
  const key = activeRuleKey.value
  return key ? activeJsonValue.value.rules[key] : null
})

function setActiveConfig(key) {
  activeConfig.value = key
}

function setEditorMode(mode) {
  editorMode.value = mode
}

function openContextMenu(event, type, key) {
  event.preventDefault()
  contextMenu.open = true
  contextMenu.type = type
  contextMenu.key = key
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
}

function closeContextMenu() {
  contextMenu.open = false
  contextMenu.type = null
  contextMenu.key = null
}

function formatActiveJson() {
  if (!activeParsed.value.value) {
    return
  }
  activeText.value = JSON.stringify(activeParsed.value.value, null, 2)
}

function minifyActiveJson() {
  if (!activeParsed.value.value) {
    return
  }
  activeText.value = JSON.stringify(activeParsed.value.value)
}

function resetActiveJson() {
  editorState[activeConfig.value].text = emptyConfigs[activeConfig.value]
  editorState[activeConfig.value].lastImportedName = null
}

async function importJson(event, key) {
  const [file] = event.target.files ?? []
  if (!file) {
    return
  }

  const text = await file.text()
  editorState[key].text = text
  editorState[key].lastImportedName = file.name
  event.target.value = ''
}

function updateActiveJson(mutator) {
  updateConfigJson(activeConfig.value, mutator)
}

function updateConfigJson(configKey, mutator) {
  const parsed = parsedConfigs.value[configKey].value
  if (!parsed) {
    return
  }

  const nextValue = JSON.parse(JSON.stringify(parsed))
  mutator(nextValue)
  editorState[configKey].text = JSON.stringify(nextValue, null, 2)
}

function updateAppField(field, value) {
  updateActiveJson((draft) => {
    if (field.startsWith('grpcSettings.')) {
      const grpcField = field.replace('grpcSettings.', '')
      draft.grpcSettings[grpcField] = value
      return
    }
    draft[field] = value
  })
}

function updateDsSourceField(field, value) {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateActiveJson((draft) => {
    const source = draft.dataSources[sourceKey]
    if (field === 'name') source.name = value
    if (field === 'location') source.pathInfo.location = value
    if (field === 'platform') source.pathInfo.platform = value
    if (field === 'logType') source.logFormat.logType = value
    if (field === 'requiredRules') source.requiredRules = csvToList(value)
    if (field === 'delayAfterStartUpMillis') source.schedule.delayAfterStartUpMillis = toNumber(value)
  })
}

function updateDataSourceOption(optionKey, nextKey, nextValue) {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    const options = { ...(source.pathInfo.options ?? {}) }
    delete options[optionKey]
    if (nextKey.trim()) {
      options[nextKey.trim()] = nextValue
    }
    source.pathInfo.options = options
  })
}

function addDataSourceOption() {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    const options = { ...(source.pathInfo.options ?? {}) }
    options[createUniqueKey(options, 'option')] = ''
    source.pathInfo.options = options
  })
}

function deleteDataSourceOption(optionKey) {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    const options = { ...(source.pathInfo.options ?? {}) }
    delete options[optionKey]
    source.pathInfo.options = options
  })
}

function updateAlertField(field, value) {
  const sourceKey = activeDsSourceKey.value
  const alertKey = activeDsAlertKey.value
  if (!sourceKey || !alertKey) {
    return
  }

  updateActiveJson((draft) => {
    const alert = draft.dataSources[sourceKey].alertData[alertKey]
    if (field === 'alertName') alert.alertName = value
    if (field === 'requiredRules') alert.requiredRules = csvToList(value)
    if (field === 'timeWindowMillis') alert.timeWindowMillis = toNumber(value)
    if (field === 'alertEndpoints') alert.alertEndpoints = csvToList(value)
  })
}

function toggleAlertRule(ruleName) {
  const sourceKey = activeDsSourceKey.value
  const alertKey = activeDsAlertKey.value
  if (!sourceKey || !alertKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const alert = draft.dataSources[sourceKey].alertData[alertKey]
    alert.requiredRules = toggleListValue(alert.requiredRules ?? [], ruleName)
    syncSourceRequiredRules(draft.dataSources[sourceKey])
  })
}

function updateRuleField(field, value) {
  const ruleKey = activeRuleKey.value
  if (!ruleKey) {
    return
  }

  updateActiveJson((draft) => {
    const rule = draft.rules[ruleKey]
    if (field === 'ruleName') rule.ruleName = value
    if (field === 'type') rule.check.type = value
    if (field === 'metricName') rule.check.metricName = value
    if (field === 'pattern') rule.check.pattern = value
    if (field === 'level') rule.check.level = value
    if (field === 'shorterThan') rule.check.shorterThan = toNumber(value)
    if (field === 'longerThan') rule.check.longerThan = toNumber(value)
    if (field === 'logTargetCount') rule.logTargetCount = toNumber(value)
    if (field === 'maxCompletionsPerAlert') rule.maxCompletionsPerAlert = toNumber(value)
  })
}

function addDataSource() {
  updateActiveJson((draft) => {
    const key = createUniqueKey(draft.dataSources, 'new_source')
    draft.dataSources[key] = {
      name: key,
      pathInfo: {
        location: '',
        platform: 'FILE_TEXT',
        options: {},
      },
      logFormat: {
        logType: 'JSON',
        defaultFields: {
          timestamp: '',
          timestampFormat: '',
          level: '',
          message: '',
          source: '',
          data: '',
          logger: '',
        },
        customFields: {},
      },
      requiredRules: [],
      alertData: {},
      schedule: {
        delayAfterStartUpMillis: 0,
      },
    }
    selectedKeys.dsSource = key
  })
}

function syncSourceRequiredRules(source) {
  const rules = new Set()
  for (const alert of Object.values(source.alertData ?? {})) {
    for (const rule of alert.requiredRules ?? []) {
      rules.add(rule)
    }
  }
  source.requiredRules = [...rules]
}

function duplicateDataSource(sourceKey) {
  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    if (!source) {
      return
    }

    const nextKey = createUniqueKey(draft.dataSources, `${sourceKey}_copy`)
    const nextSource = JSON.parse(JSON.stringify(source))
    nextSource.name = nextKey
    draft.dataSources[nextKey] = nextSource
    selectedKeys.dsSource = nextKey
  })
}

function deleteDataSource(sourceKey) {
  updateConfigJson('ds', (draft) => {
    delete draft.dataSources[sourceKey]
    const [nextKey] = Object.keys(draft.dataSources)
    selectedKeys.dsSource = nextKey ?? ''
  })
}

function addAlert() {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateActiveJson((draft) => {
    const alerts = draft.dataSources[sourceKey].alertData
    const key = createUniqueKey(alerts, 'new_alert')
    alerts[key] = {
      alertName: key,
      requiredRules: [],
      timeWindowMillis: 0,
      alertEndpoints: [],
    }
    selectedKeys.dsAlert = key
  })
}

function duplicateAlert(alertKey) {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    const alert = source.alertData[alertKey]
    if (!alert) {
      return
    }

    const nextKey = createUniqueKey(source.alertData, `${alertKey}_copy`)
    const nextAlert = JSON.parse(JSON.stringify(alert))
    nextAlert.alertName = nextKey
    source.alertData[nextKey] = nextAlert
    selectedKeys.dsAlert = nextKey
    syncSourceRequiredRules(source)
  })
}

function deleteAlert(alertKey) {
  const sourceKey = activeDsSourceKey.value
  if (!sourceKey) {
    return
  }

  updateConfigJson('ds', (draft) => {
    const source = draft.dataSources[sourceKey]
    delete source.alertData[alertKey]
    const [nextKey] = Object.keys(source.alertData)
    selectedKeys.dsAlert = nextKey ?? ''
    syncSourceRequiredRules(source)
  })
}

function addRule() {
  updateActiveJson((draft) => {
    const key = createUniqueKey(draft.rules, 'new_rule')
    draft.rules[key] = {
      ruleName: key,
      check: {
        type: 'data_regex_match_check',
        metricName: '',
        pattern: '',
      },
      logTargetCount: 1,
      maxCompletionsPerAlert: 1,
    }
    selectedKeys.rule = key
  })
}

function duplicateRule(ruleKey) {
  updateConfigJson('rule', (draft) => {
    const rule = draft.rules[ruleKey]
    if (!rule) {
      return
    }

    const nextKey = createUniqueKey(draft.rules, `${ruleKey}_copy`)
    const nextRule = JSON.parse(JSON.stringify(rule))
    nextRule.ruleName = nextKey
    draft.rules[nextKey] = nextRule
    selectedKeys.rule = nextKey
  })
}

function deleteRule(ruleKey) {
  updateConfigJson('rule', (draft) => {
    delete draft.rules[ruleKey]
    const [nextKey] = Object.keys(draft.rules)
    selectedKeys.rule = nextKey ?? ''
  })

  updateConfigJson('ds', (draft) => {
    for (const source of Object.values(draft.dataSources ?? {})) {
      source.requiredRules = (source.requiredRules ?? []).filter((rule) => rule !== ruleKey)
      for (const alert of Object.values(source.alertData ?? {})) {
        alert.requiredRules = (alert.requiredRules ?? []).filter((rule) => rule !== ruleKey)
      }
    }
  })
}

function handleContextMenuAction(action) {
  const { type, key } = contextMenu
  closeContextMenu()

  if (type === 'dataSource' && action === 'duplicate') duplicateDataSource(key)
  if (type === 'dataSource' && action === 'delete') requestDeleteDataSource(key)
  if (type === 'alert' && action === 'duplicate') duplicateAlert(key)
  if (type === 'alert' && action === 'delete') requestDeleteAlert(key)
  if (type === 'rule' && action === 'duplicate') duplicateRule(key)
  if (type === 'rule' && action === 'delete') requestDeleteRule(key)
}

function requestDeleteDataSource(sourceKey) {
  openConfirmDialog({
    title: 'Delete data source?',
    message: `This will remove "${sourceKey}" and all of its alert settings.`,
    confirmLabel: 'Delete source',
    action: () => deleteDataSource(sourceKey),
  })
}

function requestDeleteRule(ruleKey) {
  openConfirmDialog({
    title: 'Delete rule?',
    message: `This will remove "${ruleKey}" and detach it from every data source and alert.`,
    confirmLabel: 'Delete rule',
    action: () => deleteRule(ruleKey),
  })
}

function requestDeleteAlert(alertKey) {
  openConfirmDialog({
    title: 'Delete alert?',
    message: `This will remove "${alertKey}" from the selected data source.`,
    confirmLabel: 'Delete alert',
    action: () => deleteAlert(alertKey),
  })
}

function csvToList(value) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function csvToNumberList(value) {
  return csvToList(value).map((item) => Number(item)).filter((item) => !Number.isNaN(item))
}

function toggleListValue(list, value) {
  return list.includes(value)
    ? list.filter((item) => item !== value)
    : [...list, value]
}

function toNumber(value) {
  const parsed = Number(value)
  return Number.isNaN(parsed) ? 0 : parsed
}

function createUniqueKey(collection, base) {
  if (!collection[base]) {
    return base
  }

  let index = 2
  while (collection[`${base}_${index}`]) {
    index += 1
  }
  return `${base}_${index}`
}

function loadTemplate(key) {
  editorState[key].text = starterConfigs[key]
  editorState[key].lastImportedName = `${configMeta[key].filename} template`
}

function downloadActiveConfig() {
  for (const key of Object.keys(configMeta)) {
    downloadConfig(key)
  }
  clearSavedDrafts()
}

function downloadConfig(key) {
  const blob = new Blob([editorState[key].text], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = configMeta[key].filename
  link.click()
  URL.revokeObjectURL(url)
}

function loadSavedDrafts() {
  try {
    const saved = localStorage.getItem(storageKey)
    return saved ? JSON.parse(saved) : null
  } catch {
    return null
  }
}

function saveDrafts() {
  const drafts = Object.fromEntries(
    Object.entries(editorState).map(([key, value]) => [
      key,
      {
        text: value.text,
        lastImportedName: value.lastImportedName,
      },
    ]),
  )
  localStorage.setItem(storageKey, JSON.stringify(drafts))
}

function clearSavedDrafts() {
  localStorage.removeItem(storageKey)
}

watch(editorState, saveDrafts, { deep: true })

function requestLoadTemplate() {
  openConfirmDialog({
    title: 'Load template?',
    message: 'This will replace your current progress in this tab with the sample template.',
    confirmLabel: 'Load template',
    action: () => loadTemplate(activeConfig.value),
  })
}

function requestResetActiveJson() {
  openConfirmDialog({
    title: 'Reset config?',
    message: 'This will replace your current progress in this tab with an empty config.',
    confirmLabel: 'Reset config',
    action: resetActiveJson,
  })
}

function openConfirmDialog({ title, message, confirmLabel, action }) {
  confirmDialog.open = true
  confirmDialog.title = title
  confirmDialog.message = message
  confirmDialog.confirmLabel = confirmLabel
  confirmDialog.action = action
}

function closeConfirmDialog() {
  confirmDialog.open = false
  confirmDialog.title = ''
  confirmDialog.message = ''
  confirmDialog.confirmLabel = ''
  confirmDialog.action = null
}

function confirmDestructiveAction() {
  confirmDialog.action?.()
  closeConfirmDialog()
}
</script>

<template>
  <main class="mx-auto min-h-screen max-w-[1440px] px-4 py-8 text-ink sm:px-5 sm:py-10">
    <section class="mb-6 grid gap-6">
      <AppHeader
        title="Config studio"
        intro="Edit large JSON configs with structure hints, validation feedback, and local file import before we wire in backend upload."
      />
      <ConfigStatusCards :configs="configStats" :active-config="activeConfig" @select="setActiveConfig" />
    </section>

    <section class="grid gap-5 xl:grid-cols-[280px_minmax(0,1fr)]">
      <ConfigNav :config-meta="configMeta" :active-config="activeConfig" @select="setActiveConfig" />

      <section class="rounded-lg border border-slate-200 bg-white p-5">
        <header class="mb-5 flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
          <div class="min-w-0 xl:max-w-md">
            <h2 class="text-2xl font-semibold">{{ activeMeta.label }}</h2>
            <p class="mt-1.5 text-slate-500">{{ activeMeta.description }}</p>
          </div>

          <EditorToolbar
            :editor-mode="editorMode"
            :linux-config-path-available="linuxConfigPathAvailable"
            @set-mode="setEditorMode"
            @import-file="importJson($event, activeConfig)"
            @load-template="requestLoadTemplate"
            @download="downloadActiveConfig"
            @format="formatActiveJson"
            @minify="minifyActiveJson"
            @reset="requestResetActiveJson"
          />
        </header>

        <div class="grid gap-5 2xl:grid-cols-[minmax(0,1.3fr)_minmax(300px,0.7fr)]">
          <section class="min-w-0">
            <div class="mb-3 flex flex-col gap-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
              <span class="truncate">{{ editorState[activeConfig].lastImportedName || 'Starter sample loaded' }}</span>
              <span
                class="inline-flex w-fit items-center rounded-full px-3 py-1 text-sm font-semibold"
                :class="activeParsed.error ? 'bg-rose-100 text-rose-700' : 'bg-emerald-100 text-emerald-700'"
              >
                {{ activeParsed.error ? 'Invalid JSON' : 'Valid JSON' }}
              </span>
            </div>

            <div v-if="editorMode === 'form' && activeJsonValue" class="grid gap-4">
              <DataSourceEditor
                v-if="activeConfig === 'ds'"
                :ds-sources="dsSources"
                :active-ds-source-key="activeDsSourceKey"
                :active-ds-source="activeDsSource"
                :active-alerts="activeAlerts"
                :active-ds-alert-key="activeDsAlertKey"
                :active-ds-alert="activeDsAlert"
                :configured-rules="configuredRules"
                :selected-keys="selectedKeys"
                :field-help="fieldHelp"
                :platform-options="platformOptions"
                :log-type-options="logTypeOptions"
                @context-menu="openContextMenu"
                @add-source="addDataSource"
                @update-source="updateDsSourceField"
                @add-option="addDataSourceOption"
                @update-option="updateDataSourceOption"
                @delete-option="deleteDataSourceOption"
                @add-alert="addAlert"
                @update-alert="updateAlertField"
                @toggle-alert-rule="toggleAlertRule"
              />

              <RuleEditor
                v-else-if="activeConfig === 'rule'"
                :rules-list="rulesList"
                :active-rule-key="activeRuleKey"
                :active-rule="activeRule"
                :selected-keys="selectedKeys"
                :field-help="fieldHelp"
                :check-type-options="checkTypeOptions"
                @context-menu="openContextMenu"
                @add-rule="addRule"
                @update-rule="updateRuleField"
              />

              <AppSettingsEditor
                v-else
                :value="activeJsonValue"
                :field-help="fieldHelp"
                @update-field="updateAppField"
              />
            </div>

            <textarea
              v-else
              v-model="activeText"
              readonly
              class="min-h-[520px] w-full resize-y rounded-lg border border-slate-300 bg-slate-100 p-4 font-mono text-[0.93rem] leading-6 text-slate-700 outline-none ring-0 xl:min-h-[720px]"
              spellcheck="false"
              autocapitalize="off"
              autocomplete="off"
              autocorrect="off"
            />

            <p v-if="activeError" class="mt-3 text-sm text-rose-700">
              {{ activeError.message }}
            </p>
          </section>

          <ConfigInspector :summary="activeSummary" />
        </div>
      </section>
    </section>

    <ConfirmDialog
      :open="confirmDialog.open"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :confirm-label="confirmDialog.confirmLabel"
      @cancel="closeConfirmDialog"
      @confirm="confirmDestructiveAction"
    />

    <button
      v-if="contextMenu.open"
      type="button"
      class="fixed inset-0 z-30 cursor-default bg-transparent"
      aria-label="Close context menu"
      @click="closeContextMenu"
      @contextmenu.prevent="closeContextMenu"
    />

    <div
      v-if="contextMenu.open"
      class="fixed z-40 min-w-44 overflow-hidden rounded-lg border border-slate-200 bg-white py-1 text-sm shadow-xl"
      :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
      @click.stop
    >
      <button
        type="button"
        class="block w-full px-4 py-2 text-left text-slate-700 hover:bg-slate-100"
        @click="handleContextMenuAction('duplicate')"
      >
        Duplicate
      </button>
      <button
        type="button"
        class="block w-full px-4 py-2 text-left text-rose-700 hover:bg-rose-50"
        @click="handleContextMenuAction('delete')"
      >
        Delete
      </button>
    </div>
  </main>
</template>
