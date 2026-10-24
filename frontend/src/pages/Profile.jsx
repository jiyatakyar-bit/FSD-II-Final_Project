import { useEffect, useState } from 'react'
import api, { errorMessage } from '../api'
import { useAuth } from '../AuthContext.jsx'

export default function Profile() {
  const { user, refresh } = useAuth()
  const [subjects, setSubjects] = useState([])
  const [selected, setSelected] = useState(user.expertiseSubjectIds || [])
  const [name, setName] = useState(user.name)
  const [msg, setMsg] = useState('')

  useEffect(() => { api.get('/api/subjects').then((r) => setSubjects(r.data)) }, [])

  const toggle = (id) => setSelected(selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id])

  const save = async () => {
    try {
      await api.put('/api/users/me', { name })
      await api.put('/api/users/me/expertise', { subjectIds: selected })
      await refresh()
      setMsg('Profile saved')
    } catch (e) {
      setMsg(errorMessage(e))
    }
    setTimeout(() => setMsg(''), 3000)
  }

  return (
    <div className="container narrow">
      <h2>My Profile</h2>
      <div className="card">
        <div className="stats">
          <div><b>{user.reputation}</b><span>Reputation</span></div>
          <div><b>{user.level}</b><span>Level</span></div>
          <div><b>{user.badges.length}</b><span>Badges</span></div>
        </div>
        <label>Name</label>
        <input value={name} onChange={(e) => setName(e.target.value)} maxLength={80} />
        <label>Email</label>
        <input value={user.email} disabled />
        <label>Subjects I can help with</label>
        <p className="muted small-text">Doubts in these subjects are sent to you first.</p>
        <div className="row gap wrap">
          {subjects.map((s) => (
            <button type="button" key={s.id} className={`chip pick ${selected.includes(s.id) ? 'on' : ''}`} onClick={() => toggle(s.id)}>
              {s.name}
            </button>
          ))}
        </div>
        <button className="btn" onClick={save}>Save profile</button>
        {msg && <span className="muted"> {msg}</span>}
      </div>
      <div className="card">
        <h3>Badges</h3>
        {user.badges.length === 0 ? <p className="muted">Answer doubts to earn badges.</p> : (
          <div className="row gap wrap">{user.badges.map((b) => <span key={b} className="chip success">🏅 {b}</span>)}</div>
        )}
        <p className="muted small-text">Points: answer +5 · upvote +2 · accepted +15 · mentor verified +10 · downvote −3</p>
      </div>
    </div>
  )
}
