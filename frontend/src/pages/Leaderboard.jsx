import { useEffect, useState } from 'react'
import api from '../api'

export default function Leaderboard() {
  const [type, setType] = useState('overall')
  const [rows, setRows] = useState([])

  useEffect(() => { api.get('/api/leaderboard', { params: { type } }).then((r) => setRows(r.data)) }, [type])

  return (
    <div className="container narrow">
      <h2>Leaderboard</h2>
      <div className="tabs">
        <button className={type === 'overall' ? 'active' : ''} onClick={() => setType('overall')}>Overall</button>
        <button className={type === 'weekly' ? 'active' : ''} onClick={() => setType('weekly')}>This week</button>
      </div>
      <div className="card">
        {rows.length === 0 ? <p className="muted">No activity yet.</p> : (
          <table>
            <thead><tr><th>#</th><th>Name</th><th>Level</th><th>Points</th></tr></thead>
            <tbody>
              {rows.map((r, i) => (
                <tr key={r.userId}>
                  <td>{i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : i + 1}</td>
                  <td>{r.name}</td><td>{r.level}</td><td><b>{r.points}</b></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
