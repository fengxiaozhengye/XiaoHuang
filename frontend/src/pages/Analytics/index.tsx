import { useEffect, useState } from 'react'
import { Card, Row, Col, Statistic, Spin, Empty } from 'antd'
import { ClockCircleOutlined, TrophyOutlined, BookOutlined, BarChartOutlined } from '@ant-design/icons'
import { getOverview } from '../../api/analytics'

export default function Analytics() {
  const [overview, setOverview] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getOverview()
      .then((res: any) => setOverview(res.data))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  if (!overview) return <Empty description="暂无学习数据" />

  return (
    <div>
      <h2>学习分析</h2>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="累计学习时长"
              value={overview.totalDurationMinutes || 0}
              suffix="分钟"
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已完成知识点"
              value={overview.completedKnowledgePoints || 0}
              prefix={<TrophyOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="进行中路径"
              value={overview.activePaths || 0}
              prefix={<BookOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="学习记录数"
              value={overview.totalRecords || 0}
              prefix={<BarChartOutlined />}
            />
          </Card>
        </Col>
      </Row>
      <Card title="学习趋势">
        <Empty description="积累更多学习数据后展示趋势图表" />
      </Card>
    </div>
  )
}
