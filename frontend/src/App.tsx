import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuthStore } from './stores/authStore'
import MainLayout from './components/Layout'

const Login = lazy(() => import('./pages/Login'))
const Dashboard = lazy(() => import('./pages/Dashboard'))
const Profile = lazy(() => import('./pages/Profile'))
const CourseList = lazy(() => import('./pages/Course'))
const CourseDetail = lazy(() => import('./pages/Course/detail'))
const LearningPath = lazy(() => import('./pages/LearningPath'))
const Resource = lazy(() => import('./pages/Resource'))
const Chat = lazy(() => import('./pages/Chat'))
const KnowledgeGraph = lazy(() => import('./pages/KnowledgeGraph'))
const Analytics = lazy(() => import('./pages/Analytics'))
const Document = lazy(() => import('./pages/Document'))

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

function PageLoading() {
  return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
}

export default function App() {
  return (
    <Suspense fallback={<PageLoading />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="profile" element={<Profile />} />
          <Route path="course" element={<CourseList />} />
          <Route path="course/:id" element={<CourseDetail />} />
          <Route path="learning-path" element={<LearningPath />} />
          <Route path="resource" element={<Resource />} />
          <Route path="chat" element={<Chat />} />
          <Route path="knowledge-graph" element={<KnowledgeGraph />} />
          <Route path="analytics" element={<Analytics />} />
          <Route path="document" element={<Document />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  )
}
