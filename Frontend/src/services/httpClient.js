function normalizeHeaders(headersLike) {
  if (!headersLike) return {}
  if (headersLike instanceof Headers) return Object.fromEntries(headersLike.entries())
  return { ...headersLike }
}

function safeStringify(value) {
  if (value == null) return null
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return '[unserializable payload]'
  }
}

function tryParseJson(text) {
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

export async function fetchJson(url, options = {}) {
  const method = options.method ?? 'GET'
  const requestHeaders = normalizeHeaders(options.headers)
  const requestBody = safeStringify(options.body)
  const startedAt = Date.now()

  console.groupCollapsed(`[api] ${method} ${url}`)
  console.info('Request', { url, method, headers: requestHeaders, body: requestBody })

  try {
    const response = await fetch(url, options)
    const responseText = await response.text()
    const responseJson = tryParseJson(responseText)
    const responseHeaders = normalizeHeaders(response.headers)
    const durationMs = Date.now() - startedAt

    console.info('Response', {
      url,
      method,
      status: response.status,
      ok: response.ok,
      durationMs,
      headers: responseHeaders,
      body: responseJson ?? responseText,
    })

    if (!response.ok) {
      const error = new Error(`Request failed: ${method} ${url} -> ${response.status}`)
      error.details = {
        url,
        method,
        request: { headers: requestHeaders, body: requestBody },
        response: {
          status: response.status,
          headers: responseHeaders,
          bodyText: responseText,
          bodyJson: responseJson,
        },
      }
      console.error('API error details', error.details)
      throw error
    }

    console.groupEnd()
    return responseJson
  } catch (error) {
    console.error('Network/API failure', {
      url,
      method,
      durationMs: Date.now() - startedAt,
      request: { headers: requestHeaders, body: requestBody },
      error,
    })
    console.groupEnd()
    throw error
  }
}

export function formatApiError(error, fallbackMessage) {
  if (!error) return fallbackMessage
  if (error.details) {
    const responseBody = error.details.response?.bodyText
    return `${fallbackMessage} ${error.message}${responseBody ? ` Response body: ${responseBody}` : ''}`
  }
  return error instanceof Error ? `${fallbackMessage} ${error.message}` : fallbackMessage
}
