import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { errorMessage } from '../api'
import { useAuth } from '../AuthContext.jsx'
import { timeAgo } from '../components/DoubtCard.jsx'

export default function DoubtDetail() {
  const { id } = useParams()
  const { user, refresh } = useAuth()
  const navigate = useNavigate()
  const [d, setD] = useState(null)
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    api.get(`/api/doubts/${id}`).then((r) => setD(r.data)).catch((e) => setError(errorMessage(e)))
  }, [id])
  useEffect(load, [load])

  const act = async (fn) => {
    setError('')
    try { await fn(); load(); refresh() } catch (e) { setError(errorMessage(e)) }
  }

  const postAnswer = async (e) => {
    e.preventDefault()
    setBusy(true)
    await act(async () => { await api.post(`/api/doubts/${id}/answers`, { content }); setContent('') })
    setBusy(false)
  }

  const report = (answerId) => {
    const reason = window.prompt('Why are you reporting this answer?')
    if (reason && reason.trim()) act(() => api.post(`/api/answers/${answerId}/report`, { reason }))
  }

  const remove = async () => {
    if (!window.confirm('Delete this doubt?')) return
    try { await api.delete(`/api/doubts/${id}`); navigate('/') } catch (e) { setError(errorMessage(e)) }
  }

  if (!d) return <div className="container">{error ? <div className="alert">{error}</div> : 'Loading...'}</div>

  const canMentor = user.role === 'MENTOR' || user.role === 'ADMIN'
  const locked = d.status === 'CLOSED' || d.status === 'RESOLVED'

  return (
    <div className="container narrow">
      {error && <div className="alert">{error}</div>}
      <div className="card">
        <div className="row between wrap">
          <h2>{d.title}</h2>
          <span className={`status ${d.status}`}>{d.status}</span>
        </div>
        <div className="row gap wrap">
          <span className="chip">{d.subjectName}</span>
          {d.urgent && <span className="chip danger">Urgent</span>}
          {d.tags && d.tags.split(',').map((t) => <span key={t} className="chip light">#{t.trim()}</span>)}
        </div>
        <p className="muted small-text">Asked by {d.askerName} · {timeAgo(d.createdAt)}</p>
        <p className="pre">{d.description}</p>
        {d.codeSnippet && <pre className="code">{d.codeSnippet}</pre>}
        {d.mine && (
          <div className="row gap">
            {!locked && d.status !== 'RESOLVED' && <button className="btn small secondary" onClick={() => act(() => api.post(`/api/doubts/${id}/close`))}>Close doubt</button>}
            {d.answerCount === 0 && <button className="btn small danger-btn" onClick={remove}>Delete</button>}
          </div>
        )}
      </div>

      <h3>{d.answers.length} Answer{d.answers.length !== 1 && 's'}</h3>
      {d.answers.map((a) => (
        <div key={a.id} className={`card answer ${a.accepted ? 'accepted' : ''}`}>
          <div className="vote-col">
            <button className={a.myVote === 1 ? 'on' : ''} onClick={() => act(() => api.post(`/api/answers/${a.id}/vote`, { value: 1 }))}>▲</button>
            <b>{a.votes}</b>
            <button className={a.myVote === -1 ? 'on' : ''} onClick={() => act(() => api.post(`/api/answers/${a.id}/vote`, { value: -1 }))}>▼</button>
          </div>
          <div className="answer-body">
            <div className="row gap wrap">
              {a.accepted && <span className="chip success">✔ Accepted</span>}
              {a.verified && <span className="chip info">Mentor verified</span>}
            </div>
            <p className="pre">{a.content}</p>
            <div className="row between wrap">
              <span className="muted small-text">{a.helperName} · {a.helperLevel} ({a.helperReputation} pts) · {timeAgo(a.createdAt)}</span>
              <div className="row gap">
                {d.mine && !a.accepted && d.status !== 'RESOLVED' && !locked && <button className="btn small" onClick={() => act(() => api.post(`/api/answers/${a.id}/accept`))}>Accept answer</button>}
                {canMentor && !a.verified && <button className="btn small secondary" onClick={() => act(() => api.post(`/api/answers/${a.id}/verify`))}>Verify</button>}
                {a.helperId !== user.id && <button className="link-btn" onClick={() => report(a.id)}>Report</button>}
              </div>
            </div>
          </div>
        </div>
      ))}

      {!d.mine && !locked && (
        <form className="card" onSubmit={postAnswer}>
          <h3>Your answer</h3>
          <textarea rows={5} value={content} onChange={(e) => setContent(e.target.value)} maxLength={4000} required placeholder="Explain clearly and step by step" />
          <button className="btn" disabled={busy}>{busy ? 'Posting...' : 'Post Answer (+5 pts)'}</button>
        </form>
      )}
    </div>
  )
}
