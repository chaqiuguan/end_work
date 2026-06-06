export const meta = {
  name: 'api-check',
  description: 'Verify API consistency between frontend calls and backend controllers',
  phases: [
    { title: 'Scan', detail: 'Scan frontend API modules and backend controllers' },
    { title: 'Verify', detail: 'Cross-check endpoints, params, and responses' },
    { title: 'Report', detail: 'Generate consistency report with fixes' },
  ],
}

// ============================================================
// API 一致性检查 — 对比前端 API 调用与后端 Controller
// 检查：端点路径、请求方法、参数匹配、响应格式
// ============================================================

phase('Scan')

const [frontendResult, backendResult] = await parallel([
  () => agent(
    'Scan ALL files in frontend/src/api/ directory. For each API function found, extract:\n' +
    '- Function name\n' +
    '- HTTP method (get/post/put/delete)\n' +
    '- URL path\n' +
    '- Parameters (query params, path params, request body)\n' +
    'List every function systematically.',
    { label: 'scan-frontend-api', phase: 'Scan' }
  ),
  () => agent(
    'Scan ALL Controller files in backend/src/main/java/com/zhuanbaomao/controller/ directory. For each endpoint found, extract:\n' +
    '- HTTP method annotation (@GetMapping, @PostMapping, @PutMapping, @DeleteMapping)\n' +
    '- URL path\n' +
    '- Parameters (@RequestParam, @PathVariable, @RequestBody)\n' +
    '- Return type\n' +
    'List every endpoint systematically.',
    { label: 'scan-backend-api', phase: 'Scan' }
  ),
])

phase('Verify')

const verifyResult = await agent(
  `You are an API consistency auditor. Cross-check the frontend API calls against backend endpoints.

## Frontend API Calls
${frontendResult || '(scan failed)'}

## Backend Endpoints
${backendResult || '(scan failed)'}

## Check For
1. **Path mismatches**: Frontend calls a URL the backend doesn't serve
2. **Method mismatches**: Frontend uses GET but backend expects POST (or vice versa)
3. **Missing endpoints**: Backend has endpoints the frontend never calls (dead code?)
4. **Parameter mismatches**: Frontend sends params the backend doesn't accept, or vice versa
5. **Response format issues**: Check if frontend expects fields the backend might not return

## Output Format
For each issue found, provide:
- Issue type (error/warning/info)
- Frontend file and function
- Backend file and endpoint
- Description of the mismatch
- Suggested fix`,
  { label: 'verify-consistency', phase: 'Verify', schema: {
    type: 'object',
    properties: {
      summary: { type: 'string' },
      issues: {
        type: 'array',
        items: {
          type: 'object',
          properties: {
            severity: { type: 'string', enum: ['error', 'warning', 'info'] },
            frontendLocation: { type: 'string' },
            backendLocation: { type: 'string' },
            description: { type: 'string' },
            suggestedFix: { type: 'string' },
          },
          required: ['severity', 'description', 'suggestedFix'],
        },
      },
    },
    required: ['summary', 'issues'],
  } }
)

if (!verifyResult) return { error: 'Verification failed' }

phase('Report')

// Generate a readable report
const errors = verifyResult.issues.filter(i => i.severity === 'error')
const warnings = verifyResult.issues.filter(i => i.severity === 'warning')

return {
  summary: verifyResult.summary,
  errorCount: errors.length,
  warningCount: warnings.length,
  issues: verifyResult.issues,
  verdict: errors.length === 0
    ? '✅ All API endpoints are consistent!'
    : `❌ Found ${errors.length} errors and ${warnings.length} warnings.`,
}
