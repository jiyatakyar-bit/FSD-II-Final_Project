import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import { errorMessage } from '../api'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={submit}>
        <h1 className="brand-title">PeerNet</h1>
        <p className="muted center">Ask without fear. Learn from your peers.</p>
        {error && <div className="alert">{error}</div>}
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button className="btn" disabled={busy}>{busy ? 'Signing in...' : 'Login'}</button>
        <p className="center small-text">New here? <Link to="/register">Create an account</Link></p>
        <div className="demo-box small-text">
          <b>Local demo accounts</b><br />
          Student: student@peernet.com / Student@123<br />
          Mentor: mentor@peernet.com / Mentor@123<br />
          Admin: admin@peernet.com / Admin@123 (local default)
        </div>
      </form>
    </div>
  )
}
