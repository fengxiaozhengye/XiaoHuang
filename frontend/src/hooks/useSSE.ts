import { useCallback, useRef, useState } from 'react'
import { getToken } from '../utils/token'

interface SSEOptions {
  url: string
  body: Record<string, unknown>
  onMessage: (data: { type: string; content: string }) => void
  onDone?: () => void
  onError?: (error: string) => void
}

export function useSSE() {
  const [loading, setLoading] = useState(false)
  const abortRef = useRef<AbortController | null>(null)

  const start = useCallback(({ url, body, onMessage, onDone, onError }: SSEOptions) => {
    setLoading(true)
    const controller = new AbortController()
    abortRef.current = controller

    fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${getToken()}`,
      },
      body: JSON.stringify(body),
      signal: controller.signal,
    })
      .then(async (response) => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const reader = response.body?.getReader()
        if (!reader) return
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })

          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.startsWith('data:')) {
              try {
                const data = JSON.parse(line.slice(5).trim())
                onMessage(data)
                if (data.type === 'done') {
                  onDone?.()
                  setLoading(false)
                  return
                }
              } catch {
                // skip malformed data
              }
            }
          }
        }
        onDone?.()
        setLoading(false)
      })
      .catch((err) => {
        if (err.name !== 'AbortError') {
          onError?.(err.message)
        }
        setLoading(false)
      })
  }, [])

  const stop = useCallback(() => {
    abortRef.current?.abort()
    setLoading(false)
  }, [])

  return { loading, start, stop }
}
