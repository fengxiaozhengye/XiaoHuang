import api from './axios'

export function getOverview() {
  return api.get('/analytics/overview')
}

export function getProgress(courseId: number) {
  return api.get('/analytics/progress', { params: { courseId } })
}
