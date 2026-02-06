-- 수집된 불평
CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    reddit_post_id VARCHAR(20),
    subreddit VARCHAR(100),
    title TEXT,
    content TEXT,
    author VARCHAR(50),
    score INTEGER,
    created_at TIMESTAMP,
    -- LLM 분석 결과
    category VARCHAR(50),
    pain_level INTEGER,
    extracted_problem TEXT,
    analyzed_at TIMESTAMP
);

-- 생성된 비즈니스 아이디어
CREATE TABLE IF NOT EXISTS business_ideas (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200),
    problem_statement TEXT,
    solution TEXT,
    target_market TEXT,
    difficulty VARCHAR(20),
    potential_score INTEGER,
    source_complaints JSONB,
    created_at TIMESTAMP
);

-- 분석 세션
CREATE TABLE IF NOT EXISTS analysis_sessions (
    id BIGSERIAL PRIMARY KEY,
    subreddit VARCHAR(100),
    keywords TEXT[],
    total_posts INTEGER,
    total_complaints INTEGER,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING'
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_complaints_subreddit ON complaints(subreddit);
CREATE INDEX IF NOT EXISTS idx_complaints_category ON complaints(category);
CREATE INDEX IF NOT EXISTS idx_business_ideas_difficulty ON business_ideas(difficulty);
CREATE INDEX IF NOT EXISTS idx_analysis_sessions_status ON analysis_sessions(status);
