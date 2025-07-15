import type { CreateTodoRequest } from '@/types/CreateTodoRequest'
import axios from 'axios'

export const createTodo = async (data: CreateTodoRequest) => {
  return (await axios.post('http://localhost:8080/api/todos', data)).data
}
