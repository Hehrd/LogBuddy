# LogBuddy processing Helm chart

This chart deploys the `data-processing`, `spark-processing`, and `ai-analyze` services to Kubernetes.

## What it creates

- One `Deployment` and `Service` for `data-processing`
- One `Deployment` and `Service` for `spark-processing`
- One `Deployment` and `Service` for `ai-analyze`
- One `Ollama` sidecar container in the `ai-analyze` pod that preloads the configured model
- One client-provided `ConfigMap` mounted at `/opt/logbuddy/config`

## Configure images

The Docker Compose file builds images locally, but Kubernetes needs image references that your cluster can pull.

Set these values before deploying:

```bash
helm upgrade --install logbuddy-processing ./helm/logbuddy-processing \
  --set dataProcessing.image.repository=your-registry/logbuddy-data-processing \
  --set dataProcessing.image.tag=your-tag \
  --set sparkProcessing.image.repository=your-registry/logbuddy-spark-processing \
  --set sparkProcessing.image.tag=your-tag \
  --set aiAnalyze.image.repository=your-registry/logbuddy-ai-analyze \
  --set aiAnalyze.image.tag=your-tag
```

## Configure shared files

The application reads these files inside the container:

- `/opt/logbuddy/config/app.conf`
- `/opt/logbuddy/config/ds.conf`
- `/opt/logbuddy/config/rule.conf`

Those files are client-owned. This chart does not store their contents in `values.yaml`.

Create a `ConfigMap` from the real client files before installing the chart:

```bash
kubectl create configmap <configmap-name> \
  --from-file=app.conf=/path/to/app.conf \
  --from-file=ds.conf=/path/to/ds.conf \
  --from-file=rule.conf=/path/to/rule.conf
```

Then mount that `ConfigMap` at deployment time:

```bash
helm upgrade --install logbuddy-processing ./helm/logbuddy-processing \
  --set config.existingConfigMap=<configmap-name>
```

## Render locally

```bash
helm template logbuddy-processing ./helm/logbuddy-processing
```
