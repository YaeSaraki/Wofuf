<script setup lang="ts">
import AppNavbar from './AppNavbar.vue'
import AppFooter from './AppFooter.vue'
import { RouterView } from 'vue-router'
</script>

<template>
  <AppNavbar />
    <main>
      <RouterView />
    </main>
  <AppFooter />
</template>

<style scoped>

main {
  margin-top: 5%;
}

</style>
