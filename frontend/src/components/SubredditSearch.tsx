import { useState, useEffect, useCallback } from 'react'
import { analyzeApi } from '../services/api'
import type { AnalyzeResponse, AnalysisStatus } from '../types'

const STATUS_MESSAGES: Record<AnalysisStatus, { text: string; color: string }> = {
  PENDING: { text: 'Waiting to start...', color: 'text-gray-500' },
  COLLECTING: { text: 'Collecting Reddit posts...', color: 'text-blue-500' },
  ANALYZING: { text: 'Analyzing complaints with AI...', color: 'text-purple-500' },
  GENERATING_IDEAS: { text: 'Generating business ideas...', color: 'text-green-500' },
  COMPLETED: { text: 'Analysis complete!', color: 'text-green-600' },
  FAILED: { text: 'Analysis failed', color: 'text-red-500' },
}

export default function SubredditSearch() {
  const [subreddit, setSubreddit] = useState('')
  const [keywords, setKeywords] = useState('')
  const [limit, setLimit] = useState(50)
  const [loading, setLoading] = useState(false)
  const [session, setSession] = useState<AnalyzeResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  const pollStatus = useCallback(async (sessionId: number) => {
    try {
      const status = await analyzeApi.getStatus(sessionId)
      setSession(status)

      if (status.status !== 'COMPLETED' && status.status !== 'FAILED') {
        setTimeout(() => pollStatus(sessionId), 2000)
      }
    } catch (err) {
      setError('Failed to get status')
    }
  }, [])

  useEffect(() => {
    if (session?.sessionId && session.status !== 'COMPLETED' && session.status !== 'FAILED') {
      const timeoutId = setTimeout(() => pollStatus(session.sessionId), 2000)
      return () => clearTimeout(timeoutId)
    }
  }, [session, pollStatus])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    setSession(null)

    try {
      const response = await analyzeApi.start({
        subreddit: subreddit.replace('r/', ''),
        keywords: keywords ? keywords.split(',').map(k => k.trim()) : undefined,
        limit,
      })
      setSession(response)
    } catch (err) {
      setError('Failed to start analysis')
    } finally {
      setLoading(false)
    }
  }

  const statusInfo = session ? STATUS_MESSAGES[session.status] : null

  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Analyze Subreddit</h2>
        <p className="text-gray-600 mb-6">
          Enter a subreddit to analyze complaints and generate business ideas.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="subreddit" className="block text-sm font-medium text-gray-700">
              Subreddit
            </label>
            <div className="mt-1 relative rounded-md shadow-sm">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-500">
                r/
              </span>
              <input
                type="text"
                id="subreddit"
                value={subreddit}
                onChange={(e) => setSubreddit(e.target.value)}
                className="block w-full pl-8 pr-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                placeholder="programming"
                required
              />
            </div>
          </div>

          <div>
            <label htmlFor="keywords" className="block text-sm font-medium text-gray-700">
              Keywords (optional)
            </label>
            <input
              type="text"
              id="keywords"
              value={keywords}
              onChange={(e) => setKeywords(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
              placeholder="frustrating, hate, terrible (comma-separated)"
            />
          </div>

          <div>
            <label htmlFor="limit" className="block text-sm font-medium text-gray-700">
              Number of posts to analyze
            </label>
            <select
              id="limit"
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value))}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
            >
              <option value={25}>25 posts</option>
              <option value={50}>50 posts</option>
              <option value={100}>100 posts</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={loading || (session?.status !== undefined &&
              session.status !== 'COMPLETED' &&
              session.status !== 'FAILED')}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {loading ? 'Starting...' : 'Start Analysis'}
          </button>
        </form>

        {error && (
          <div className="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            {error}
          </div>
        )}

        {session && (
          <div className="mt-6 border-t pt-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Analysis Progress</h3>

            <div className="space-y-4">
              <div className="flex items-center space-x-3">
                {session.status !== 'COMPLETED' && session.status !== 'FAILED' && (
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500"></div>
                )}
                {session.status === 'COMPLETED' && (
                  <div className="h-5 w-5 bg-green-500 rounded-full flex items-center justify-center">
                    <svg className="h-3 w-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                )}
                {session.status === 'FAILED' && (
                  <div className="h-5 w-5 bg-red-500 rounded-full flex items-center justify-center">
                    <svg className="h-3 w-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                )}
                <span className={`text-sm font-medium ${statusInfo?.color}`}>
                  {statusInfo?.text}
                </span>
              </div>

              {session.message && (
                <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                  {session.message}
                </p>
              )}

              {session.status === 'COMPLETED' && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <p className="text-green-700">
                    Analysis complete! Check the Complaints and Business Ideas tabs to see the results.
                  </p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Popular Subreddits */}
      <div className="mt-6 bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-3">Popular Subreddits to Analyze</h3>
        <div className="flex flex-wrap gap-2">
          {['programming', 'webdev', 'startup', 'entrepreneur', 'technology', 'software', 'SaaS'].map(
            (sub) => (
              <button
                key={sub}
                onClick={() => setSubreddit(sub)}
                className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full text-sm text-gray-700"
              >
                r/{sub}
              </button>
            )
          )}
        </div>
      </div>
    </div>
  )
}
