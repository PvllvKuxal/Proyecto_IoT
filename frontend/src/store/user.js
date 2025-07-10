import { defineStore } from 'pinia'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'

function getUserIdFromToken(token) {
  try {
    const decoded = jwtDecode(token)
    // Suponiendo que el id viene como 'id' o 'userId' en el payload
    return decoded.id || decoded.userId || null
  } catch (e) {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    email: '',
    token: '',
    mediciones: [],
  }),
  actions: {
    async login(email, password) {
      const res = await axios.post('/auth/login', { email, password })
      this.token = res.data.token
      this.id = getUserIdFromToken(this.token)
      this.email = email
      localStorage.setItem('token', this.token)
      localStorage.setItem('userId', this.id)
      localStorage.setItem('userEmail', this.email)
    },
    loadFromStorage() {
      this.token = localStorage.getItem('token') || ''
      this.id = getUserIdFromToken(this.token)
      this.email = localStorage.getItem('userEmail') || ''
      console.log('loadFromStorage', this.id, this.token, this.email)
    },
    async fetchMediciones() {
      if (!this.id || !this.token) return
      const res = await axios.get(`/users/${this.id}/mediciones`, {
        headers: { Authorization: `Bearer ${this.token}` }
      })
      this.mediciones = res.data.mediciones
    }
  }
}) 