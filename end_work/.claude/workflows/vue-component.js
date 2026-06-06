export const meta = {
  name: 'vue-component',
  description: 'Generate Vue 3 components following project patterns (Composition API, Element Plus, scoped styles)',
  phases: [
    { title: 'Analyze', detail: 'Analyze existing component patterns' },
    { title: 'Generate', detail: 'Generate the Vue component' },
  ],
}

// ============================================================
// Vue 组件生成器 — 按照项目规范生成 Vue 3 组件
// 项目技术栈: Vue 3 + Composition API + Element Plus + Pinia
// ============================================================

phase('Analyze')

// 读取项目中已有的组件示例，了解代码风格
const examples = await parallel([
  () => agent(
    'Read the following files and summarize the coding patterns:\n' +
    '- frontend/src/components/ProductCard.vue\n' +
    '- frontend/src/components/Navbar.vue\n' +
    '- frontend/src/components/Pagination.vue\n' +
    'Focus on: template structure, script setup style, style approach (CSS vars used, scoped), how props/emits are defined, how Element Plus components are used.',
    { label: 'analyze-components' }
  ),
  () => agent(
    'Read the following files and summarize the patterns:\n' +
    '- frontend/src/api/request.js\n' +
    '- frontend/src/api/product.js\n' +
    '- frontend/src/router/index.js\n' +
    '- frontend/src/store/user.js\n' +
    'Focus on: API request wrapper pattern, how stores are defined (Pinia), how routes use lazy loading and meta fields.',
    { label: 'analyze-infra' }
  ),
])

const patterns = examples.filter(Boolean).map(r => r || '').join('\n\n')

phase('Generate')

const componentSpec = args?.component || args?.spec || args

const result = await agent(
  `You are a Vue 3 expert generating a component for the "转鱼宝猫" e-commerce project.

## Project Tech Stack
- Vue 3 (Composition API, <script setup>)
- Element Plus (UI components, icons via @element-plus/icons-vue)
- Pinia (state management)
- Vue Router 4 (lazy loading)
- Axios (via @/api/request wrapper)
- CSS custom properties for theming (var(--radius), var(--shadow), var(--text-secondary), etc.)

## Existing Patterns Reference
${patterns}

## Component Specification
${typeof componentSpec === 'string' ? componentSpec : JSON.stringify(componentSpec, null, 2)}

## Requirements
1. Use <script setup> syntax with Composition API
2. Use Element Plus components where appropriate (el-button, el-input, el-dialog, etc.)
3. Use scoped styles with CSS custom properties
4. Follow the existing naming conventions and code style
5. If it's a VIEW component, add route config as a comment
6. If it needs API calls, add the API function as a comment
7. Handle loading, empty, and error states
8. Props should use defineProps with type validation
9. Emits should use defineEmits

Generate ONLY the complete .vue file content. No explanations, just the code.`,
  { label: 'generate-component', schema: { type: 'object', properties: { filename: { type: 'string' }, code: { type: 'string' } }, required: ['filename', 'code'] } }
)

if (!result) return { error: 'Component generation failed' }

// Determine where the component should live
const isView = result.filename.toLowerCase().includes('view') ||
               result.filename.includes('views')
const targetDir = isView ? 'frontend/src/views/' : 'frontend/src/components/'

const filePath = result.filename.startsWith('frontend/')
  ? result.filename
  : targetDir + (result.filename.includes('/') ? result.filename.split('/').pop() : result.filename)

return {
  componentName: result.filename.replace(/\.vue$/, ''),
  filePath,
  code: result.code,
  type: isView ? 'view' : 'component',
  instruction: `请将以下代码写入 ${filePath}，然后更新 router/index.js（如果是 view）并验证。`,
}
