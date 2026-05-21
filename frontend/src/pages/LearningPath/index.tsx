import { useEffect, useState } from 'react'
import { Card, Steps, Button, Empty, Spin, Tag, message } from 'antd'
import { getUserPaths, advanceStep } from '../../api/path'

export default function LearningPath() {
  const [paths, setPaths] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  const fetchPaths = async () => {
    setLoading(true)
    try {
      const res = await getUserPaths() as any
      setPaths(res.data || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPaths() }, [])

  const handleAdvance = async (pathId: number) => {
    await advanceStep(pathId)
    message.success('已推进到下一步')
    fetchPaths()
  }

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />

  return (
    <div>
      <h2>学习路径</h2>
      {paths.length === 0 ? (
        <Empty description="暂无学习路径，选择课程后系统将自动生成" />
      ) : (
        paths.map((path: any) => {
          let pathSteps: any[] = []
          try {
            const data = JSON.parse(path.pathData || '{}')
            pathSteps = data.steps || []
          } catch { /* ignore */ }

          return (
            <Card
              key={path.id}
              title={`课程路径 #${path.id}`}
              extra={
                <Tag color={path.status === 'ACTIVE' ? 'green' : path.status === 'COMPLETED' ? 'blue' : 'default'}>
                  {path.status}
                </Tag>
              }
              style={{ marginBottom: 16 }}
            >
              <p>进度：{path.currentStep} / {path.totalSteps} 步</p>
              {pathSteps.length > 0 ? (
                <Steps
                  direction="vertical"
                  current={path.currentStep}
                  items={pathSteps.map((step: any, i: number) => ({
                    title: step.name || `步骤 ${i + 1}`,
                    description: (
                      <div>
                        <span>难度: {step.difficulty || '-'}</span>
                        {step.estimatedHours && <span> · 预计 {step.estimatedHours}h</span>}
                        {step.reason && <p style={{ color: '#666', margin: '4px 0 0' }}>{step.reason}</p>}
                      </div>
                    ),
                  }))}
                />
              ) : (
                <p style={{ color: '#999' }}>路径数据解析中...</p>
              )}
              {path.status === 'ACTIVE' && (
                <Button type="primary" onClick={() => handleAdvance(path.id)} style={{ marginTop: 12 }}>
                  完成当前步骤，推进下一步
                </Button>
              )}
            </Card>
          )
        })
      )}
    </div>
  )
}
