import { useState } from 'react'
import { Card, InputNumber, Button, Empty, Spin, Tag, Space } from 'antd'
import { getKnowledgeGraph } from '../../api/course'

export default function KnowledgeGraph() {
  const [courseId, setCourseId] = useState<number | null>(null)
  const [graph, setGraph] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  const fetchGraph = async () => {
    if (!courseId) return
    setLoading(true)
    try {
      const res = await getKnowledgeGraph(courseId) as any
      setGraph(res.data)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2>知识图谱</h2>
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <InputNumber
            placeholder="课程ID"
            value={courseId}
            onChange={(v) => setCourseId(v)}
            style={{ width: 150 }}
          />
          <Button type="primary" onClick={fetchGraph} loading={loading} disabled={!courseId}>
            加载图谱
          </Button>
        </Space>
      </Card>

      {loading && <Spin size="large" style={{ display: 'block', margin: '60px auto' }} />}

      {graph && !loading && (
        <Card title="知识图谱结构">
          <h4>知识点（{graph.nodes?.length || 0}个）</h4>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 24 }}>
            {(graph.nodes || []).map((node: any) => (
              <Tag key={node.id} color="blue" style={{ padding: '4px 12px', fontSize: 14 }}>
                {node.name} <span style={{ fontSize: 12, color: '#999' }}>(难度:{node.difficultyLevel})</span>
              </Tag>
            ))}
          </div>
          <h4>依赖关系（{graph.edges?.length || 0}条）</h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            {(graph.edges || []).map((edge: any, i: number) => {
              const source = graph.nodes?.find((n: any) => n.id === edge.source)
              const target = graph.nodes?.find((n: any) => n.id === edge.target)
              return (
                <div key={i} style={{ fontSize: 14 }}>
                  <Tag color="green">{source?.name || edge.source}</Tag>
                  → <Tag color="orange">{target?.name || edge.target}</Tag>
                  <Tag>{edge.type}</Tag>
                </div>
              )
            })}
          </div>
        </Card>
      )}

      {!graph && !loading && <Empty description="输入课程ID加载知识图谱" />}
    </div>
  )
}
