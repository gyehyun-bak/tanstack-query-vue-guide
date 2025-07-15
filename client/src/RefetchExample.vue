<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { getTodos } from './api/getTodos'

const { data, isPending, isError, error, refetch } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
  refetchInterval: 5000, // 5초마다 자동 새로고침
})
</script>

<template>
  <button @click="() => refetch()">새로고침</button>

  <div v-if="isPending">로딩 중...</div>
  <div v-else-if="isError">에러: {{ error?.message }}</div>
  <ul v-else>
    <li v-for="todo in data" :key="todo.id">{{ todo.title }}</li>
  </ul>
</template>
