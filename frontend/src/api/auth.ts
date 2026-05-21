import api from './axios'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  email?: string
  realName?: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  role: string
}

export function login(params: LoginParams) {
  return api.post<LoginResult>('/auth/login', params)
}

export function register(params: RegisterParams) {
  return api.post<LoginResult>('/auth/register', params)
}

export function getCurrentUser() {
  return api.get('/auth/me')
}
