export async function fetchConfigBundle() {
  const response = await fetch('/api/config')
  if (!response.ok) {
    throw new Error(`Config request failed with status ${response.status}`)
  }
  return response.json()
}

export async function fetchHealth() {
  const response = await fetch('/health')
  if (!response.ok) {
    throw new Error(`Health request failed with status ${response.status}`)
  }
  return response.json()
}
