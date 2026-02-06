import { useState } from 'react'
import Dashboard from './components/Dashboard'
import SubredditSearch from './components/SubredditSearch'
import ComplaintList from './components/ComplaintList'
import IdeaList from './components/IdeaList'

type Tab = 'dashboard' | 'search' | 'complaints' | 'ideas'

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('dashboard')

  const tabs: { id: Tab; label: string }[] = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'search', label: 'Analyze Subreddit' },
    { id: 'complaints', label: 'Complaints' },
    { id: 'ideas', label: 'Business Ideas' },
  ]

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-gray-900">
            FindComplain
          </h1>
          <p className="text-gray-600 mt-1">Reddit Complaint Analyzer & Business Idea Generator</p>
        </div>
      </header>

      <nav className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex space-x-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'search' && <SubredditSearch />}
        {activeTab === 'complaints' && <ComplaintList />}
        {activeTab === 'ideas' && <IdeaList />}
      </main>
    </div>
  )
}

export default App
