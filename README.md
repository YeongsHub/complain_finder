# FindComplain - Reddit Complaint Analyzer

Reddit에서 사용자들의 불평/불만을 수집하여 LLM으로 분석하고, 이를 기반으로 비즈니스 아이디어를 제안하는 시장 조사 앱

## 프로젝트 구조

```
find_complain/
├── backend/          # Spring Boot 백엔드
├── frontend/         # React + TypeScript 프론트엔드
├── docker-compose.yml
└── init.sql
```

## 빠른 시작

### 1. PostgreSQL 실행

```bash
docker-compose up -d
```

### 2. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

백엔드는 http://localhost:8080 에서 실행됩니다.
Swagger UI: http://localhost:8080/swagger-ui.html

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드는 http://localhost:5173 에서 실행됩니다.

## 환경 설정

### Reddit API (선택사항)

실제 Reddit 데이터를 사용하려면:
1. https://www.reddit.com/prefs/apps 에서 앱 생성 ("script" 타입)
2. 환경 변수 설정:

```bash
export REDDIT_CLIENT_ID=your_client_id
export REDDIT_CLIENT_SECRET=your_client_secret
export REDDIT_MOCK_MODE=false
```

### LLM API (선택사항)

실제 LLM 분석을 사용하려면:

```bash
export LLM_API_KEY=your_openai_api_key
export LLM_MODEL=gpt-4o-mini
export LLM_MOCK_MODE=false
```

## 주요 기능

1. **서브레딧 분석**: r/programming, r/webdev 등 원하는 서브레딧 분석
2. **불평 추출**: LLM을 사용하여 게시글에서 불평/불만 자동 추출
3. **카테고리 분류**: 가격, UX, 기능부족, 버그, 서비스 등으로 분류
4. **아이디어 생성**: 불평 패턴을 분석하여 비즈니스 아이디어 자동 생성
5. **대시보드**: 분석 결과 시각화

## API 엔드포인트

- `POST /api/analyze` - 분석 시작
- `GET /api/analyze/{sessionId}/status` - 분석 상태 조회
- `GET /api/complaints` - 불평 목록 조회
- `GET /api/ideas` - 아이디어 목록 조회
- `GET /api/dashboard/stats` - 대시보드 통계

## Mock 모드

API 키 없이도 테스트할 수 있도록 Mock 모드가 기본으로 활성화되어 있습니다.
Mock 데이터로 전체 워크플로우를 테스트해볼 수 있습니다.
