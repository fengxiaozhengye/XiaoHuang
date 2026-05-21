import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Form, Input, Button, message, Tabs } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { login, register } from '../../api/auth'
import { useAuthStore } from '../../stores/authStore'

export default function Login() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const authLogin = useAuthStore((s) => s.login)

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const res = await login(values) as any
      authLogin(res.data.token, {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
      })
      message.success('登录成功')
      navigate('/')
    } catch {
      // interceptor handles error
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (values: { username: string; password: string; email?: string }) => {
    setLoading(true)
    try {
      const res = await register(values) as any
      authLogin(res.data.token, {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
      })
      message.success('注册成功')
      navigate('/')
    } catch {
      // interceptor handles error
    } finally {
      setLoading(false)
    }
  }

  const loginForm = (
    <Form onFinish={handleLogin} size="large">
      <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
        <Input prefix={<UserOutlined />} placeholder="用户名" />
      </Form.Item>
      <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading} block>
          登录
        </Button>
      </Form.Item>
    </Form>
  )

  const registerForm = (
    <Form onFinish={handleRegister} size="large">
      <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }, { min: 3, message: '至少3个字符' }]}>
        <Input prefix={<UserOutlined />} placeholder="用户名" />
      </Form.Item>
      <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }, { min: 6, message: '至少6个字符' }]}>
        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
      </Form.Item>
      <Form.Item name="email">
        <Input placeholder="邮箱（选填）" />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading} block>
          注册
        </Button>
      </Form.Item>
    </Form>
  )

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400 }} title="EduSmart 智慧学习平台">
        <Tabs
          items={[
            { key: 'login', label: '登录', children: loginForm },
            { key: 'register', label: '注册', children: registerForm },
          ]}
        />
      </Card>
    </div>
  )
}
