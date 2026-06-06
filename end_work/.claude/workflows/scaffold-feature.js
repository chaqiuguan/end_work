export const meta = {
  name: 'scaffold-feature',
  description: 'Scaffold a complete full-stack feature: entity → mapper → service → controller → API → view',
  phases: [
    { title: 'Plan', detail: 'Plan the feature across the full stack' },
    { title: 'Backend', detail: 'Generate backend layers (entity, mapper, service, controller)' },
    { title: 'Frontend', detail: 'Generate frontend API module and view component' },
    { title: 'Wire Up', detail: 'Wire everything together (route, navigation, permissions)' },
  ],
}

// ============================================================
// 全栈功能脚手架 — 一键生成完整功能模块
// 从后端 Entity → Mapper → Service → Controller
// 到前端 API → Store → View → Route
// ============================================================

const featureName = args?.name || args?.feature || args
const featureDesc = args?.description || args?.desc || `${featureName} 功能模块`

if (!featureName || typeof featureName !== 'string') {
  return {
    error: '请提供功能名称，例如: /workflow scaffold-feature name=ProductReview description=商品评价功能',
    hint: '用法: args.name = 功能英文名, args.description = 功能中文描述',
  }
}

phase('Plan')

const plan = await agent(
  `Plan a full-stack feature for the "转鱼宝猫" e-commerce platform.

## Feature: ${featureName}
## Description: ${featureDesc}

## Existing Architecture
- Backend: Spring Boot + MyBatis Plus + MySQL
  - Pattern: Entity → Mapper (extends BaseMapper) → Service (interface + impl) → Controller
  - Base package: com.zhuanbaomao
  - Common classes: Result<T>, PageResult<T>, ResultCode
  - Security: JWT auth via @CurrentUser annotation
  - Config: MybatisPlusConfig, CorsConfig, SecurityConfig

- Frontend: Vue 3 + Vite + Element Plus + Pinia + Axios
  - API: frontend/src/api/ — Axios wrapper with base /api, auto token
  - Store: frontend/src/store/ — Pinia with persistedstate
  - Views: frontend/src/views/ — lazy-loaded route components
  - Components: frontend/src/components/ — reusable components
  - Router: frontend/src/router/index.js — meta.title, meta.requiresAuth

## Plan the following
1. Database table design (table name, columns, indexes)
2. Backend files needed (Entity, Mapper, Service interface, Service impl, Controller, DTO, VO)
3. Frontend files needed (API module, View component, Route entry)
4. Any new permissions or navigation items needed

Be specific about file names, paths, and key code decisions.`,
  { label: 'plan-feature', phase: 'Plan' }
)

log(`功能规划完成: ${featureName}`)

phase('Backend')

const backendCode = await agent(
  `Generate ALL backend code for the "${featureName}" feature.

## Plan
${plan}

## Project Patterns (MUST FOLLOW)

### Entity Pattern
\`\`\`java
@Data
@TableName("table_name")
public class EntityName {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
\`\`\`

### Mapper Pattern
\`\`\`java
@Mapper
public interface EntityMapper extends BaseMapper<Entity> { }
\`\`\`

### Service Pattern
\`\`\`java
// Interface
public interface EntityService { Result<VO> method(DTO dto); }
// Impl
@Service
public class EntityServiceImpl implements EntityService { ... }
\`\`\`

### Controller Pattern
\`\`\`java
@RestController
@RequestMapping("/api/entity")
public class EntityController {
    @Autowired
    private EntityService entityService;
    // Use @CurrentUser for auth
}
\`\`\`

## Output
Generate each file with its COMPLETE content and exact file path.
Use the same package structure, imports, and coding style as the existing project.`,
  { label: 'generate-backend', phase: 'Backend', schema: {
    type: 'object',
    properties: {
      files: {
        type: 'array',
        items: {
          type: 'object',
          properties: {
            path: { type: 'string' },
            content: { type: 'string' },
          },
          required: ['path', 'content'],
        },
      },
    },
    required: ['files'],
  } }
)

phase('Frontend')

const frontendCode = await agent(
  `Generate ALL frontend code for the "${featureName}" feature.

## Backend API Contract
${backendCode ? JSON.stringify(backendCode.files?.map(f => f.path) || []) : 'See plan above'}

## Project Patterns (MUST FOLLOW)

### API Module Pattern
\`\`\`js
import request from './request'
export function getXxxAPI(params) {
  return request.get('/xxx/list', { params })
}
\`\`\`

### View Pattern
- <script setup> with Composition API
- Element Plus components
- Loading/empty/error states
- Responsive design
- Uses CSS custom properties

### Router Entry
- Lazy loaded: () => import('@/views/NewView.vue')
- meta: { title: '页面标题', requiresAuth: true }

## Output
Generate each file with its COMPLETE content. Include:
1. API module: frontend/src/api/${featureName.toLowerCase()}.js
2. View component: frontend/src/views/${featureName}.vue
3. Router entry (as a code snippet to insert)`,
  { label: 'generate-frontend', phase: 'Frontend', schema: {
    type: 'object',
    properties: {
      files: {
        type: 'array',
        items: {
          type: 'object',
          properties: {
            path: { type: 'string' },
            content: { type: 'string' },
            type: { type: 'string', enum: ['api', 'view', 'route', 'component'] },
          },
          required: ['path', 'content', 'type'],
        },
      },
    },
    required: ['files'],
  } }
)

phase('Wire Up')

const integration = await agent(
  `Summarize the integration steps needed to wire up the "${featureName}" feature:

## Backend Files
${(backendCode?.files || []).map(f => `- ${f.path}`).join('\n')}

## Frontend Files
${(frontendCode?.files || []).map(f => `- ${f.path}`).join('\n')}

## Checklist
1. Which existing files need to be modified (e.g., router/index.js, Navbar.vue)?
2. What exactly needs to be added to each?
3. Any database migration needed?
4. Any new permissions or roles?
5. Any new npm/maven dependencies?

Be specific — provide exact code snippets for each modification.`,
  { label: 'plan-integration', phase: 'Wire Up' }
)

return {
  feature: featureName,
  description: featureDesc,
  plan,
  backendFiles: backendCode?.files || [],
  frontendFiles: frontendCode?.files || [],
  integrationSteps: integration,
  summary: `✅ "${featureName}" 功能脚手架生成完成！
- 后端文件: ${(backendCode?.files || []).length} 个
- 前端文件: ${(frontendCode?.files || []).length} 个
请使用 /run 启动项目并验证功能。`,
}
