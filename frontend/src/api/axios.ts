import axios from 'axios'
import { getToken, removeToken } from '../utils/token'
import { message } from 'antd'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    message.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default api
