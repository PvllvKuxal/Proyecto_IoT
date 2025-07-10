<template>
  <aside :class="['sidebar', { 'sidebar-collapsed': collapsed }]">
    <div class="sidebar-top">
      <div class="sidebar-header-row" v-if="!collapsed">
        <div class="sidebar-logo">Mudae IoT</div>
        <button class="sidebar-arrow-btn" @click="collapsed = !collapsed" title="Colapsar">
          <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="12" fill="#2563eb"/>
            <path d="M10 8l4 4-4 4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <div v-else>
        <button class="sidebar-arrow-btn" @click="collapsed = !collapsed" title="Expandir">
          <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="12" fill="#2563eb"/>
            <path d="M14 8l-4 4 4 4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <nav class="sidebar-nav">
        <div class="sidebar-link active">
          <span class="sidebar-icon-svg">
            <img src="@/assets/dashboard.svg" alt="Dashboard" class="sidebar-svg-icon" />
          </span>
          <span v-if="!collapsed" class="sidebar-link-text">Dashboard</span>
        </div>
        <!-- Botón azul para agregar dispositivo -->
        <button v-if="!collapsed" class="sidebar-btn-add" @click="agregarDispositivo" :disabled="agregando">
          <span v-if="!agregando">+ Agregar dispositivo</span>
          <span v-else>Agregando...</span>
        </button>
        <!-- Lista de dispositivos -->
        <div v-if="!collapsed" class="sidebar-devices-list">
          <div v-for="device in dispositivos" :key="device.thingsboardId" class="sidebar-device-item">
            <span>{{ device.name }}</span>
            <button class="sidebar-btn-delete" @click="eliminarDispositivo(device.thingsboardId)" :disabled="eliminandoId === device.thingsboardId">
              <span v-if="eliminandoId === device.thingsboardId">
                <svg class="spinner" width="16" height="16" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
              </span>
              <span v-else>Eliminar</span>
            </button>
          </div>
        </div>
        <div class="sidebar-link" @click="archivesOpen = !archivesOpen" :aria-expanded="archivesOpen">
          <span class="sidebar-icon-svg">
            <img src="@/assets/archives.svg" alt="Archives" class="sidebar-svg-icon" />
          </span>
          <span v-if="!collapsed" class="sidebar-link-text">Archives</span>
          <span v-if="!collapsed" class="sidebar-arrow" :class="{ 'open': archivesOpen }">▼</span>
        </div>
        <transition name="fade">
          <div v-if="!collapsed && archivesOpen" class="sidebar-sublinks">
            <a v-for="med in userStore.mediciones" :key="med.id" class="sidebar-sublink sidebar-sublink-download"
               :href="`/api/mediciones/pdf/${med.rutaPdf ? med.rutaPdf.split('/').pop() : ''}`"
               target="_blank" download
               :title="med.rutaPdf ? med.rutaPdf.split('/').pop() : 'Archivo ' + med.id">
              {{ med.rutaPdf ? med.rutaPdf.split('/').pop() : 'Archivo ' + med.id }}
            </a>
          </div>
        </transition>
      </nav>
    </div>
    <!-- Botón de logout (debe estar abajo) -->
    <div class="sidebar-bottom">
      <button class="sidebar-btn-logout" @click="logout">Logout</button>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '../store/user'
import axios from 'axios'
import '../assets/sidebar.css'

const userStore = useUserStore()
const dispositivos = ref([])
const collapsed = ref(false)
const archivesOpen = ref(false)
const agregando = ref(false)
const eliminandoId = ref(null)

async function cargarDispositivos() {
  if (!userStore.id || !userStore.token) return;
  const res = await axios.get(`/users/${userStore.id}/devices`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  })
  dispositivos.value = res.data.devices || []
}

async function agregarDispositivo() {
  if (!userStore.id || !userStore.token || agregando.value) return;
  try {
    agregando.value = true
    await axios.post(`/api/devices/register/${userStore.id}`, {}, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    await cargarDispositivos()
    alert('Dispositivo agregado correctamente')
  } catch (e) {
    alert(e.response?.data?.error || e.message)
  } finally {
    agregando.value = false
  }
}

async function eliminarDispositivo(thingsboardId) {
  if (!userStore.token || eliminandoId.value) return;
  if (!confirm('¿Seguro que deseas eliminar este dispositivo?')) return;
  try {
    eliminandoId.value = thingsboardId
    await axios.delete(`/api/devices/${thingsboardId}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    })
    await cargarDispositivos()
    alert('Dispositivo eliminado correctamente')
  } catch (e) {
    alert(e.response?.data?.error || e.message)
  } finally {
    eliminandoId.value = null
  }
}

function logout() {
  userStore.token = ''
  userStore.id = null
  userStore.email = ''
  localStorage.clear()
  window.location.href = '/login'
}

onMounted(() => {
  cargarDispositivos()
})

watch(() => [userStore.id, userStore.token], ([id, token]) => {
  if (id && token) cargarDispositivos()
})
</script>

<style scoped>
.sidebar-btn-add {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  margin: 12px 0 8px 0;
  font-weight: 600;
  cursor: pointer;
  width: 90%;
  display: block;
  position: relative;
}
.sidebar-btn-add[disabled] {
  opacity: 0.7;
  cursor: not-allowed;
}
.sidebar-btn-add:hover {
  background: #1746a2;
}
.sidebar-devices-list {
  margin-bottom: 16px;
}
.sidebar-device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #e0e7ff;
  border-radius: 5px;
  padding: 6px 10px;
  margin-bottom: 6px;
  font-size: 1rem;
  color: #222;
}
.sidebar-btn-delete {
  background: #e11d48;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 3px 10px;
  font-size: 0.95rem;
  cursor: pointer;
}
.sidebar-btn-delete:hover {
  background: #b91c1c;
}
.sidebar-bottom {
  margin-top: auto;
  padding: 16px 0 8px 0;
  display: flex;
  justify-content: center;
}
.sidebar-btn-logout {
  background: #fff;
  color: #2563eb;
  border: 1.5px solid #2563eb;
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 600;
  cursor: pointer;
  width: 90%;
  display: block;
}
.sidebar-btn-logout:hover {
  background: #2563eb;
  color: #fff;
}
.spinner {
  margin-right: 6px;
  animation: spin 1s linear infinite;
  vertical-align: middle;
}
@keyframes spin {
  100% { transform: rotate(360deg); }
}
.spinner .path {
  stroke: #fff;
  stroke-linecap: round;
}
.sidebar-sublink-download {
  color: #60a5fa;
  text-decoration: underline;
  display: block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sidebar-sublink-download:hover {
  color: #2563eb;
}
.sidebar-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.sidebar-logo {
  font-size: 1.5rem;
  font-weight: bold;
  letter-spacing: 1px;
  color: #fff;
}
.sidebar-arrow-btn {
  background: none;
  border: none;
  padding: 0 0 0 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.sidebar-arrow-btn:focus {
  outline: none;
}
</style> 