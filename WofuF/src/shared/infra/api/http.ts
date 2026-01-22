import axios from 'axios'

export const http = axios.create({
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
})

