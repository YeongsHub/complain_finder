export interface Complaint {
  id: number
  redditPostId: string
  subreddit: string
  title: string
  content: string
  author: string
  score: number
  createdAt: string
  category: string
  painLevel: number
  extractedProblem: string
  analyzedAt: string
}

export interface BusinessIdea {
  id: number
  title: string
  problemStatement: string
  solution: string
  targetMarket: string
  difficulty: 'easy' | 'medium' | 'hard'
  potentialScore: number
  sourceComplaints: number[]
  createdAt: string
}

export interface AnalyzeRequest {
  subreddit: string
  keywords?: string[]
  limit?: number
}

export interface AnalyzeResponse {
  sessionId: number
  subreddit: string
  status: AnalysisStatus
  message: string
}

export type AnalysisStatus =
  | 'PENDING'
  | 'COLLECTING'
  | 'ANALYZING'
  | 'GENERATING_IDEAS'
  | 'COMPLETED'
  | 'FAILED'

export interface DashboardStats {
  totalComplaints: number
  totalIdeas: number
  totalSubreddits: number
  categoryDistribution: Record<string, number>
  recentActivities: RecentActivity[]
}

export interface RecentActivity {
  type: 'complaint' | 'idea'
  description: string
  timestamp: string
}
