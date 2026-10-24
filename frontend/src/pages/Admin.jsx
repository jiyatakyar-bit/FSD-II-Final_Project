import { useEffect, useState } from 'react'
import { Bar } from 'react-chartjs-2'
import { BarElement, CategoryScale, Chart as ChartJS, LinearScale, Tooltip } from 'chart.js'
import api, { errorMessage } from '../api'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip)

const WEIGHT_LABELS = {
  w_topic: 'Topic match weight', w_reputation: 'Reputation weight', w_availability: 'Availability weight',
  w_responsiveness: 'Responsiveness weight', escalation_minutes: 'Escalate after (minutes)', helpers_per_round: 'Helpers notified per round',
}

export default function Admin() {
  const [tab, setTab] = useState('stats')
  const [stats, setStats] = useState(null)
  const [settings, setSettings] = useState({})
  const [users, setUsers] = useState([])
  const [subjects, setSubjects] = useState([])
  const [reports, setReports] = useState([])
  const [newSubject, setNewSubject] = useState('')
  const [msg, setMsg] = useState('')

  const flash = (m) => { setMsg(m); setTimeout(() => setMsg(''), 3500) }
  const run = async (fn, ok) => { try { await fn(); if (ok) flash(ok) } catch (e) { flash(errorMessage(e)) } }

  const loadAll = () => {
    api.get('/api/admin/stats').then((r) => setStats(r.data))
    api.get('/api/admin/settings').then((r) => setSettings(r.data))
    api.get('/api/admin/users').then((r) => setUsers(r.data))
    api.get('/api/subjects').then((r) => setSubjects(r.data))
    api.get('/api/admin/reports').then((r) => setReports(r.data))
  }
  useEffect(loadAll, [])

  return (
    <div className="container">
      <h2>Admin Dashboard</h2>
      {msg && <div className="alert info-alert">{msg}</div>}
      <div className="tabs">
        {['stats', 'matching', 'subjects', 'users', 'reports'].map((t) => (
          <button key={t} className={tab === t ? 'active' : ''} onClick={() => setTab(t)}>
            {t === 'reports' ? `Reports (${reports.length})` : t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'stats' && stats && (
        <>
          <div className="stats big">
            <div><b>{stats.totalUsers}</b><span>Users</span></div>
            <div><b>{stats.totalDoubts}</b><span>Doubts</span></div>
            <div><b>{stats.openDoubts}</b><span>Unanswered</span></div>
            <div><b>{stats.resolutionRate}%</b><span>Resolution rate</span></div>
            <div><b>{stats.avgFirstResponseMinutes}</b><span>Avg first response (min)</span></div>
          </div>
          <div className="card">
            <h3>Doubts by subject (most difficult topics)</h3>
            <Bar
              data={{ labels: Object.keys(stats.doubtsBySubject), datasets: [{ label: 'Doubts', data: Object.values(stats.doubtsBySubject), backgroundColor: '#4f6df5' }] }}
              options={{ plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }}
            />
          </div>
        </>
      )}

      {tab === 'matching' && (
        <div className="card">
          <h3>Matching settings</h3>
          <p className="muted small-text">Score = topic × w1 + reputation × w2 + availability × w3 + responsiveness × w4</p>
          {Object.keys(WEIGHT_LABELS).map((k) => (
            <div key={k} className="row between setting">
              <label>{WEIGHT_LABELS[k]}</label>
              <input type="number" step="0.05" min="0" value={settings[k] ?? ''} onChange={(e) => setSettings({ ...settings, [k]: e.target.value })} />
            </div>
          ))}
          <button className="btn" onClick={() => run(() => api.put('/api/admin/settings', settings), 'Settings saved')}>Save settings</button>
        </div>
      )}

      {tab === 'subjects' && (
        <div className="card">
          <h3>Subjects</h3>
          <form className="row gap" onSubmit={(e) => { e.preventDefault(); run(async () => { await api.post('/api/admin/subjects', { name: newSubject }); setNewSubject(''); loadAll() }, 'Subject added') }}>
            <input className="grow" value={newSubject} onChange={(e) => setNewSubject(e.target.value)} placeholder="New subject name" required />
            <button className="btn">Add</button>
          </form>
          {subjects.map((s) => (
            <div key={s.id} className="row between line">
              {s.name}
              <button className="link-btn" onClick={() => run(async () => { await api.delete(`/api/admin/subjects/${s.id}`); loadAll() }, 'Subject deleted')}>Delete</button>
            </div>
          ))}
        </div>
      )}

      {tab === 'users' && (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Points</th><th>Active</th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.name}</td><td>{u.email}</td>
                  <td>
                    <select value={u.role} onChange={(e) => run(async () => { await api.put(`/api/admin/users/${u.id}/role`, { role: e.target.value }); loadAll() }, 'Role updated')}>
                      <option>STUDENT</option><option>MENTOR</option><option>ADMIN</option>
                    </select>
                  </td>
                  <td>{u.reputation}</td>
                  <td><input type="checkbox" checked={u.active} onChange={(e) => run(async () => { await api.put(`/api/admin/users/${u.id}/active`, { active: e.target.checked }); loadAll() })} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'reports' && (
        <div>
          {reports.length === 0 ? <div className="card empty">No open reports.</div> : reports.map((r) => (
            <div key={r.id} className="card">
              <p className="pre">{r.answerContent}</p>
              <p className="muted small-text">Reported by {r.reporterName}: {r.reason}</p>
              <div className="row gap">
                <button className="btn small danger-btn" onClick={() => run(async () => { await api.post(`/api/admin/reports/${r.id}/resolve`, { uphold: true }); loadAll() }, 'Report upheld (-3 pts to author)')}>Uphold</button>
                <button className="btn small secondary" onClick={() => run(async () => { await api.post(`/api/admin/reports/${r.id}/resolve`, { uphold: false }); loadAll() }, 'Report dismissed')}>Dismiss</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
