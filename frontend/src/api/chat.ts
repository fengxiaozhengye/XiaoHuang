import api from './axios'

export function createSession(data: { agentType?: string; courseId?: number; knowledgePointId?: number }) {
  return api.post('/chat/session', data)
}

export function sendMessage(sessionId: number, content: string) {
  return api.post('/chat/send', { sessionId, content })
}

export function getSessions() {
  return api.get('/chat/sessions')
}

export function getHistory(sessionId: number, page = 0, size = 50) {
  return api.get(`/chat/session/${sessionId}/history`, { params: { page, size } })
}
