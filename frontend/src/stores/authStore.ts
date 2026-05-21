import { create } from 'zustand'
import { getToken, setToken, removeToken } from '../utils/token'

interface UserInfo {
  userId: number
  username: string
  role: string
}

interface AuthState {
  token: string | null
  userInfo: UserInfo | null
  login: (token: string, userInfo: UserInfo) => void
  logout: () => void
  setUserInfo: (userInfo: UserInfo) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: getToken(),
  userInfo: null,
  login: (token, userInfo) => {
    setToken(token)
    set({ token, userInfo })
  },
  logout: () => {
    removeToken()
    set({ token: null, userInfo: null })
  },
  setUserInfo: (userInfo) => set({ userInfo }),
}))
