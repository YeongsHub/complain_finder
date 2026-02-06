import { useEffect, useState } from 'react'
import { complaintApi } from '../services/api'
import type { Complaint } from '../types'

const CATEGORY_COLORS: Record<string, string> = {
  '가격': 'bg-red-100 text-red-800',
  'UX': 'bg-purple-100 text-purple-800',
  '기능부족': 'bg-yellow-100 text-yellow-800',
  '버그': 'bg-orange-100 text-orange-800',
  '서비스': 'bg-blue-100 text-blue-800',
  '기타': 'bg-gray-100 text-gray-800',
}

export default function ComplaintList() {
  const [complaints, setComplaints] = useState<Complaint[]>([])
  const [subreddits, setSubreddits] = useState<string[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedSubreddit, setSelectedSubreddit] = useState<string>('')
  const [selectedCategory, setSelectedCategory] = useState<string>('')

  useEffect(() => {
    const fetchSubreddits = async () => {
      try {
        const data = await complaintApi.getSubreddits()
        setSubreddits(data)
      } catch (err) {
        console.error('Failed to fetch subreddits', err)
      }
    }
    fetchSubreddits()
  }, [])

  useEffect(() => {
    const fetchComplaints = async () => {
      setLoading(true)
      try {
        const data = await complaintApi.getAll(
          selectedSubreddit || undefined,
          selectedCategory || undefined
        )
        setComplaints(data)
      } catch (err) {
        console.error('Failed to fetch complaints', err)
      } finally {
        setLoading(false)
      }
    }
    fetchComplaints()
  }, [selectedSubreddit, selectedCategory])

  const categories = ['가격', 'UX', '기능부족', '버그', '서비스', '기타']

  const getPainLevelBars = (level: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <div
        key={i}
        className={`h-3 w-2 rounded-sm ${
          i < level ? 'bg-red-500' : 'bg-gray-200'
        }`}
      ></div>
    ))
  }

  const getRedditUrl = (complaint: Complaint) => {
    if (complaint.redditPostId.startsWith('mock_')) {
      return null
    }
    return `https://www.reddit.com/r/${complaint.subreddit}/comments/${complaint.redditPostId}/`
  }

  return (
    <div className="space-y-6">
      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="flex flex-wrap gap-4">
          <div className="flex-1 min-w-[200px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Subreddit
            </label>
            <select
              value={selectedSubreddit}
              onChange={(e) => setSelectedSubreddit(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">All Subreddits</option>
              {subreddits.map((sub) => (
                <option key={sub} value={sub}>
                  r/{sub}
                </option>
              ))}
            </select>
          </div>
          <div className="flex-1 min-w-[200px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Results Count */}
      <div className="text-sm text-gray-600">
        Showing {complaints.length} complaint{complaints.length !== 1 ? 's' : ''}
      </div>

      {/* Loading State */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      ) : complaints.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
          No complaints found. Start by analyzing a subreddit!
        </div>
      ) : (
        <div className="space-y-4">
          {complaints.map((complaint) => (
            <div
              key={complaint.id}
              className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-sm font-medium text-blue-600">
                      r/{complaint.subreddit}
                    </span>
                    {complaint.category && (
                      <span
                        className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          CATEGORY_COLORS[complaint.category] || CATEGORY_COLORS['기타']
                        }`}
                      >
                        {complaint.category}
                      </span>
                    )}
                    <span className="text-xs text-gray-500">
                      by u/{complaint.author}
                    </span>
                  </div>
                  {getRedditUrl(complaint) ? (
                    <a
                      href={getRedditUrl(complaint)!}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-lg font-medium text-blue-600 hover:text-blue-800 hover:underline mb-2 block"
                    >
                      {complaint.title}
                      <svg className="inline-block w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                    </a>
                  ) : (
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                      {complaint.title}
                    </h3>
                  )}
                  <p className="text-gray-600 text-sm mb-3 line-clamp-3">
                    {complaint.content}
                  </p>
                  {complaint.extractedProblem && (
                    <div className="bg-yellow-50 border-l-4 border-yellow-400 p-3 mb-3">
                      <p className="text-sm text-yellow-800">
                        <strong>Core Problem:</strong> {complaint.extractedProblem}
                      </p>
                    </div>
                  )}
                </div>
                <div className="flex flex-col items-end ml-4">
                  <div className="text-sm text-gray-500 mb-2">Pain Level</div>
                  <div className="flex gap-0.5">{getPainLevelBars(complaint.painLevel)}</div>
                  <div className="mt-2 text-sm text-gray-500">
                    Score: {complaint.score}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
