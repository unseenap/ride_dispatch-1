# Build stage: production Angular bundle (uses environment.prod.ts, whose
# apiBaseUrl is the relative /api - nginx proxies that to the backend).
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage: nginx serves the static bundle and proxies /api.
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/dispatchhub-frontend/browser /usr/share/nginx/html
EXPOSE 80
