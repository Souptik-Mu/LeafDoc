const API_BASE_URL = 'http://localhost:8080/api'

async function request(path: string, options: RequestInit = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    let message = 'Something went wrong. Please try again.'
    try {
      const data = await response.json()
      message = data.message || data.error || message
    } catch {
      // Some successful/error responses have no JSON body.
    }
    throw new Error(message)
  }

  const contentType = response.headers.get('content-type')
  return contentType?.includes('application/json') ? response.json() : null
}

export type RegisterData = {
  name: string
  email: string
  password: string
}

export async function register(data: RegisterData) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ ...data, role: 'USER' }),
  })
}

export async function login(email: string, password: string) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function verifyEmail(token: string) {
  return request(`/auth/verify-email?token=${encodeURIComponent(token)}`, {
    method: 'GET',
  })
}

export async function getCurrentUser() {
  return request('/auth/me', { method: 'GET' })
}
