#!/usr/bin/env bash
set -euo pipefail

# --------------------------------------------------
# Minimal LogBuddy Minikube Deployment Script
# Deploys ONLY:
# - data-processing
# - spark-processing
# --------------------------------------------------

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

RELEASE_NAME="${RELEASE_NAME:-logbuddy-processing}"
NAMESPACE="${NAMESPACE:-default}"
MINIKUBE_PROFILE="${MINIKUBE_PROFILE:-minikube}"
CONFIGMAP_NAME="${CONFIGMAP_NAME:-logbuddy-client-config}"
HELM_CHART_PATH="${HELM_CHART_PATH:-$ROOT_DIR/helm/logbuddy-processing}"

DATA_IMAGE="${DATA_IMAGE:-logbuddy/data-processing:latest}"
SPARK_IMAGE="${SPARK_IMAGE:-logbuddy/spark-processing:latest}"

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
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

kubectl delete deployment logbuddy-processing-data-processing
kubectl delete deployment logbuddy-processing-spark-processing

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

# --------------------------------------------------
# Deploy Helm Release
# --------------------------------------------------

log "Deploying Helm release"
helm upgrade --install "$RELEASE_NAME" "$HELM_CHART_PATH" \
  --namespace "$NAMESPACE" \
  --create-namespace \
  --set "config.existingConfigMap=$CONFIGMAP_NAME"

# --------------------------------------------------
# Restart Deployments
# --------------------------------------------------

log "Restarting deployments"
kubectl rollout restart deployment/"$RELEASE_NAME"-data-processing -n "$NAMESPACE"
kubectl rollout restart deployment/"$RELEASE_NAME"-spark-processing -n "$NAMESPACE"

# --------------------------------------------------
# Wait for Rollouts
# --------------------------------------------------

log "Waiting for rollouts"
kubectl rollout status deployment/"$RELEASE_NAME"-data-processing -n "$NAMESPACE" --timeout=120s
kubectl rollout status deployment/"$RELEASE_NAME"-spark-processing -n "$NAMESPACE" --timeout=120s

# --------------------------------------------------
# Status + Logs
# --------------------------------------------------

log "Current pods"
kubectl get pods -n "$NAMESPACE" -o wide

log "Recent data-processing logs"
kubectl logs deployment/"$RELEASE_NAME"-data-processing -n "$NAMESPACE" --since=2m || true

log "Recent spark-processing logs"
kubectl logs deployment/"$RELEASE_NAME"-spark-processing -n "$NAMESPACE" --since=2m || true
