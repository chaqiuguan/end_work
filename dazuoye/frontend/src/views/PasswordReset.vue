<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">找回密码</h2>
      <el-steps :active="step" align-center style="margin-bottom:32px">
        <el-step title="验证邮箱" /><el-step title="重置密码" /><el-step title="完成" />
      </el-steps>

      <!-- Step 1: 验证邮箱 -->
      <el-form v-if="step === 0" ref="emailFormRef" :model="emailForm" :rules="emailRules" size="large" @submit.prevent="sendResetCode">
        <el-form-item prop="email">
          <div class="email-row">
            <el-input v-model="emailForm.email" placeholder="请输入注册邮箱" :prefix-icon="Message" class="email-input" />
            <el-button class="code-btn" :disabled="countdown > 0" :loading="sending" @click="sendResetCode">
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="emailForm.code" placeholder="请输入验证码" :prefix-icon="Key" maxlength="6" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="sending" block round size="large" @click="verifyCode">验证</el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: 重置密码 -->
      <el-form v-if="step === 1" ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" size="large" @submit.prevent="doReset">
        <el-form-item prop="password">
          <el-input v-model="pwdForm.password" placeholder="新密码（至少6位）" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="确认新密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" block round size="large" @click="doReset">重置密码</el-button>
        </el-form-item>
      </el-form>

      <!-- Step 3: 完成 -->
      <div v-if="step === 2" style="text-align:center">
        <el-icon :size="60" color="#27ae60"><CircleCheckFilled /></el-icon>
        <p style="margin:16px 0;font-size:16px">密码重置成功！</p>
        <el-button type="primary" size="large" round @click="$router.push('/login')">去登录</el-button>
      </div>

      <div class="auth-extra"><router-link to="/login">← 返回登录</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Message, Lock, Key, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const step = ref(0)
const sending = ref(false)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const emailForm = reactive({ email: '', code: '' })
const emailRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const pwdForm = reactive({ password: '', confirmPassword: '' })
const pwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
}

async function sendResetCode() {
  if (!emailForm.email) return ElMessage.warning('请输入邮箱')
  sending.value = true
  try {
    await request.post('/user/reset-code', { email: emailForm.email })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } finally { sending.value = false }
}

async function verifyCode() {
  if (!emailForm.code) return ElMessage.warning('请输入验证码')
  // 验证码校验在重置密码时一并校验，这里直接进入下一步
  step.value = 1
}

async function doReset() {
  if (pwdForm.password !== pwdForm.confirmPassword) return ElMessage.warning('两次密码不一致')
  loading.value = true
  try {
    await request.post('/user/reset-password', { email: emailForm.email, code: emailForm.code, newPassword: pwdForm.password })
    step.value = 2
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display:flex; justify-content:center; align-items:center; min-height:80vh; padding:40px 20px; }
.auth-card { width:100%; max-width:480px; background:#fff; border-radius:12px; padding:40px; box-shadow:0 2px 16px rgba(0,0,0,0.08); }
.auth-title { text-align:center; font-size:24px; font-weight:600; margin-bottom:24px; }
.auth-extra { text-align:center; font-size:14px; margin-top:12px; }
.auth-extra a { color:var(--primary-color); text-decoration:none; }
.email-row { display:flex; align-items:stretch; }
.email-input { flex:1; }
.code-btn { flex-shrink:0; white-space:nowrap; border-radius:0 8px 8px 0; margin-left:-1px; }
</style>
