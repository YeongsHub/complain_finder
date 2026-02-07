export interface AppIdea {
  id: number
  redditPostId: string
  subreddit: string
  originalTitle: string
  originalContent: string
  author: string
  score: number
  appName: string
  problemSummary: string
  proposedSolution: string
  targetUsers: string
  keyFeatures: string
  techStack: string
  difficulty: 'easy' | 'medium' | 'hard'
  viabilityScore: number
  reasoning: string
  bookmarked: boolean
  redditCreatedAt: string
  analyzedAt: string
}
