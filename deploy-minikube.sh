#!/usr/bin/env bash
set -euo pipefail

# --------------------------------------------------
# Minimal LogBuddy Minikube Deployment Script
# Deploys:
# - data-processing
# - spark-processing
# - ai-analyze
# --------------------------------------------------

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

RELEASE_NAME="${RELEASE_NAME:-logbuddy-processing}"
NAMESPACE="${NAMESPACE:-default}"
MINIKUBE_PROFILE="${MINIKUBE_PROFILE:-minikube}"
CONFIGMAP_NAME="${CONFIGMAP_NAME:-logbuddy-client-config}"
HELM_CHART_PATH="${HELM_CHART_PATH:-$ROOT_DIR/helm/logbuddy-processing}"

DATA_IMAGE="${DATA_IMAGE:-logbuddy/data-processing:latest}"
SPARK_IMAGE="${SPARK_IMAGE:-logbuddy/spark-processing:latest}"
AI_ANALYZE_IMAGE="${AI_ANALYZE_IMAGE:-logbuddy/ai-analyze:latest}"

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

delete_deployment_and_wait() {
  local deployment_name="$1"
  local app_name="$2"

  kubectl delete deployment "$deployment_name" -n "$NAMESPACE" --ignore-not-found
  kubectl wait --for=delete deployment/"$deployment_name" -n "$NAMESPACE" --timeout=120s >/dev/null 2>&1 || true
  kubectl wait \
    --for=delete pod \
    -l "app.kubernetes.io/instance=$RELEASE_NAME,app.kubernetes.io/name=$app_name,app.kubernetes.io/part-of=logbuddy-processing" \
    -n "$NAMESPACE" \
    --timeout=180s >/dev/null 2>&1 || true
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

# --------------------------------------------------
# Preconditions
# --------------------------------------------------

require_cmd minikube
require_cmd kubectl
require_cmd helm
require_cmd docker

log "Ensuring Minikube is running"
if ! minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1; then
  minikube -p "$MINIKUBE_PROFILE" start
fi

minikube -p "$MINIKUBE_PROFILE" update-context

log "Switching Docker client to Minikube daemon"
eval "$(minikube -p "$MINIKUBE_PROFILE" docker-env)"

log "Cleaning up previous deployments if they exist"
delete_deployment_and_wait "$RELEASE_NAME"-data-processing data-processing
delete_deployment_and_wait "$RELEASE_NAME"-spark-processing spark-processing
delete_deployment_and_wait "$RELEASE_NAME"-ai-analyze ai-analyze

# --------------------------------------------------
# Force refresh ConfigMap
# --------------------------------------------------

log "Recreating ConfigMap $CONFIGMAP_NAME"
kubectl delete configmap "$CONFIGMAP_NAME" -n "$NAMESPACE" --ignore-not-found

kubectl create configmap "$CONFIGMAP_NAME" -n "$NAMESPACE" \
  --from-file=app.conf=/opt/logbuddy-k8s/config/app.conf \
  --from-file=ds.conf=/opt/logbuddy-k8s/config/ds.conf \
  --from-file=rule.conf=/opt/logbuddy-k8s/config/rule.conf

# --------------------------------------------------
# Build Images
# --------------------------------------------------

log "Building data-processing image"
docker build -t "$DATA_IMAGE" "$ROOT_DIR/DataProcessing"

log "Building spark-processing image"
docker build -t "$SPARK_IMAGE" "$ROOT_DIR/SparkProcessing"

log "Building ai-analyze image"
docker build -t "$AI_ANALYZE_IMAGE" "$ROOT_DIR/ai_analyze"

# --------------------------------------------------
# Deploy Helm Release
# --------------------------------------------------

log "Deploying Helm release"
helm upgrade --install "$RELEASE_NAME" "$HELM_CHART_PATH" \
  --namespace "$NAMESPACE" \
  --create-namespace \
  --set "config.existingConfigMap=$CONFIGMAP_NAME" \
  --set "dataProcessing.image.repository=${DATA_IMAGE%:*}" \
  --set "dataProcessing.image.tag=${DATA_IMAGE##*:}" \
  --set "sparkProcessing.image.repository=${SPARK_IMAGE%:*}" \
  --set "sparkProcessing.image.tag=${SPARK_IMAGE##*:}" \
  --set "aiAnalyze.image.repository=${AI_ANALYZE_IMAGE%:*}" \
  --set "aiAnalyze.image.tag=${AI_ANALYZE_IMAGE##*:}"

# --------------------------------------------------
# Wait for Rollouts
# --------------------------------------------------

log "Waiting for Helm-managed rollouts"
kubectl rollout status deployment/"$RELEASE_NAME"-data-processing -n "$NAMESPACE" --timeout=120s
kubectl rollout status deployment/"$RELEASE_NAME"-spark-processing -n "$NAMESPACE" --timeout=120s
kubectl rollout status deployment/"$RELEASE_NAME"-ai-analyze -n "$NAMESPACE" --timeout=120s

# --------------------------------------------------
# Status + Logs
# --------------------------------------------------

log "Current pods"
kubectl get pods -n "$NAMESPACE" -o wide

log "Recent data-processing logs"
kubectl logs deployment/"$RELEASE_NAME"-data-processing -n "$NAMESPACE" --since=2m || true

log "Recent spark-processing logs"
kubectl logs deployment/"$RELEASE_NAME"-spark-processing -n "$NAMESPACE" --since=2m || true

log "Recent ai-analyze logs"
kubectl logs deployment/"$RELEASE_NAME"-ai-analyze -n "$NAMESPACE" --since=2m || true
