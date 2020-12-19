# Stage 0, based on Node.js, to build and compile Angular
FROM node as node
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 1, run on nginx
FROM nginx
# copy over the artifacts in dist folder to hidden nginx public folder

COPY nginx.conf.template /etc/nginx/conf.d/default.conf
COPY --from=node /usr/src/app/dist/plasma-ui /usr/share/nginx/html
# copy a nginx config that works with angular

