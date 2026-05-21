import { useEffect, useState } from 'react'
import { Card, Tag, Empty, Spin, Row, Col, Progress } from 'antd'
import { getMyProfile } from '../../api/profile'
import { LEARNING_STYLES } from '../../utils/constants'

export default function Profile() {
  const [profile, setProfile] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getMyProfile()
      .then((res: any) => setProfile(res.data))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  if (!profile) return <Empty description="暂无画像数据，完成初始测评后生成" />

  let knowledgeLevel: Record<string, number> = {}
  try { knowledgeLevel = JSON.parse(profile.knowledgeLevel || '{}') } catch { /* ignore */ }
  let weakPoints: string[] = []
  try { weakPoints = JSON.parse(profile.weakPoints || '[]') } catch { /* ignore */ }
  let strongPoints: string[] = []
  try { strongPoints = JSON.parse(profile.strongPoints || '[]') } catch { /* ignore */ }

  return (
    <div>
      <h2>学生画像</h2>
      <Row gutter={16}>
        <Col span={8}>
          <Card title="学习风格">
            <Tag color="blue" style={{ fontSize: 16, padding: '4px 12px' }}>
              {LEARNING_STYLES[profile.learningStyle as keyof typeof LEARNING_STYLES] || '未评估'}
            </Tag>
          </Card>
        </Col>
        <Col span={8}>
          <Card title="薄弱环节">
            {weakPoints.length > 0
              ? weakPoints.map((p: string) => <Tag color="red" key={p}>{p}</Tag>)
              : <span style={{ color: '#999' }}>暂无数据</span>}
          </Card>
        </Col>
        <Col span={8}>
          <Card title="优势领域">
            {strongPoints.length > 0
              ? strongPoints.map((p: string) => <Tag color="green" key={p}>{p}</Tag>)
              : <span style={{ color: '#999' }}>暂无数据</span>}
          </Card>
        </Col>
      </Row>

      <Card title="知识点掌握度" style={{ marginTop: 16 }}>
        {Object.keys(knowledgeLevel).length > 0 ? (
          Object.entries(knowledgeLevel).map(([name, level]) => (
            <div key={name} style={{ marginBottom: 12 }}>
              <span style={{ display: 'inline-block', width: 120 }}>{name}</span>
              <Progress
                percent={Math.round((level as number) * 100)}
                style={{ width: 300, display: 'inline-block' }}
                status={(level as number) >= 0.8 ? 'success' : (level as number) < 0.3 ? 'exception' : 'normal'}
              />
            </div>
          ))
        ) : (
          <Empty description="暂无掌握度数据" />
        )}
      </Card>
    </div>
  )
}
