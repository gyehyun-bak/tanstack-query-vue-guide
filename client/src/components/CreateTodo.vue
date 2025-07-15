<script setup lang="ts">
import { ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { createTodo } from '@/api/createTodo'

const title = ref('')
const queryClient = useQueryClient()

const { mutate, isPending } = useMutation({
  mutationFn: createTodo,
  onSuccess: () => {
    title.value = ''
    queryClient.invalidateQueries({ queryKey: ['todos'] })
  },
  onError: (error) => {
    console.error(error)
  },
})

const onClick = async () => {
  mutate({ title: title.value })
}
</script>

<template>
  <div>
    <input v-model="title" placeholder="할 일을 입력하세요" />
    <button @click="onClick" :disabled="isPending || !title">추가</button>
  </div>
</template>
