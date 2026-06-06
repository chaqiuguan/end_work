export const meta = {
  name: 'frontend-review',
  description: 'Vue 3 frontend code review — check component design, performance, accessibility, and best practices',
  phases: [
    { title: 'Review', detail: 'Multi-dimensional frontend code review' },
    { title: 'Score', detail: 'Score and summarize findings' },
  ],
}

// ============================================================
// 前端代码审查 — 针对 Vue 3 + Element Plus 项目的专业审查
// 审查维度：组件设计、性能、可访问性、安全性、最佳实践
// ============================================================

phase('Review')

const files = args?.files || ['frontend/src/**/*.vue', 'frontend/src/**/*.js']

const DIMENSIONS = [
  {
    key: 'component-design',
    label: '组件设计',
    prompt: `Review Vue 3 component design:
- Are components properly split (single responsibility)?
- Are props and emits well-defined and documented?
- Is Composition API used correctly (<script setup>, composables)?
- Are computed properties used instead of methods where appropriate?
- Is reactive state minimized and well-structured?
- Are there any components that are too large (>300 lines)?
Find specific issues with file paths and line references.`,
  },
  {
    key: 'performance',
    label: '性能优化',
    prompt: `Review frontend performance:
- Are large lists using virtual scrolling or pagination?
- Are images lazy-loaded?
- Are there unnecessary re-renders or computed re-evaluations?
- Is route-level code splitting used (dynamic imports)?
- Are there memory leaks (unremoved event listeners, timers)?
- Are API calls properly debounced/throttled?
- Is the bundle size reasonable? Any large dependencies that could be tree-shaken?
Find specific issues with file paths and line references.`,
  },
  {
    key: 'accessibility',
    label: '可访问性',
    prompt: `Review web accessibility (a11y):
- Are form inputs properly labeled?
- Are there sufficient color contrasts?
- Is the page navigable by keyboard?
- Are ARIA attributes used where needed?
- Are images missing alt text?
- Is the focus order logical?
Find specific issues with file paths and line references.`,
  },
  {
    key: 'security',
    label: '安全性',
    prompt: `Review frontend security:
- Is user input sanitized before rendering (XSS prevention)?
- Are tokens stored securely?
- Is sensitive data exposed in client-side code?
- Are there any unsafe innerHTML/v-html usages?
- Are third-party scripts used securely?
- Is HTTPS enforced?
Find specific issues with file paths and line references.`,
  },
  {
    key: 'best-practices',
    label: '最佳实践',
    prompt: `Review Vue 3 and Element Plus best practices:
- Are Vue 3 reactivity rules followed?
- Are Element Plus components used idiomatically?
- Is error handling consistent (API calls, user feedback)?
- Are loading/empty/error states handled for every data-fetching component?
- Is the Pinia store usage correct (no direct mutations)?
- Are there magic numbers or hardcoded strings that should be constants?
- Is the project structure clean and conventional?
Find specific issues with file paths and line references.`,
  },
]

const results = await pipeline(
  DIMENSIONS,
  d => agent(d.prompt, { label: `review:${d.key}`, phase: 'Review', schema: {
    type: 'object',
    properties: {
      dimension: { type: 'string' },
      issues: {
        type: 'array',
        items: {
          type: 'object',
          properties: {
            severity: { type: 'string', enum: ['critical', 'major', 'minor', 'suggestion'] },
            file: { type: 'string' },
            line: { type: 'string' },
            title: { type: 'string' },
            description: { type: 'string' },
            recommendation: { type: 'string' },
          },
          required: ['severity', 'title', 'description', 'recommendation'],
        },
      },
    },
    required: ['dimension', 'issues'],
  } }),
  // Verify each finding
  review => parallel((review?.issues || []).map(f => () =>
    agent(
      `Verify this finding. Is it a real issue or a false positive?

Finding: ${f.title}
File: ${f.file || 'unknown'}
Description: ${f.description}

Respond with: isReal (true/false) and confidence (high/medium/low). If false positive, explain why.`,
      { label: `verify:${f.title?.slice(0, 40)}`, phase: 'Review', schema: {
        type: 'object',
        properties: {
          isReal: { type: 'boolean' },
          confidence: { type: 'string', enum: ['high', 'medium', 'low'] },
          explanation: { type: 'string' },
        },
        required: ['isReal', 'confidence', 'explanation'],
      } }
    ).then(v => ({ ...f, verified: v }))
  )).then(verified => ({
    dimension: review?.dimension,
    issues: (verified || []).filter(Boolean).filter(v => v.verified?.isReal !== false),
  }))
)

phase('Score')

const allIssues = results.filter(Boolean).flatMap(r => r.issues || [])
const critical = allIssues.filter(i => i.severity === 'critical').length
const major = allIssues.filter(i => i.severity === 'major').length
const minor = allIssues.filter(i => i.severity === 'minor').length
const suggestion = allIssues.filter(i => i.severity === 'suggestion').length

const score = Math.max(0, 100 - critical * 20 - major * 10 - minor * 3 - suggestion * 1)

return {
  score,
  grade: score >= 90 ? 'A' : score >= 75 ? 'B' : score >= 60 ? 'C' : 'D',
  summary: `总分: ${score}/100 (${critical}严重, ${major}重要, ${minor}次要, ${suggestion}建议)`,
  dimensions: results.filter(Boolean).map(r => ({
    dimension: r.dimension,
    issueCount: (r.issues || []).length,
    issues: r.issues,
  })),
  topIssues: allIssues
    .sort((a, b) => ['critical','major','minor','suggestion'].indexOf(a.severity) - ['critical','major','minor','suggestion'].indexOf(b.severity))
    .slice(0, 10),
}
