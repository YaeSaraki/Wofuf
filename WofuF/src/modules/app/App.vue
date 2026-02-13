<script lang="ts" setup>
import AppNavbar from '@M/app/components/AppNavbar.vue'
import AppFooter from '@M/app/components/AppFooter.vue'
import {RouterView} from 'vue-router'
</script>

<template>
  <AppNavbar/>
  <main>
    <RouterView/>
  </main>
  <AppFooter/>
</template>

<style scoped>
main {
  margin-top: 5%;
}
</style>
