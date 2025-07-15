<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { getTodos } from './api/getTodos'

const { data, isPending, isError, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
</script>

<template>
  <span v-if="isPending">Loading...</span>
  <span v-else-if="isError">Error: {{ error?.message }}</span>
  <ul v-else-if="data">
    <li v-for="todo in data" :key="todo.id">{{ todo.title }}</li>
  </ul>
</template>
