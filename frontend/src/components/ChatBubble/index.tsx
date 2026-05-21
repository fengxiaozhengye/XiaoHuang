import { Avatar } from 'antd'
import { UserOutlined, RobotOutlined } from '@ant-design/icons'
import MarkdownRenderer from '../MarkdownRenderer'

interface Props {
  role: 'USER' | 'ASSISTANT'
  content: string
}

export default function ChatBubble({ role, content }: Props) {
  const isUser = role === 'USER'

  return (
    <div
      style={{
        display: 'flex',
        gap: 12,
        marginBottom: 16,
        flexDirection: isUser ? 'row-reverse' : 'row',
      }}
    >
      <Avatar icon={isUser ? <UserOutlined /> : <RobotOutlined />} />
      <div
        style={{
          maxWidth: '75%',
          padding: '12px 16px',
          borderRadius: 12,
          background: isUser ? '#1677ff' : '#f5f5f5',
          color: isUser ? '#fff' : '#333',
        }}
      >
        {isUser ? content : <MarkdownRenderer content={content} />}
      </div>
    </div>
  )
}
