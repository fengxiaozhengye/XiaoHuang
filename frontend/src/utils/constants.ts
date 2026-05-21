export const API_BASE_URL = '/api'

export const AGENT_TYPES = {
  DIAGNOSIS: 'DIAGNOSIS',
  PLANNING: 'PLANNING',
  GENERATION: 'GENERATION',
  TUTOR: 'TUTOR',
  EVALUATION: 'EVALUATION',
} as const

export const LEARNING_STYLES = {
  VISUAL: '视觉型',
  AUDITORY: '听觉型',
  READING: '阅读型',
  KINESTHETIC: '实践型',
} as const

export const RESOURCE_TYPES = {
  TEXT: '文本讲解',
  CODE_EXAMPLE: '代码示例',
  EXERCISE: '练习题',
  MIND_MAP: '思维导图',
  VIDEO_SCRIPT: '视频脚本',
} as const
