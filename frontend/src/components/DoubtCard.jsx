import { Link } from 'react-router-dom'

export const timeAgo = (iso) => {
  const mins = Math.floor((Date.now() - new Date(iso).getTime()) / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins} min ago`
  if (mins < 1440) return `${Math.floor(mins / 60)} h ago`
  return `${Math.floor(mins / 1440)} d ago`
}

export default function DoubtCard({ d }) {
  return (
    <Link to={`/doubts/${d.id}`} className="card doubt-card">
      <div className="row between">
        <h3>{d.title}</h3>
        <span className={`status ${d.status}`}>{d.status}</span>
      </div>
      <p className="muted clamp">{d.description}</p>
      <div className="row gap wrap">
        <span className="chip">{d.subjectName}</span>
        {d.urgent && <span className="chip danger">Urgent</span>}
        {d.escalationLevel > 0 && <span className="chip warn">Escalated x{d.escalationLevel}</span>}
        {d.tags && d.tags.split(',').map((t) => <span key={t} className="chip light">#{t.trim()}</span>)}
        <span className="muted small-text push-right">
          {d.askerName} · {d.answerCount} answers · {timeAgo(d.createdAt)}
        </span>
      </div>
    </Link>
  )
}
