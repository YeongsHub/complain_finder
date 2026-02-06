import { useEffect, useState } from 'react'
import { ideaApi } from '../services/api'
import type { BusinessIdea } from '../types'
import IdeaCard from './IdeaCard'

export default function IdeaList() {
  const [ideas, setIdeas] = useState<BusinessIdea[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<string>('')

  useEffect(() => {
    const fetchIdeas = async () => {
      setLoading(true)
      try {
        const data = await ideaApi.getAll(filter || undefined)
        setIdeas(data)
      } catch (err) {
        console.error('Failed to fetch ideas', err)
      } finally {
        setLoading(false)
      }
    }
    fetchIdeas()
  }, [filter])

  return (
    <div className="space-y-6">
      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-4">
        <div className="flex flex-wrap gap-4 items-center">
          <label className="text-sm font-medium text-gray-700">Filter by difficulty:</label>
          <div className="flex gap-2">
            {['', 'easy', 'medium', 'hard'].map((difficulty) => (
              <button
                key={difficulty}
                onClick={() => setFilter(difficulty)}
                className={`px-4 py-2 rounded-full text-sm font-medium ${
                  filter === difficulty
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {difficulty === '' ? 'All' : difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Results Count */}
      <div className="text-sm text-gray-600">
        Showing {ideas.length} business idea{ideas.length !== 1 ? 's' : ''}
      </div>

      {/* Loading State */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      ) : ideas.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
          No business ideas found. Analyze a subreddit to generate ideas!
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {ideas.map((idea) => (
            <IdeaCard key={idea.id} idea={idea} />
          ))}
        </div>
      )}
    </div>
  )
}
