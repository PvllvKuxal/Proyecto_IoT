<template>
  <div class="login-layout">
    <div class="login-image-container">
      <img src="@/assets/Left side panel.png" alt="Register Illustration" class="login-image" />
    </div>
    <div class="login-form-container">
      <form class="login-form" @submit.prevent="handleRegister">
        <h2 class="login-title">Crear cuenta</h2>
        <div class="login-field">
          <label for="email">Email</label>
          <input id="email" v-model="email" placeholder="Ingrese email" type="email" required />
        </div>
        <div class="login-field">
          <label for="password">Contraseña</label>
          <input id="password" v-model="password" placeholder="Ingrese Contraseña" type="password" required />
        </div>
        <div class="login-field">
          <label for="confirm">Confirmar contraseña</label>
          <input id="confirm" v-model="confirmPassword" placeholder="Repita la contraseña" type="password" required />
        </div>
        <button class="login-btn" :disabled="loading">{{ loading ? 'Registrando...' : 'Registrarse' }}</button>
        <div class="login-links">
          <span class="login-secondary">¿Ya tienes cuenta? <a href="/login" class="login-link">Iniciar sesión</a></span>
          <span v-if="error" class="login-secondary" style="color: #d32f2f;">{{ error }}</span>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import '../assets/login.css'

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

async function handleRegister() {
  error.value = ''
  if (password.value !== confirmPassword.value) {
    error.value = 'Las contraseñas no coinciden.'
    return
  }
  loading.value = true
  try {
    await axios.post('/auth/register', {
      email: email.value,
      password: password.value
    })
    router.push('/login')
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al registrar usuario.'
  } finally {
    loading.value = false
  }
}
</script> 