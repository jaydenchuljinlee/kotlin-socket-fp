<script setup lang="ts">
import Chat from './components/Chat.vue'
</script>

<template>
  <main class="min-h-screen w-full">
    <Chat />
  </main>
</template>

<style>
/* 전역 스타일 */
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100vh;
}

main {
  height: 100%;
  width: 100%;
}
</style>
