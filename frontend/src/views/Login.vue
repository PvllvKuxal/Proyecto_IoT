<template>
  <div class="login-layout">
    <!-- Imagen a la izquierda (1/3, pegada al borde) -->
    <div class="login-image-container">
      <img src="@/assets/Left side panel.png" alt="Login Illustration" class="login-image" />
    </div>
    <!-- Formulario a la derecha (2/3, centrado) -->
    <div class="login-form-container">
      <form class="login-form" @submit.prevent="handleLogin">
        <h2 class="login-title">Login to your account</h2>
        <div class="login-field">
          <label for="email">Email</label>
          <input id="email" v-model="email" placeholder="Ingrese email" type="email" required />
        </div>
        <div class="login-field">
          <label for="password">Contraseña</label>
          <input id="password" v-model="password" placeholder="Ingrese Contraseña" type="password" required />
        </div>
        <button class="login-btn" :disabled="loading">{{ loading ? 'Ingresando...' : 'Login' }}</button>
        <div class="login-links">
          <span class="login-secondary">No tiene cuenta? <router-link to="/register" class="login-link">Crear una cuenta</router-link></span>
          <span v-if="error" class="login-secondary" style="color: #d32f2f;">{{ error }}</span>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import axios from 'axios'
import '../assets/login.css'

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const response = await axios.post('/auth/login', {
      email:email.value,
      password:password.value
    })
    // Guarda el token si el backend lo retorna
    if (response.data && response.data.token) {
      localStorage.setItem('token', response.data.token)
    }
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || 'Credenciales incorrectas.'
  } finally {
    loading.value = false
  }
}
</script> 