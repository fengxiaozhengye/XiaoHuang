import api from './axios'

export function uploadDocument(file: File, courseId?: number) {
  const formData = new FormData()
  formData.append('file', file)
  if (courseId) formData.append('courseId', String(courseId))
  return api.post('/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getDocuments(params?: { fileType?: string; page?: number; size?: number }) {
  return api.get('/document/list', { params })
}

export function getDocument(id: number) {
  return api.get(`/document/${id}`)
}

export function getDocumentStatus(id: number) {
  return api.get(`/document/${id}/status`)
}

export function deleteDocument(id: number) {
  return api.delete(`/document/${id}`)
}
