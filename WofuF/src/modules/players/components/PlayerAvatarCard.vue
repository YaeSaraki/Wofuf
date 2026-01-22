<script setup lang="ts">
import Card from 'primevue/card'
import Avatar from 'primevue/avatar'

defineProps<{
  name: string
}>()
</script>

<template>
  <Card class="player-card">
    <template #content>
      <div class="content">
        <Avatar :label="name.charAt(0)" size="large" shape="circle" />
        <span class="name">{{ name }}</span>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.player-card {
  cursor: pointer;
}

.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 0.85rem;
  opacity: 0.85;
}
</style>
