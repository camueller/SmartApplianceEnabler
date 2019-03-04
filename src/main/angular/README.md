Clockpicker rausziehen
DOW dropdown leer nach Seitenwechsel


## Angular CLI help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## Install Smart Appliance Enabler dependencies

The installation of Semantic UI causes the installation of jQuery as well, which is required in order to call Semantic UI functions.
```
$ npm i --save semantic-ui-css
smartapplianceenabler@0.0.0
└─┬ semantic-ui-css@2.2.12
  └── jquery@3.2.1
```
In order to make time entry easier a clock picker is used:
```
$ npm i --save clockpicker
saeweb@0.0.0 /data/IdeaProjects/SmartApplianceEnabler/src/main/angular
└── clockpicker@0.0.7
```

In order to switch languages without compilation we use ```ngx-translate```:
```
npm install @ngx-translate/core --save
npm install @ngx-translate/http-loader --save
```

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Run development server

Run local dev web server without access restrictions (in order to test access from smartphone etc.)
Navigate to `http://localhost:4200/`.
The app will automatically reload if you change any of the source files.
```
$ ng serve --host 0.0.0.0 --disable-host-check
```

The error
```
axel@tpw520:~/IdeaProjects/SmartApplianceEnabler/src/main/angular$ ng serve --host 0.0.0.0 --disable-host-check
The "@angular/compiler-cli" package was not properly installed. Error: Error: Cannot find module '@angular/compiler-cli'
Error: The "@angular/compiler-cli" package was not properly installed. Error: Error: Cannot find module '@angular/compiler-cli'
    at Object.CompilerCliIsSupported (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/node_modules/@ngtools/webpack/src/ngtools_api.js:25:15)
    at new AotPlugin (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/node_modules/@ngtools/webpack/src/plugin.js:29:23)
    at _createAotPlugin (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/models/webpack-configs/typescript.js:92:16)
    at Object.getNonAotConfig (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/models/webpack-configs/typescript.js:100:19)
    at NgCliWebpackConfig.buildConfig (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/models/webpack-config.js:37:37)
    at Class.run (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/tasks/serve.js:71:98)
    at check_port_1.checkPort.then.port (/data/opt/node-v8.9.3-linux-x64/lib/node_modules/@angular/cli/commands/serve.js:123:26)
    at <anonymous>
    at process._tickCallback (internal/process/next_tick.js:188:7)
```
can be fixed by
```
npm install --save-dev @angular/cli@latest
```
