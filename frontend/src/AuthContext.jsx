import { createContext, useContext, useEffect, useState } from 'react'
import api from './api'

const AuthContext = createContext(null)
export const useAuth = () => useContext(AuthContext)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const refresh = async () => {
    try {
      const res = await api.get('/api/users/me')
      setUser(res.data)
    } catch {
      setUser(null)
    }
  }

  useEffect(() => {
    if (localStorage.getItem('peernet_token')) {
      refresh().finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const login = async (email, password) => {
    const res = await api.post('/api/auth/login', { email, password })
    localStorage.setItem('peernet_token', res.data.token)
    setUser(res.data.user)
  }

  const register = async (name, email, password) => {
    const res = await api.post('/api/auth/register', { name, email, password })
    localStorage.setItem('peernet_token', res.data.token)
    setUser(res.data.user)
  }

  const logout = () => {
    localStorage.removeItem('peernet_token')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  )
}
