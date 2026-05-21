import api from './axios'

export function getCourses(params?: { keyword?: string; page?: number; size?: number }) {
  return api.get('/course/list', { params })
}

export function getCourse(id: number) {
  return api.get(`/course/${id}`)
}

export function createCourse(data: { name: string; description?: string; subjectArea?: string }) {
  return api.post('/course', data)
}

export function publishCourse(id: number) {
  return api.put(`/course/${id}/publish`)
}

export function getKnowledgePoints(courseId: number) {
  return api.get(`/knowledge/course/${courseId}`)
}

export function getKnowledgeGraph(courseId: number) {
  return api.get(`/knowledge/graph/${courseId}`)
}

export function importKnowledge(courseId: number, data: { points: unknown[]; dependencies?: unknown[] }) {
  return api.post(`/knowledge/course/${courseId}/import`, data)
}
