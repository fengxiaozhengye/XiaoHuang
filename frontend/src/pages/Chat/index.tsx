import { useEffect, useState, useRef } from 'react'
import { Input, Button, List, Card, Spin } from 'antd'
import { SendOutlined, PlusOutlined } from '@ant-design/icons'
import { useChat } from '../../hooks/useChat'
import { getSessions } from '../../api/chat'
import ChatBubble from '../../components/ChatBubble'

export default function Chat() {
  const { messages, streamingContent, loading, initSession, loadHistory, send } = useChat()
  const [input, setInput] = useState('')
  const [sessions, setSessions] = useState<any[]>([])
  const [currentSessionId, setCurrentSessionId] = useState<number | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    getSessions().then((res: any) => setSessions(res.data || []))
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, streamingContent])

  const handleNewSession = async () => {
    const sid = await initSession('TUTOR')
    setCurrentSessionId(sid)
    setSessions((prev) => [{ id: sid, agentType: 'TUTOR', createdAt: new Date().toISOString() }, ...prev])
  }

  const handleSelectSession = async (sid: number) => {
    setCurrentSessionId(sid)
    await loadHistory(sid)
  }

  const handleSend = () => {
    if (!input.trim()) return
    if (!currentSessionId) {
      handleNewSession().then(() => {
        // will be handled after session created
      })
      return
    }
    send(input.trim())
    setInput('')
  }

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 160px)' }}>
      {/* 会话列表 */}
      <Card
        style={{ width: 250, marginRight: 16, overflow: 'auto' }}
        title="对话会话"
        extra={<Button icon={<PlusOutlined />} size="small" onClick={handleNewSession}>新建</Button>}
      >
        <List
          size="small"
          dataSource={sessions}
          renderItem={(session: any) => (
            <List.Item
              style={{
                cursor: 'pointer',
                background: currentSessionId === session.id ? '#e6f4ff' : 'transparent',
                padding: '8px',
                borderRadius: 6,
              }}
              onClick={() => handleSelectSession(session.id)}
            >
              <div>
                <div style={{ fontWeight: 500 }}>{session.agentType || 'TUTOR'}</div>
                <div style={{ fontSize: 12, color: '#999' }}>{new Date(session.createdAt).toLocaleString()}</div>
              </div>
            </List.Item>
          )}
        />
      </Card>

      {/* 对话区域 */}
      <Card style={{ flex: 1, display: 'flex', flexDirection: 'column' }} bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <div style={{ flex: 1, overflow: 'auto', padding: '0 0 16px' }}>
          {messages.map((msg, i) => (
            <ChatBubble key={i} role={msg.role} content={msg.content} />
          ))}
          {streamingContent && (
            <ChatBubble role="ASSISTANT" content={streamingContent} />
          )}
          {loading && !streamingContent && (
            <div style={{ textAlign: 'center', padding: 20 }}><Spin tip="思考中..." /></div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <div style={{ display: 'flex', gap: 8, borderTop: '1px solid #f0f0f0', paddingTop: 12 }}>
          <Input.TextArea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onPressEnter={(e) => { if (!e.shiftKey) { e.preventDefault(); handleSend() } }}
            placeholder="输入你的问题..."
            autoSize={{ minRows: 1, maxRows: 4 }}
            style={{ flex: 1 }}
          />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={handleSend}
            loading={loading}
          >
            发送
          </Button>
        </div>
      </Card>
    </div>
  )
}
