import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import { errorMessage } from '../api'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await register(form.name, form.email, form.password)
      navigate('/profile')
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={submit}>
        <h1 className="brand-title">Join PeerNet</h1>
        {error && <div className="alert">{error}</div>}
        <label>Full name</label>
        <input value={form.name} onChange={set('name')} required maxLength={80} />
        <label>Email</label>
        <input type="email" value={form.email} onChange={set('email')} required />
        <label>Password (min 6 characters)</label>
        <input type="password" value={form.password} onChange={set('password')} required minLength={6} />
        <button className="btn" disabled={busy}>{busy ? 'Creating...' : 'Register'}</button>
        <p className="center small-text">Already registered? <Link to="/login">Login</Link></p>
      </form>
    </div>
  )
}
