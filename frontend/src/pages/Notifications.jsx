import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import { timeAgo } from '../components/DoubtCard.jsx'

export default function Notifications() {
  const navigate = useNavigate()
  const [items, setItems] = useState([])

  useEffect(() => {
    api.get('/api/notifications').then((r) => {
      setItems(r.data)
      api.post('/api/notifications/read-all').then(() => window.dispatchEvent(new Event('notifications-read')))
    })
  }, [])

  return (
    <div className="container narrow">
      <h2>Notifications</h2>
      {items.length === 0 ? <div className="card empty">No notifications yet.</div> : items.map((n) => (
        <div key={n.id} className={`card notif ${n.seen ? '' : 'unseen'}`} onClick={() => n.doubtId && navigate(`/doubts/${n.doubtId}`)}>
          <span className="chip light">{n.type}</span> {n.message}
          <div className="muted small-text">{timeAgo(n.createdAt)}</div>
        </div>
      ))}
    </div>
  )
}
