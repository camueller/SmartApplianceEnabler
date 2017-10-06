This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.3.1.

## Development server

Run `ng serve [--host 0.0.0.0 --disable-host-check]` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## Smart Appliance Enabler specifics

The installation of Semantic UI causes the installation of jQuery as well, which is required in order to call Semantic UI functions.
```
$ npm i --save semantic-ui-css
smartapplianceenabler@0.0.0
└─┬ semantic-ui-css@2.2.12
  └── jquery@3.2.1

