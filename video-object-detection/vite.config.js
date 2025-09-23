// vite.config.js
import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  build: {
    target: "esnext",
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        professor: resolve(__dirname, 'professor.html'),
        student: resolve(__dirname, 'student.html'),
        facedetection: resolve(__dirname, 'facedetection.html'),
      }
    }
  }
});
