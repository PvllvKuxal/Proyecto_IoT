<template>
  <div class="dashboard-layout">
    <Sidebar />
    <div class="dashboard-center-wrapper">
      <div class="dashboard-content">
        <div class="dashboard-cards">
          <SensorCard title="Temperatura" icon="thermostat" unit="C°"
            :actual="telemetria.temperatura.actual"
            :history="telemetria.temperatura.history"
          />
          <SensorCard title="Humedad" icon="water_drop" unit="%"
            :actual="telemetria.humedad.actual"
            :history="telemetria.humedad.history"
          />
          <SensorCard title="Presión" icon="speed" unit="Pa"
            :actual="telemetria.presion.actual"
            :history="telemetria.presion.history"
          />
        </div>
        <div class="dashboard-actions">
          <button class="dashboard-btn dashboard-btn-primary" @click="iniciarMedicion">Comenzar Medición</button>
          <button class="dashboard-btn dashboard-btn-danger" @click="terminarMedicion">Terminar Medición</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import Sidebar from '../components/Sidebar.vue'
import SensorCard from '../components/SensorCard.vue'
import axios from 'axios'
import '../assets/dashboard.css'
import '../assets/sidebar.css'
import '../assets/sensorcard.css'

const userStore = useUserStore()
const dispositivos = ref([])
const telemetria = ref({
  temperatura: { actual: '--', history: [] },
  humedad: { actual: '--', history: [] },
  presion: { actual: '--', history: [] }
})

onMounted(async () => {
  await userStore.loadFromStorage();
  if (userStore.id && userStore.token) {
    await userStore.fetchMediciones();
    await cargarDispositivosYTelemetria();
    startPollingTelemetria(); // El polling se inicia solo al entrar al dashboard
  }
});

let pollingInterval = null;
function startPollingTelemetria() {
  if (pollingInterval) clearInterval(pollingInterval);
  pollingInterval = setInterval(cargarDispositivosYTelemetria, 2000);
}
function stopPollingTelemetria() {
  if (pollingInterval) clearInterval(pollingInterval);
}

async function cargarDispositivosYTelemetria() {
  if (!userStore.id || !userStore.token) {
    return;
  }
  const res = await axios.get(`/users/${userStore.id}/devices`, {
    headers: { Authorization: `Bearer ${userStore.token}` }
  });
  dispositivos.value = res.data.devices || [];
  if (dispositivos.value.length > 0) {
    const device = dispositivos.value[0];
    const tRes = await axios.get(`/api/devices/${device.thingsboardId}/telemetry`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    });
    console.log('Telemetría recibida:', tRes.data);
    const temp = tRes.data.temperatura ?? '--';
    const hum = tRes.data.humedad ?? '--';
    const pres = tRes.data.presion ?? '--';
    telemetria.value.temperatura.actual = temp;
    telemetria.value.humedad.actual = hum;
    telemetria.value.presion.actual = pres;
    // Actualizar historial (solo si es un número válido)
    if (!isNaN(parseFloat(temp))) {
      telemetria.value.temperatura.history.push(Number(temp));
      if (telemetria.value.temperatura.history.length > 20) telemetria.value.temperatura.history.shift();
    }
    if (!isNaN(parseFloat(hum))) {
      telemetria.value.humedad.history.push(Number(hum));
      if (telemetria.value.humedad.history.length > 20) telemetria.value.humedad.history.shift();
    }
    if (!isNaN(parseFloat(pres))) {
      telemetria.value.presion.history.push(Number(pres));
      if (telemetria.value.presion.history.length > 20) telemetria.value.presion.history.shift();
    }
    console.log('Temp actual:', telemetria.value.temperatura.actual, 'Historial:', telemetria.value.temperatura.history);
    console.log('Hum actual:', telemetria.value.humedad.actual, 'Historial:', telemetria.value.humedad.history);
    console.log('Pres actual:', telemetria.value.presion.actual, 'Historial:', telemetria.value.presion.history);
  } else {
    telemetria.value.temperatura.actual = '--';
    telemetria.value.temperatura.history = [];
    telemetria.value.humedad.actual = '--';
    telemetria.value.humedad.history = [];
    telemetria.value.presion.actual = '--';
    telemetria.value.presion.history = [];
  }
}

async function iniciarMedicion() {
  console.log('Iniciar medición', dispositivos.value);
  for (const device of dispositivos.value) {
    try {
      await axios.post(`/mediciones/${device.thingsboardId}/iniciar`, {}, {
        headers: { Authorization: `Bearer ${userStore.token}` }
      });
    } catch (e) {
      alert(`Error al iniciar medición para dispositivo ${device.thingsboardId}: ${e.response?.data?.message || e.message}`);
    }
  }
  alert('Intento de iniciar medición completado.');
}

async function terminarMedicion() {
  for (const device of dispositivos.value) {
    try {
      const res = await axios.post(`/mediciones/${device.thingsboardId}/detener`, {}, {
        headers: { Authorization: `Bearer ${userStore.token}` }
      });
      const medicion = res.data;
      if (medicion.rutaPdf) {
        const filename = medicion.rutaPdf.split('/').pop();
        const link = document.createElement('a');
        link.href = `/mediciones/pdf/${filename}`;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    } catch (e) {
      alert(`Error al detener medición para dispositivo ${device.thingsboardId}: ${e.response?.data?.message || e.message}`);
    }
  }
  alert('Intento de terminar medición completado.');
  // stopPollingTelemetria(); // Ya no se detiene el polling al terminar la medición
}
</script>