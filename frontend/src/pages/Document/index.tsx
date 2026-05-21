import { useEffect, useState } from 'react'
import { Card, Table, Upload, Button, Tag, message, Popconfirm } from 'antd'
import { UploadOutlined, DeleteOutlined } from '@ant-design/icons'
import { getDocuments, uploadDocument, deleteDocument } from '../../api/document'

export default function Document() {
  const [documents, setDocuments] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)

  const fetchDocuments = async () => {
    setLoading(true)
    try {
      const res = await getDocuments({ page: 0, size: 50 }) as any
      setDocuments(res.data.content || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchDocuments() }, [])

  const handleUpload = async (file: File) => {
    setUploading(true)
    try {
      await uploadDocument(file)
      message.success('上传成功，正在处理...')
      fetchDocuments()
    } catch {
      message.error('上传失败')
    } finally {
      setUploading(false)
    }
    return false
  }

  const handleDelete = async (id: number) => {
    await deleteDocument(id)
    message.success('已删除')
    fetchDocuments()
  }

  const statusColor: Record<string, string> = {
    UPLOADING: 'blue',
    PARSING: 'orange',
    VECTORIZING: 'orange',
    COMPLETED: 'green',
    FAILED: 'red',
  }

  const columns = [
    { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
    { title: '类型', dataIndex: 'fileType', key: 'fileType', render: (t: string) => <Tag>{t}</Tag> },
    { title: '分块数', dataIndex: 'totalChunks', key: 'totalChunks' },
    {
      title: '状态', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={statusColor[s]}>{s}</Tag>,
    },
    {
      title: '上传时间', dataIndex: 'createdAt', key: 'createdAt',
      render: (t: string) => new Date(t).toLocaleString(),
    },
    {
      title: '操作', key: 'action',
      render: (_: any, record: any) => (
        <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
          <Button icon={<DeleteOutlined />} danger size="small" />
        </Popconfirm>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2>个人知识库</h2>
        <Upload beforeUpload={handleUpload} showUploadList={false} accept=".pdf,.docx,.doc,.txt,.md,.jpg,.png">
          <Button type="primary" icon={<UploadOutlined />} loading={uploading}>
            上传文档
          </Button>
        </Upload>
      </div>
      <Card>
        <p style={{ color: '#666', marginBottom: 16 }}>
          支持上传 PDF、Word、TXT、Markdown、图片文件，系统将自动解析并建立向量索引，可用于AI对话中的个人知识库检索。
        </p>
        <Table
          columns={columns}
          dataSource={documents}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  )
}
