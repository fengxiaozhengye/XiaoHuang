import api from './axios'

export function getMyProfile() {
  return api.get('/profile/me')
}

export function getProfile(userId: number) {
  return api.get(`/profile/${userId}`)
}

export function updateProfile(userId: number, data: Record<string, unknown>) {
  return api.put(`/profile/${userId}`, data)
}

export function getProfileSummary(userId: number) {
  return api.get(`/profile/${userId}/summary`)
}
