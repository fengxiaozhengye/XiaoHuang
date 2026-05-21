import api from './axios'

export function generatePath(courseId: number) {
  return api.post('/path/generate', { courseId })
}

export function getActivePath(courseId: number) {
  return api.get('/path/active', { params: { courseId } })
}

export function getUserPaths() {
  return api.get('/path/list')
}

export function advanceStep(pathId: number) {
  return api.put(`/path/${pathId}/next`)
}
