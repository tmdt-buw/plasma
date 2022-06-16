# PLASMA Frontend

This project was generated with [Angular CLI](https://github.com/angular/angular-cli).

## Services

This repository includes the PLASMA modeling UI library located under `projects/modeling`.
The library can be ported into other apps as a npm dependency `@tmdt-buw/pls-modeling`.
Please note that this library does still need the backend services to run and cannot be used
as a standalone library!

## Building the frontend
Build the UI by running `npm run build:dms` and `npm run build`

## Building the frontend Docker container
Build the docker container using the `Dockerfile`. Pay attention to the correct name and version.
Example `docker build -t plasma/frontend:1.3.0`. If the compose file is used, this step will be done
automatically.

## Development

### Client generation from backend

Run `npm run gen:dms`, `npm run gen:kgs`, `npm run gen:sas` to generate updated http-client and model from backend openapi-specs.
The DMS, KGS and SAS have to be up and running to do so.

### Development server

Run `npm run watch:dms` to build the library. The library will automatically reload if you change any of the source files.

Run `npm start` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.
