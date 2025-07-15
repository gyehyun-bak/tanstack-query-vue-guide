import axios from 'axios'

export const getTodos = async () => {
  return (await axios.get('http://localhost:8080/api/todos')).data
}
