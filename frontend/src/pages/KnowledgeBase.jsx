import { useEffect, useState } from 'react'
import api from '../api'
import DoubtCard from '../components/DoubtCard.jsx'

export default function KnowledgeBase() {
  const [subjects, setSubjects] = useState([])
  const [subjectId, setSubjectId] = useState('')
  const [q, setQ] = useState('')
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ items: [], totalPages: 0, totalItems: 0 })

  useEffect(() => { api.get('/api/subjects').then((r) => setSubjects(r.data)) }, [])
  useEffect(() => {
    const params = { status: 'RESOLVED', page, size: 8 }
    if (subjectId) params.subjectId = subjectId
    if (query) params.q = query
    api.get('/api/doubts', { params }).then((r) => setData(r.data))
  }, [subjectId, query, page])

  const search = (e) => { e.preventDefault(); setPage(0); setQuery(q) }

  return (
    <div className="container">
      <h2>Knowledge Base</h2>
      <p className="muted">Every solved doubt is saved here. Search before you ask.</p>
      <form className="row gap wrap" onSubmit={search}>
        <input className="grow" placeholder="Search title, description or tags" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={subjectId} onChange={(e) => { setSubjectId(e.target.value); setPage(0) }}>
          <option value="">All subjects</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        <button className="btn">Search</button>
      </form>
      <p className="muted small-text">{data.totalItems} solved doubts</p>
      {data.items.length === 0 ? <div className="card empty">Nothing found.</div> : data.items.map((d) => <DoubtCard key={d.id} d={d} />)}
      {data.totalPages > 1 && (
        <div className="row gap center-row">
          <button className="btn small secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span>Page {page + 1} of {data.totalPages}</span>
          <button className="btn small secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}
