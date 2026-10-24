import { useEffect, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import api, { API_URL } from '../api'
import { useAuth } from '../AuthContext.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)
  const [toast, setToast] = useState(null)

  useEffect(() => {
    const loadCount = () =>
      api.get('/api/notifications/unread-count').then((r) => setUnread(r.data.count)).catch(() => {})
    loadCount()

    // live notifications over WebSocket (STOMP)
    const client = new Client({
      brokerURL: API_URL.replace(/^http/, 'ws') + '/ws',
      connectHeaders: { Authorization: `Bearer ${localStorage.getItem('peernet_token')}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (msg) => {
          const n = JSON.parse(msg.body)
          setUnread((c) => c + 1)
          setToast(n)
          setTimeout(() => setToast(null), 5000)
        })
      },
    })
    client.activate()
    window.addEventListener('notifications-read', loadCount)
    return () => {
      client.deactivate()
      window.removeEventListener('notifications-read', loadCount)
    }
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <>
      <nav className="navbar">
        <Link to="/" className="brand">PeerNet</Link>
        <div className="nav-links">
          <NavLink to="/" end>Home</NavLink>
          <NavLink to="/ask">Ask Doubt</NavLink>
          <NavLink to="/knowledge-base">Knowledge Base</NavLink>
          <NavLink to="/leaderboard">Leaderboard</NavLink>
          {(user.role === 'MENTOR' || user.role === 'ADMIN') && <NavLink to="/mentor">Mentor Panel</NavLink>}
          {user.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
        </div>
        <div className="nav-right">
          <Link to="/notifications" className="bell" title="Notifications">
            &#128276;{unread > 0 && <span className="badge-count">{unread}</span>}
          </Link>
          <Link to="/profile" className="nav-user">{user.name} · {user.reputation} pts</Link>
          <button className="btn small secondary" onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      {toast && (
        <div className="toast" onClick={() => toast.doubtId && navigate(`/doubts/${toast.doubtId}`)}>
          {toast.message}
        </div>
      )}
    </>
  )
}
