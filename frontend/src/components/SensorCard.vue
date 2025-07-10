<template>
  <div class="sensor-card">
    <div class="sensor-icon">
      <img v-if="icon === 'thermostat'" src="@/assets/Temperatura.svg" alt="Temperatura" class="sensor-svg-icon" />
      <img v-else-if="icon === 'water_drop'" src="@/assets/Humedad.svg" alt="Humedad" class="sensor-svg-icon" />
      <img v-else-if="icon === 'speed'" src="@/assets/Presion.svg" alt="Presión" class="sensor-svg-icon" />
    </div>
    <h3 class="sensor-title">{{ title }}</h3>
    <div class="sensor-value">Actual <span class="sensor-unit">{{ actual !== undefined ? actual : '--' }} {{ unit }}</span></div>
    <Line :data="chartData" :options="chartOptions" class="sensor-graph" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart,
  LineElement,
  PointElement,
  LinearScale,
  Title,
  CategoryScale,
  Tooltip,
  Legend
} from 'chart.js'

Chart.register(LineElement, PointElement, LinearScale, Title, CategoryScale, Tooltip, Legend)

const props = defineProps({
  title: String,
  icon: String,
  unit: String,
  actual: [String, Number],
  history: {
    type: Array,
    default: () => []
  }
})

const chartData = computed(() => ({
  labels: props.history.map((_, i) => i + 1),
  datasets: [
    {
      label: props.title,
      data: props.history.map(v => typeof v === 'number' ? v : null),
      fill: false,
      borderColor: '#2563eb',
      backgroundColor: '#2563eb',
      tension: 0.3,
      pointRadius: 2
    }
  ]
}))

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { display: false },
    tooltip: { enabled: true }
  },
  scales: {
    x: { display: false },
    y: { display: true, beginAtZero: true }
  }
}

import '../assets/sensorcard.css'
</script>

<style scoped>
.sensor-graph {
  width: 220px;
  height: 70px;
  margin-bottom: 10px;
}
</style> 