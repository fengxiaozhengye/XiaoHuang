import { useState } from 'react'
import { Card, Select, InputNumber, Button, Space, Empty, Spin, Tag } from 'antd'
import { ThunderboltOutlined } from '@ant-design/icons'
import { generateResource } from '../../api/resource'
import MarkdownRenderer from '../../components/MarkdownRenderer'
import { RESOURCE_TYPES } from '../../utils/constants'

export default function Resource() {
  const [kpId, setKpId] = useState<number | null>(null)
  const [type, setType] = useState('TEXT')
  const [difficulty, setDifficulty] = useState(3)
  const [resource, setResource] = useState<any>(null)
  const [loading, setLoading] = useState(false)

  const handleGenerate = async () => {
    if (!kpId) return
    setLoading(true)
    try {
      const res = await generateResource({
        knowledgePointId: kpId,
        type,
        difficulty,
      }) as any
      setResource(res.data)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2>AI资源生成</h2>
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <InputNumber
            placeholder="知识点ID"
            value={kpId}
            onChange={(v) => setKpId(v)}
            style={{ width: 150 }}
          />
          <Select value={type} onChange={setType} style={{ width: 150 }}>
            {Object.entries(RESOURCE_TYPES).map(([k, v]) => (
              <Select.Option key={k} value={k}>{v}</Select.Option>
            ))}
          </Select>
          <Select value={difficulty} onChange={setDifficulty} style={{ width: 120 }}>
            {[1, 2, 3, 4, 5].map((d) => (
              <Select.Option key={d} value={d}>难度 {d}</Select.Option>
            ))}
          </Select>
          <Button
            type="primary"
            icon={<ThunderboltOutlined />}
            onClick={handleGenerate}
            loading={loading}
            disabled={!kpId}
          >
            生成资源
          </Button>
        </Space>
      </Card>

      {loading && <Spin size="large" style={{ display: 'block', margin: '60px auto' }} />}

      {resource && !loading && (
        <Card title={resource.title} extra={<Tag color="blue">{RESOURCE_TYPES[resource.type as keyof typeof RESOURCE_TYPES]}</Tag>}>
          <MarkdownRenderer content={resource.content || ''} />
        </Card>
      )}

      {!resource && !loading && <Empty description="选择知识点并点击生成" />}
    </div>
  )
}
