FROM ollama/ollama:latest AS ollama

FROM python:3.11-alpine

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    APP_PORT=8000

RUN apk add --no-cache curl && \
    addgroup -S app && \
    adduser -S -G app app

COPY --from=ollama /bin/ollama /usr/local/bin/ollama

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN chown -R app:app /app

USER app

EXPOSE ${APP_PORT}

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
