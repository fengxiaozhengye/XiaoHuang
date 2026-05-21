import api from './axios'

export function getResources(params: { knowledgePointId: number; type?: string; difficulty?: number }) {
  return api.get('/resource/list', { params })
}

export function getResource(id: number) {
  return api.get(`/resource/${id}`)
}

export function generateResource(data: {
  knowledgePointId: number
  type?: string
  difficulty?: number
  studentLevel?: string
}) {
  return api.post('/resource/generate', data)
}
