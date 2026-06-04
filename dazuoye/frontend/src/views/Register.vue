<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">📝 注册账号</h2>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message">
            <template #append>
              <el-button
                :disabled="countdown > 0"
                :loading="sendingCode"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="code">
          <el-input v-model="form.code" placeholder="请输入6位验证码" :prefix-icon="Key" maxlength="6" />
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-50个字符）" :prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）"
            :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码（再次输入）"
            :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="danger" native-type="submit" :loading="loading" block round>
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-extra">
        <router-link to="/login">已有账号？去登录 →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { User, Lock, Message, Key } from '@element-plus/icons-vue'
import { emailValidator } from '@/utils/validators'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = reactive({
  email: '',
  code: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: emailValidator, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在3-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/** 发送验证码 */
async function sendCode() {
  // 先校验邮箱
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(form.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  sendingCode.value = true
  try {
    await request.post('/user/send-code', { email: form.email })
    ElMessage.success('验证码已发送，请查收邮件')

    // 60秒倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    sendingCode.value = false
  }
}

/** 注册 */
async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      email: form.email,
      code: form.code
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
  padding: 20px;
}
.login-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: var(--shadow);
}
.login-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 30px;
}
.login-extra {
  text-align: center;
  font-size: 14px;
}
.login-extra a { color: var(--primary-color); }

/* Element Plus input-group append button styling */
:deep(.el-input-group__append) {
  padding: 0;
}
:deep(.el-input-group__append .el-button) {
  border: none;
  height: 100%;
  padding: 0 12px;
}
</style>
