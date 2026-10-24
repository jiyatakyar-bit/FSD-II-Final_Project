import axios from 'axios'

export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

const api = axios.create({ baseURL: API_URL })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('peernet_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    // expired or invalid token: go back to login
    if (err.response && err.response.status === 401 && !err.config.url.includes('/api/auth/')) {
      localStorage.removeItem('peernet_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const errorMessage = (err) =>
  err?.response?.data?.message || (err?.response ? 'Something went wrong' : 'Cannot reach the server')

export default api
