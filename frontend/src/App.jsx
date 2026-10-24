import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'
import Navbar from './components/Navbar.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Home from './pages/Home.jsx'
import AskDoubt from './pages/AskDoubt.jsx'
import DoubtDetail from './pages/DoubtDetail.jsx'
import KnowledgeBase from './pages/KnowledgeBase.jsx'
import Leaderboard from './pages/Leaderboard.jsx'
import Profile from './pages/Profile.jsx'
import Notifications from './pages/Notifications.jsx'
import Mentor from './pages/Mentor.jsx'
import Admin from './pages/Admin.jsx'

function Protected({ roles, children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="container">Loading...</div>
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const { user } = useAuth()
  return (
    <>
      {user && <Navbar />}
      <Routes>
        <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
        <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />
        <Route path="/" element={<Protected><Home /></Protected>} />
        <Route path="/ask" element={<Protected><AskDoubt /></Protected>} />
        <Route path="/doubts/:id" element={<Protected><DoubtDetail /></Protected>} />
        <Route path="/knowledge-base" element={<Protected><KnowledgeBase /></Protected>} />
        <Route path="/leaderboard" element={<Protected><Leaderboard /></Protected>} />
        <Route path="/profile" element={<Protected><Profile /></Protected>} />
        <Route path="/notifications" element={<Protected><Notifications /></Protected>} />
        <Route path="/mentor" element={<Protected roles={['MENTOR', 'ADMIN']}><Mentor /></Protected>} />
        <Route path="/admin" element={<Protected roles={['ADMIN']}><Admin /></Protected>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  )
}
