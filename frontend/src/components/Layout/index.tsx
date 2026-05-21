import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Avatar, Dropdown, theme } from 'antd'
import {
  DashboardOutlined,
  UserOutlined,
  BookOutlined,
  NodeIndexOutlined,
  FileTextOutlined,
  RobotOutlined,
  BarChartOutlined,
  FolderOpenOutlined,
  LogoutOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../../stores/authStore'

const { Header, Sider, Content } = Layout

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '学习概览' },
  { key: '/profile', icon: <UserOutlined />, label: '学生画像' },
  { key: '/course', icon: <BookOutlined />, label: '课程中心' },
  { key: '/learning-path', icon: <NodeIndexOutlined />, label: '学习路径' },
  { key: '/resource', icon: <FileTextOutlined />, label: '学习资源' },
  { key: '/chat', icon: <RobotOutlined />, label: 'AI对话' },
  { key: '/knowledge-graph', icon: <NodeIndexOutlined />, label: '知识图谱' },
  { key: '/analytics', icon: <BarChartOutlined />, label: '学习分析' },
  { key: '/document', icon: <FolderOpenOutlined />, label: '个人知识库' },
]

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const logout = useAuthStore((s) => s.logout)
  const userInfo = useAuthStore((s) => s.userInfo)
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken()

  const userMenuItems = [
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录' },
  ]

  const handleUserMenu = ({ key }: { key: string }) => {
    if (key === 'logout') {
      logout()
      navigate('/login')
    }
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ height: 32, margin: 16, textAlign: 'center', color: '#fff', fontWeight: 'bold', fontSize: collapsed ? 14 : 18 }}>
          {collapsed ? 'ES' : 'EduSmart'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: '0 24px', background: colorBgContainer, display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenu }} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} />
              <span>{userInfo?.username || '用户'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: colorBgContainer, borderRadius: borderRadiusLG, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
