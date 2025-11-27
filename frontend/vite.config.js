import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://18.141.34.176:6868',
        // target: 'http://localhost:6868',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
