import { useEffect, useState } from 'react'
import api from '../api'
import DoubtCard from '../components/DoubtCard.jsx'

export default function Mentor() {
  const [items, setItems] = useState([])
  useEffect(() => { api.get('/api/mentor/queue').then((r) => setItems(r.data)) }, [])

  return (
    <div className="container">
      <h2>Mentor Panel</h2>
      <p className="muted">Unanswered doubts, most escalated first. Open a doubt to answer it, or verify good peer answers.</p>
      {items.length === 0 ? <div className="card empty">Great! No unanswered doubts.</div> : items.map((d) => <DoubtCard key={d.id} d={d} />)}
    </div>
  )
}
