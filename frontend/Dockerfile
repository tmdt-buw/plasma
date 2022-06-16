FROM node:lts-alpine as build

WORKDIR /home/
COPY ./package-lock.json ./
COPY ./package.json ./

RUN npm install

COPY ./ ./

RUN npm run build:dms -- --configuration=production
RUN npm run build -- --configuration=production

FROM nginx
# copy over the artifacts in dist folder to hidden nginx public folder
COPY --from=build /home/dist/plasma /usr/share/nginx/html

# copy a nginx config that works with angular
COPY nginx.conf /etc/nginx/conf.d/default.conf
