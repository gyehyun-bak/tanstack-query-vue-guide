<script setup lang="ts">
import { ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { createTodo } from '@/api/createTodo' // 위에서 작성한 API
import type { CreateTodoRequest } from '@/types/CreateTodoRequest'

const title = ref('')
const queryClient = useQueryClient()

const { mutate, isPending } = useMutation({
  mutationFn: (data: CreateTodoRequest) => createTodo(data),
  onSuccess: () => {
    title.value = ''
    queryClient.invalidateQueries({ queryKey: ['todos'] }) // todos 다시 불러오기
  },
})
</script>

<template>
  <div>
    <input v-model="title" placeholder="할 일을 입력하세요" />
    <button @click="mutate({ title })" :disabled="isPending || !title">추가</button>
  </div>
</template>
