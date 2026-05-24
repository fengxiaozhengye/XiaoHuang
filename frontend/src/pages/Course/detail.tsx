import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Spin, Empty, Tag, Button, Descriptions } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { getCourse, getKnowledgeGraph } from '../../api/course'

export default function CourseDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [course, setCourse] = useState<any>(null)
  const [graph, setGraph] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    const fetchData = async () => {
      setLoading(true)
      try {
        const [courseRes, graphRes] = await Promise.all([
          getCourse(Number(id)),
          getKnowledgeGraph(Number(id)),
        ])
        setCourse((courseRes as any).data)
        setGraph((graphRes as any).data)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  if (!course) return <Empty description="课程不存在" />

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/course')} style={{ marginBottom: 16 }}>
        返回课程列表
      </Button>

      <Card title={course.name} style={{ marginBottom: 16 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="学科领域">{course.subjectArea || '未分类'}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={course.status === 'PUBLISHED' ? 'green' : 'default'}>{course.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="描述" span={2}>{course.description || '暂无描述'}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title={`知识图谱（${graph?.nodes?.length || 0} 个知识点，${graph?.edges?.length || 0} 条依赖关系）`}>
        {graph?.nodes?.length > 0 ? (
          <>
            <h4>知识点</h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 24 }}>
              {graph.nodes.map((node: any) => (
                <Tag key={node.id} color="blue" style={{ padding: '4px 12px', fontSize: 14 }}>
                  {node.name} <span style={{ fontSize: 12, color: '#999' }}>(难度:{node.difficultyLevel})</span>
                </Tag>
              ))}
            </div>
            <h4>依赖关系</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {graph.edges?.map((edge: any, i: number) => {
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
          </>
        ) : (
          <Empty description="暂无知识点，请先导入" />
        )}
      </Card>
    </div>
  )
}
