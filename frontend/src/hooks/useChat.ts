import { useState, useCallback } from 'react'
import { getHistory, createSession } from '../api/chat'
import { useSSE } from './useSSE'

interface Message {
  role: 'USER' | 'ASSISTANT'
  content: string
}

export function useChat() {
  const [messages, setMessages] = useState<Message[]>([])
  const [sessionId, setSessionId] = useState<number | null>(null)
  const [streamingContent, setStreamingContent] = useState('')
  const { loading, start, stop } = useSSE()

  const initSession = useCallback(async (agentType = 'TUTOR', courseId?: number) => {
    const res = await createSession({ agentType, courseId }) as any
    const sid = res.data.id
    setSessionId(sid)
    setMessages([])
    return sid
  }, [])

  const loadHistory = useCallback(async (sid: number) => {
    const res = await getHistory(sid, 0, 50) as any
    setMessages(res.data.content || [])
  }, [])

  const send = useCallback((content: string) => {
    if (!sessionId) return

    setMessages((prev) => [...prev, { role: 'USER', content }])
    setStreamingContent('')

    let fullContent = ''

    start({
      url: '/api/chat/send',
      body: { sessionId, content },
      onMessage: (data) => {
        if (data.type === 'content') {
          fullContent += data.content
          setStreamingContent(fullContent)
        }
      },
      onDone: () => {
        setMessages((prev) => [...prev, { role: 'ASSISTANT', content: fullContent }])
        setStreamingContent('')
      },
      onError: (err) => {
        setMessages((prev) => [...prev, { role: 'ASSISTANT', content: `错误: ${err}` }])
        setStreamingContent('')
      },
    })
  }, [sessionId, start])

  return { messages, sessionId, streamingContent, loading, initSession, loadHistory, send, stop }
}
