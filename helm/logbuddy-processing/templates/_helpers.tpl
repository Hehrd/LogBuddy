{{- define "logbuddy-processing.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "logbuddy-processing.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := include "logbuddy-processing.name" . -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "logbuddy-processing.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "logbuddy-processing.labels" -}}
helm.sh/chart: {{ include "logbuddy-processing.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "logbuddy-processing.selectorLabels" -}}
app.kubernetes.io/part-of: {{ include "logbuddy-processing.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "logbuddy-processing.dataProcessingName" -}}
{{- printf "%s-data-processing" (include "logbuddy-processing.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "logbuddy-processing.sparkProcessingName" -}}
{{- printf "%s-spark-processing" (include "logbuddy-processing.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "logbuddy-processing.testHadoopName" -}}
{{- printf "%s-%s" (include "logbuddy-processing.fullname" .) .Values.testHadoop.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
