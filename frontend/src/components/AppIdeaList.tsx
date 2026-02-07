import { useEffect, useState } from 'react'
import { appIdeaApi } from '../services/api'
import type { AppIdea } from '../types/appIdea'

const DIFFICULTY_STYLES = {
  easy: 'bg-green-100 text-green-800',
  medium: 'bg-yellow-100 text-yellow-800',
  hard: 'bg-red-100 text-red-800',
}

export default function AppIdeaList() {
  const [ideas, setIdeas] = useState<AppIdea[]>([])
  const [loading, setLoading] = useState(true)
  const [discovering, setDiscovering] = useState(false)
  const [filter, setFilter] = useState<'all' | 'bookmarked'>('all')
  const [subreddits, setSubreddits] = useState<string[]>([])

  useEffect(() => {
    fetchIdeas()
    fetchSubreddits()
  }, [filter])

  const fetchIdeas = async () => {
    setLoading(true)
    try {
      const data = filter === 'bookmarked'
        ? await appIdeaApi.getBookmarked()
        : await appIdeaApi.getAll()
      setIdeas(data)
    } catch (err) {
      console.error('Failed to fetch ideas', err)
    } finally {
      setLoading(false)
    }
  }

  const fetchSubreddits = async () => {
    try {
      const data = await appIdeaApi.getTargetSubreddits()
      setSubreddits(data)
    } catch (err) {
      console.error('Failed to fetch subreddits', err)
    }
  }

  const handleDiscover = async () => {
    setDiscovering(true)
    try {
      const result = await appIdeaApi.discover()
      alert(`Discovery complete! Found ${result.newIdeasFound} new ideas.`)
      fetchIdeas()
    } catch (err) {
      console.error('Discovery failed', err)
      alert('Discovery failed. Check console for details.')
    } finally {
      setDiscovering(false)
    }
  }

  const handleToggleBookmark = async (id: number) => {
    try {
      const updated = await appIdeaApi.toggleBookmark(id)
      setIdeas(ideas.map(i => i.id === id ? updated : i))
    } catch (err) {
      console.error('Failed to toggle bookmark', err)
    }
  }

  const getRedditUrl = (idea: AppIdea) => {
    return `https://www.reddit.com/r/${idea.subreddit}/comments/${idea.redditPostId}/`
  }

  const getViabilityStars = (score: number) => {
    return Array.from({ length: 10 }, (_, i) => (
      <div
        key={i}
        className={`h-2 w-2 rounded-full ${i < score ? 'bg-yellow-400' : 'bg-gray-200'}`}
      />
    ))
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-gray-900">App Idea Discovery</h2>
            <p className="text-gray-600 text-sm mt-1">
              Scanning: {subreddits.join(', ')}
            </p>
          </div>
          <div className="flex gap-3">
            <div className="flex rounded-lg overflow-hidden border">
              <button
                onClick={() => setFilter('all')}
                className={`px-4 py-2 text-sm ${filter === 'all' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700'}`}
              >
                All Ideas
              </button>
              <button
                onClick={() => setFilter('bookmarked')}
                className={`px-4 py-2 text-sm ${filter === 'bookmarked' ? 'bg-blue-600 text-white' : 'bg-white text-gray-700'}`}
              >
                Bookmarked
              </button>
            </div>
            <button
              onClick={handleDiscover}
              disabled={discovering}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 flex items-center gap-2"
            >
              {discovering ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Scanning...
                </>
              ) : (
                <>
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                  Discover Now
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Results */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      ) : ideas.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
          No app ideas found. Click "Discover Now" to scan subreddits!
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {ideas.map((idea) => (
            <div
              key={idea.id}
              className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow"
            >
              <div className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-sm text-blue-600 font-medium">r/{idea.subreddit}</span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${DIFFICULTY_STYLES[idea.difficulty] || DIFFICULTY_STYLES.medium}`}>
                        {idea.difficulty}
                      </span>
                      <span className="text-xs text-gray-500">by u/{idea.author}</span>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900">{idea.appName}</h3>
                  </div>
                  <button
                    onClick={() => handleToggleBookmark(idea.id)}
                    className={`p-2 rounded-full ${idea.bookmarked ? 'text-yellow-500' : 'text-gray-300 hover:text-yellow-500'}`}
                  >
                    <svg className="w-6 h-6" fill={idea.bookmarked ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                    </svg>
                  </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div className="bg-gray-50 rounded-lg p-4">
                    <h4 className="text-sm font-semibold text-gray-700 mb-2">Problem</h4>
                    <p className="text-gray-600 text-sm">{idea.problemSummary}</p>
                  </div>
                  <div className="bg-blue-50 rounded-lg p-4">
                    <h4 className="text-sm font-semibold text-gray-700 mb-2">Proposed Solution</h4>
                    <p className="text-gray-600 text-sm">{idea.proposedSolution}</p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">Target Users</h4>
                    <p className="text-gray-600 text-sm">{idea.targetUsers}</p>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">Key Features</h4>
                    <p className="text-gray-600 text-sm">{idea.keyFeatures}</p>
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">Tech Stack</h4>
                    <p className="text-gray-600 text-sm">{idea.techStack}</p>
                  </div>
                </div>

                <div className="bg-yellow-50 border-l-4 border-yellow-400 p-3 mb-4">
                  <p className="text-sm text-yellow-800">
                    <strong>Why this idea?</strong> {idea.reasoning}
                  </p>
                </div>

                <div className="flex items-center justify-between pt-4 border-t">
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">Viability Score:</span>
                    <div className="flex gap-0.5">{getViabilityStars(idea.viabilityScore)}</div>
                    <span className="text-sm font-bold text-gray-700">{idea.viabilityScore}/10</span>
                  </div>
                  <a
                    href={getRedditUrl(idea)}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-blue-600 hover:underline flex items-center gap-1"
                  >
                    View Original Post
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
