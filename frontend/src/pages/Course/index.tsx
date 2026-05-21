import { useEffect, useState } from 'react'
import { Card, List, Input, Tag, Space } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import { getCourses } from '../../api/course'
import { useNavigate } from 'react-router-dom'

export default function CourseList() {
  const [courses, setCourses] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()

  const fetchCourses = async (kw?: string) => {
    setLoading(true)
    try {
      const res = await getCourses({ keyword: kw, page: 0, size: 20 }) as any
      setCourses(res.data.content || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchCourses() }, [])

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2>课程中心</h2>
        <Space>
          <Input
            placeholder="搜索课程"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={() => fetchCourses(keyword)}
            style={{ width: 250 }}
          />
        </Space>
      </div>
      <List
        grid={{ gutter: 16, column: 3 }}
        loading={loading}
        dataSource={courses}
        renderItem={(course: any) => (
          <List.Item>
            <Card
              title={course.name}
              extra={<Tag color={course.status === 'PUBLISHED' ? 'green' : 'default'}>{course.status}</Tag>}
              hoverable
              onClick={() => navigate(`/course/${course.id}`)}
            >
              <p style={{ color: '#666', height: 48, overflow: 'hidden' }}>{course.description || '暂无描述'}</p>
              <p><Tag>{course.subjectArea || '未分类'}</Tag></p>
            </Card>
          </List.Item>
        )}
      />
    </div>
  )
}
