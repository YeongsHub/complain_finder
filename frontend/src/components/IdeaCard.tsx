import type { BusinessIdea } from '../types'

interface IdeaCardProps {
  idea: BusinessIdea
}

const DIFFICULTY_STYLES = {
  easy: 'bg-green-100 text-green-800',
  medium: 'bg-yellow-100 text-yellow-800',
  hard: 'bg-red-100 text-red-800',
}

export default function IdeaCard({ idea }: IdeaCardProps) {
  const getPotentialStars = (score: number) => {
    return Array.from({ length: 10 }, (_, i) => (
      <svg
        key={i}
        className={`h-4 w-4 ${i < score ? 'text-yellow-400' : 'text-gray-200'}`}
        fill="currentColor"
        viewBox="0 0 20 20"
      >
        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
      </svg>
    ))
  }

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow">
      <div className="p-6">
        <div className="flex items-start justify-between mb-4">
          <h3 className="text-xl font-bold text-gray-900 flex-1">{idea.title}</h3>
          <span
            className={`ml-2 px-3 py-1 rounded-full text-xs font-medium ${
              DIFFICULTY_STYLES[idea.difficulty] || DIFFICULTY_STYLES.medium
            }`}
          >
            {idea.difficulty}
          </span>
        </div>

        <div className="space-y-4">
          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-1">Problem</h4>
            <p className="text-gray-600 text-sm">{idea.problemStatement}</p>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-1">Solution</h4>
            <p className="text-gray-600 text-sm">{idea.solution}</p>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-gray-700 mb-1">Target Market</h4>
            <p className="text-gray-600 text-sm">{idea.targetMarket}</p>
          </div>
        </div>

        <div className="mt-6 pt-4 border-t">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm text-gray-500">Potential Score</span>
              <div className="flex mt-1">{getPotentialStars(idea.potentialScore)}</div>
            </div>
            <div className="text-right">
              <span className="text-sm text-gray-500">Based on</span>
              <p className="text-sm font-medium text-blue-600">
                {idea.sourceComplaints?.length || 0} complaints
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-gray-50 px-6 py-3">
        <p className="text-xs text-gray-500">
          Generated: {new Date(idea.createdAt).toLocaleDateString()}
        </p>
      </div>
    </div>
  )
}
