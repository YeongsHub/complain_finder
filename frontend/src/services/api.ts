import axios from 'axios'
import type {
  Complaint,
  BusinessIdea,
  AnalyzeRequest,
  AnalyzeResponse,
  DashboardStats
} from '../types'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const analyzeApi = {
  start: (request: AnalyzeRequest): Promise<AnalyzeResponse> =>
    api.post('/analyze', request).then(res => res.data),

  getStatus: (sessionId: number): Promise<AnalyzeResponse> =>
    api.get(`/analyze/${sessionId}/status`).then(res => res.data),
}

export const complaintApi = {
  getAll: (subreddit?: string, category?: string): Promise<Complaint[]> => {
    const params = new URLSearchParams()
    if (subreddit) params.append('subreddit', subreddit)
    if (category) params.append('category', category)
    return api.get(`/complaints?${params}`).then(res => res.data)
  },

  getById: (id: number): Promise<Complaint> =>
    api.get(`/complaints/${id}`).then(res => res.data),

  getSubreddits: (): Promise<string[]> =>
    api.get('/complaints/subreddits').then(res => res.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/complaints/${id}`),
}

export const ideaApi = {
  getAll: (difficulty?: string, limit?: number): Promise<BusinessIdea[]> => {
    const params = new URLSearchParams()
    if (difficulty) params.append('difficulty', difficulty)
    if (limit) params.append('limit', limit.toString())
    return api.get(`/ideas?${params}`).then(res => res.data)
  },

  getById: (id: number): Promise<BusinessIdea> =>
    api.get(`/ideas/${id}`).then(res => res.data),

  getTop: (): Promise<BusinessIdea[]> =>
    api.get('/ideas/top').then(res => res.data),

  delete: (id: number): Promise<void> =>
    api.delete(`/ideas/${id}`),
}

export const dashboardApi = {
  getStats: (): Promise<DashboardStats> =>
    api.get('/dashboard/stats').then(res => res.data),
}
