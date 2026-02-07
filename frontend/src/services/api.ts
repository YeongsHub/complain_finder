import axios from 'axios'
import type { AppIdea } from '../types/appIdea'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const appIdeaApi = {
  getAll: (): Promise<AppIdea[]> =>
    api.get('/app-ideas').then(res => res.data),

  getTop: (): Promise<AppIdea[]> =>
    api.get('/app-ideas/top').then(res => res.data),

  getBookmarked: (): Promise<AppIdea[]> =>
    api.get('/app-ideas/bookmarked').then(res => res.data),

  toggleBookmark: (id: number): Promise<AppIdea> =>
    api.post(`/app-ideas/${id}/bookmark`).then(res => res.data),

  discover: (): Promise<{ message: string; newIdeasFound: number; subredditsScanned: string[] }> =>
    api.post('/app-ideas/discover').then(res => res.data),

  analyzeSubreddit: (subreddit: string, limit?: number): Promise<AppIdea[]> =>
    api.post(`/app-ideas/analyze/${subreddit}?limit=${limit || 20}`).then(res => res.data),

  getTargetSubreddits: (): Promise<string[]> =>
    api.get('/app-ideas/subreddits').then(res => res.data),
}
