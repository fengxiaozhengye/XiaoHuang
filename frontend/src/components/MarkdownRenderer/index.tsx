import { useEffect, useRef } from 'react'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
})

interface Props {
  content: string
}

export default function MarkdownRenderer({ content }: Props) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (ref.current) {
      ref.current.innerHTML = md.render(content || '')
    }
  }, [content])

  return (
    <div
      ref={ref}
      style={{
        lineHeight: 1.8,
        wordBreak: 'break-word',
      }}
    />
  )
}
