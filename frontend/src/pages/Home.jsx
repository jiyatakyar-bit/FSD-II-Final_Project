import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../AuthContext.jsx'
import DoubtCard from '../components/DoubtCard.jsx'

export default function Home() {
  const { user } = useAuth()
  const [tab, setTab] = useState('assigned')
  const [subjects, setSubjects] = useState([])
  const [subjectId, setSubjectId] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ items: [], totalPages: 0 })
  const [assigned, setAssigned] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { api.get('/api/subjects').then((r) => setSubjects(r.data)) }, [])
  useEffect(() => { api.get('/api/doubts/assigned').then((r) => setAssigned(r.data)) }, [])

  useEffect(() => {
    if (tab === 'assigned') { setLoading(false); return }
    setLoading(true)
    const params = { page, size: 8 }
    if (subjectId) params.subjectId = subjectId
    if (tab === 'mine') params.mine = true
    if (tab === 'open') params.status = 'OPEN'
    api.get('/api/doubts', { params }).then((r) => setData(r.data)).finally(() => setLoading(false))
  }, [tab, subjectId, page])

  const switchTab = (t) => { setTab(t); setPage(0) }
  const list = tab === 'assigned' ? assigned : data.items

  return (
    <div className="container">
      <div className="row between wrap">
        <div>
          <h2>Hello, {user.name.split(' ')[0]}</h2>
          <p className="muted">Level: <b>{user.level}</b> · Reputation: <b>{user.reputation}</b></p>
        </div>
        <Link to="/ask" className="btn">+ Ask a Doubt</Link>
      </div>

      <div className="tabs">
        <button className={tab === 'assigned' ? 'active' : ''} onClick={() => switchTab('assigned')}>
          Matched for me ({assigned.length})
        </button>
        <button className={tab === 'feed' ? 'active' : ''} onClick={() => switchTab('feed')}>All doubts</button>
        <button className={tab === 'open' ? 'active' : ''} onClick={() => switchTab('open')}>Unanswered</button>
        <button className={tab === 'mine' ? 'active' : ''} onClick={() => switchTab('mine')}>My doubts</button>
      </div>

      {tab !== 'assigned' && (
        <select className="filter" value={subjectId} onChange={(e) => { setSubjectId(e.target.value); setPage(0) }}>
          <option value="">All subjects</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
      )}

      {loading ? <p>Loading...</p> : list.length === 0 ? (
        <div className="card empty">
          {tab === 'assigned'
            ? 'No doubts are waiting for you right now. Tick your subjects on your profile so we can match you with doubts.'
            : 'No doubts found.'}
        </div>
      ) : list.map((d) => <DoubtCard key={d.id} d={d} />)}

      {tab !== 'assigned' && data.totalPages > 1 && (
        <div className="row gap center-row">
          <button className="btn small secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span>Page {page + 1} of {data.totalPages}</span>
          <button className="btn small secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}
