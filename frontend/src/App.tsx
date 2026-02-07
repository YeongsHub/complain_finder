import AppIdeaList from './components/AppIdeaList'

function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-gradient-to-r from-blue-600 to-purple-600 shadow">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <h1 className="text-3xl font-bold text-white">
            IdeaHunter
          </h1>
          <p className="text-blue-100 mt-1">Discover App Ideas from Reddit - Daily Automated Scanning</p>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <AppIdeaList />
      </main>
    </div>
  )
}

export default App
