import { Card, Col, Row, Statistic } from 'antd'
import { BookOutlined, ClockCircleOutlined, TrophyOutlined, FireOutlined } from '@ant-design/icons'

export default function Dashboard() {
  return (
    <div>
      <h2>学习概览</h2>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="学习课程数" value={0} prefix={<BookOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="累计学习时长(h)" value={0} prefix={<ClockCircleOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="掌握知识点" value={0} prefix={<TrophyOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="连续学习天数" value={0} prefix={<FireOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="今日推荐">
            <p style={{ color: '#999' }}>完成初始测评后，系统将为你推荐个性化学习内容</p>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="学习路径进度">
            <p style={{ color: '#999' }}>选择课程后开始你的个性化学习之旅</p>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
