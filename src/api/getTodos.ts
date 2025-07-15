import axios from 'axios'

export const getTodos = async () => {
  return (await axios.get('/api/todos')).data
}
