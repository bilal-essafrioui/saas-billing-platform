# ═══ STAGE 1 — BUILD ═══
FROM node:20-alpine AS builder

WORKDIR /app

# copier package.json en premier
COPY package*.json ./

# installer les dépendances
RUN npm ci

# copier le code source
COPY . .

# builder l'application React
RUN npm run build

# ═══ STAGE 2 — SERVE avec Nginx ═══
FROM nginx:alpine

# copier le build React
COPY --from=builder /app/dist /usr/share/nginx/html

# configuration Nginx pour React Router
# sans ça → les routes React renvoient 404 au refresh
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]