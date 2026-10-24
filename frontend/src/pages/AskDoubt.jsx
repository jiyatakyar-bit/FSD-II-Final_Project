import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../api'

export default function AskDoubt() {
  const navigate = useNavigate()
  const [subjects, setSubjects] = useState([])
  const [similar, setSimilar] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({
    subjectId: '', title: '', description: '', tags: '', codeSnippet: '', anonymous: true, urgent: false,
  })

  useEffect(() => {
    api.get('/api/subjects').then((r) => {
      setSubjects(r.data)
      if (r.data.length) setForm((f) => ({ ...f, subjectId: r.data[0].id }))
    })
  }, [])

  // duplicate suggestions while the title is typed
  useEffect(() => {
    if (form.title.trim().length < 4) { setSimilar([]); return }
    const t = setTimeout(() => {
      api.get('/api/doubts/similar', { params: { title: form.title } }).then((r) => setSimilar(r.data)).catch(() => {})
    }, 500)
    return () => clearTimeout(t)
  }, [form.title])

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const res = await api.post('/api/doubts', { ...form, subjectId: Number(form.subjectId) })
      navigate(`/doubts/${res.data.id}`)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container narrow">
      <h2>Ask a Doubt</h2>
      <form className="card" onSubmit={submit}>
        {error && <div className="alert">{error}</div>}
        <label>Subject</label>
        <select value={form.subjectId} onChange={set('subjectId')} required>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        <label>Title</label>
        <input value={form.title} onChange={set('title')} maxLength={200} required placeholder="Summarise your doubt in one line" />
        {similar.length > 0 && (
          <div className="similar">
            <b>Already solved? Check these first:</b>
            {similar.map((s) => <div key={s.id}><Link to={`/doubts/${s.id}`} target="_blank">{s.title}</Link> <span className="muted">({s.subjectName})</span></div>)}
          </div>
        )}
        <label>Description</label>
        <textarea rows={6} value={form.description} onChange={set('description')} maxLength={4000} required
                  placeholder="Explain what you tried and where you are stuck" />
        <label>Tags (comma separated)</label>
        <input value={form.tags} onChange={set('tags')} maxLength={200} placeholder="arrays, recursion" />
        <label>Code snippet (optional)</label>
        <textarea className="mono" rows={5} value={form.codeSnippet} onChange={set('codeSnippet')} maxLength={4000} />
        <label className="check"><input type="checkbox" checked={form.anonymous} onChange={set('anonymous')} /> Post anonymously (others will see "Anonymous Student")</label>
        <label className="check"><input type="checkbox" checked={form.urgent} onChange={set('urgent')} /> Mark as urgent (sent to more helpers)</label>
        <button className="btn" disabled={busy}>{busy ? 'Posting...' : 'Post Doubt'}</button>
      </form>
    </div>
  )
}
