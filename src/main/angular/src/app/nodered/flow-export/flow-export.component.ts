import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {FlowExportData} from './flow-export-data';
import {MqttSettings} from '../../settings/mqtt-settings';

@Component({
  selector: 'app-flow-export',
  templateUrl: './flow-export.component.html',
  styles: [
  ]
})
export class FlowExportComponent implements OnInit {

  exportJson: string;
  row1 = 40;
  row2 = 100;
  row3 = 160;
  row4 = 220;
  row5 = 280;
  row6 = 340;
  row7 = 400;
  col1 = 170;
  col2 = 490;
  col3 = 690;
  col4 = 890;
  col5 = 1120;

  constructor(@Inject(MAT_DIALOG_DATA) public data: FlowExportData) { }

  ngOnInit(): void {
    const mqttBrokerId = this.generateId();
    const uiTabId = this.generateId();
    const generalNodes = this.createGeneralNodes(mqttBrokerId, uiTabId);
    const applianceNodes = this.data.applianceIds.flatMap(applianceId => this.createApplianceNodes(applianceId, mqttBrokerId, uiTabId));
    this.exportJson = JSON.stringify([
      this.mqttBroker(mqttBrokerId, this.data.mqttSettings),
      this.dashboard(),
      this.uiTab(uiTabId),
      ...generalNodes,
      ...applianceNodes,
    ], null, 2);
  }

  private createGeneralNodes(mqttBrokerId: string, uiTabId: string) {
    const tabId = this.generateId();
    const uiGroupId = this.generateId();
    const eventJoinId = this.generateId();
    const eventPrepareDataId = this.generateId();
    const eventDashboardId = this.generateId();
    return [
      this.tab(tabId, 'General'),
      this.uiGroup(uiGroupId, 'General', uiTabId),
      this.styleTemplate(this.col1, this.row1, tabId, uiGroupId),

      this.mqttIn(this.col1, this.row2, tabId, mqttBrokerId, 'sae/Event/#', [eventJoinId]),
      this.join(this.col2, this.row2, eventJoinId, tabId, 'object', 1, [eventPrepareDataId]),
      this.generalEventPrepareData(this.col3, this.row2, eventPrepareDataId, tabId, [eventDashboardId]),
      this.uiTable(this.col4, this.row2, eventDashboardId, tabId, uiGroupId, 'Events', 0, 0),
    ];
  }

  private createApplianceNodes(applianceId: string, mqttBrokerId: string, uiTabId: string) {
    const tabId = this.generateId();
    const uiGroupId = this.generateId();
    const evChargerSocChangedMqttFilterId = this.generateId();
    const evChargerStatehangedMqttFilterId = this.generateId();
    const applianceInfoMqttFilterId = this.generateId();
    const applianceJoinId = this.generateId();
    const appliancePrepareDataId = this.generateId();
    const applianceInDashboardId = this.generateId();
    const controlMqttFilterId = this.generateId();
    const controlChangedId = this.generateId();
    const controlInDashboardId = this.generateId();
    const meterMqttFilterId = this.generateId();
    const meterJoinId = this.generateId();
    const meterPrepareDataId = this.generateId();
    const meterChartInDashboardId = this.generateId();
    const meterFiguresPrepareDataId = this.generateId();
    const meterFiguresInDashboardId = this.generateId();
    const timeframeIntervalsMqttFilterId = this.generateId();
    const timeframeIntervalsChartPrepareDataId = this.generateId();
    const timeframeIntervalsInDashboardId = this.generateId();
    return [
      this.tab(tabId, applianceId),
      this.uiGroup(uiGroupId, applianceId, uiTabId),
      this.mqttIn(this.col1, this.row5, tabId, mqttBrokerId, `sae/${applianceId}/#`, [
        evChargerSocChangedMqttFilterId,
        evChargerStatehangedMqttFilterId,
        applianceInfoMqttFilterId,
        controlMqttFilterId,
        meterMqttFilterId,
        timeframeIntervalsMqttFilterId
      ]),

      this.topicFilter(this.col2, this.row1, evChargerSocChangedMqttFilterId, tabId, 'EVChargerSocChanged',
        '.*\\/EVChargerSocChanged', [applianceJoinId]),

      this.topicFilter(this.col2, this.row2, evChargerStatehangedMqttFilterId, tabId, 'EVChargerStateChanged',
        '.*\\/EVChargerStateChanged$', [applianceJoinId]),

      this.topicFilter(this.col2, this.row3, applianceInfoMqttFilterId, tabId, 'ApplianceInfo', '.*\\/ApplianceInfo$', [applianceJoinId]),
      this.join(this.col3, this.row3, applianceJoinId, tabId, 'merged', 2, [appliancePrepareDataId]),
      this.appliancePrepareData(this.col4, this.row3, appliancePrepareDataId, tabId, [applianceInDashboardId]),
      this.uiTable(this.col5, this.row3, applianceInDashboardId, tabId, uiGroupId, 'Appliance', 8, 8),

      this.topicFilter(this.col2, this.row4, controlMqttFilterId, tabId, 'Control', '.*\\/Control$',
        [applianceJoinId, controlChangedId, meterJoinId]),
      this.controlChange(this.col3, this.row4, controlChangedId, tabId, [controlInDashboardId]),
      this.controlInDashboard(this.col5, this.row4, controlInDashboardId, tabId, uiGroupId),

      this.topicFilter(this.col2, this.row5, meterMqttFilterId, tabId, 'Meter', '.*\\/Meter$', [meterJoinId, meterFiguresPrepareDataId]),
      this.join(this.col3, this.row5, meterJoinId, tabId, 'merged', 2, [meterPrepareDataId, meterFiguresPrepareDataId]),
      this.meterChartPrepareData(this.col4, this.row5, meterPrepareDataId, tabId, meterChartInDashboardId),
      this.meterChartInDashboard(this.col5, this.row5, meterChartInDashboardId, tabId, uiGroupId),

      this.meterFiguresPrepareData(this.col4, this.row6, meterFiguresPrepareDataId, tabId, [meterFiguresInDashboardId]),
      this.meterFiguresInDashboard(this.col5, this.row6, meterFiguresInDashboardId, tabId, uiGroupId),

      this.topicFilter(this.col2, this.row7, timeframeIntervalsMqttFilterId, tabId, 'TimeframeIntervalQueue',
        '.*\\/TimeframeIntervalQueue$', [timeframeIntervalsChartPrepareDataId]),
      this.timeframeIntervalsChartPrepareData(this.col4, this.row7, timeframeIntervalsChartPrepareDataId, tabId,
        [timeframeIntervalsInDashboardId]),
      this.timeframeIntervalsChartInDashboard(this.col5, this.row7, timeframeIntervalsInDashboardId, tabId, uiGroupId)
    ];
  }

  private styleTemplate(x: number, y: number, tabId: string, uiGroupId: string) {
    return {
      'id': this.generateId(),
      'type': 'ui_template',
      'z': tabId,
      'group': uiGroupId,
      'name': 'Styles',
      'order': 5,
      'width': 0,
      'height': 0,
      'format': '<style>\n.nr-dashboard-ui_table {\n    padding-top: 15px !important;\n}\n</style>',
      'storeOutMessages': true,
      'fwdInMessages': true,
      'resendOnRefresh': true,
      'templateScope': 'global',
      'className': '',
      x,
      y,
      'wires': [
        []
      ]
    };
  }

  private mqttBroker(id: string, mqttSettings: MqttSettings) {
    return {
      id,
      'type': 'mqtt-broker',
      'name': 'MQTT Broker (SAE)',
      'broker': mqttSettings.mqttBrokerHost,
      'port': mqttSettings.mqttBrokerPort,
      'clientid': '',
      'autoConnect': true,
      'usetls': false,
      'protocolVersion': '4',
      'keepalive': '60',
      'cleansession': true,
      'birthTopic': '',
      'birthQos': '0',
      'birthPayload': '',
      'birthMsg': {},
      'closeTopic': '',
      'closeQos': '0',
      'closePayload': '',
      'closeMsg': {},
      'willTopic': '',
      'willQos': '0',
      'willPayload': '',
      'willMsg': {},
      'sessionExpiry': ''
    };
  }

  private tab(id: string, label: string) {
    return {
      id,
      'type': 'tab',
      label,
      'disabled': false,
      'info': ''
    };
  }

  private uiGroup(id: string, name: string, uiTabId: string) {
    return  {
      id,
      'type': 'ui_group',
      name,
      'tab': uiTabId,
      'order': 4,
      'disp': true,
      'width': '27',
      'collapse': true,
      'className': ''
    };
  }

  private uiTab(id: string) {
    return {
      id,
      'type': 'ui_tab',
      'name': 'Smart Appliance Enabler',
      'icon': 'dashboard',
      'disabled': false,
      'hidden': false
    };
  }

  private dashboard() {
    return     {
      'id': this.generateId(),
      'type': 'ui_base',
      'theme': {
        'name': 'theme-light',
        'lightTheme': {
          'default': '#0094CE',
          'baseColor': '#0094CE',
          'baseFont': '-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif',
          'edited': true,
          'reset': false
        },
        'darkTheme': {
          'default': '#097479',
          'baseColor': '#097479',
          'baseFont': '-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif',
          'edited': false
        },
        'customTheme': {
          'name': 'Untitled Theme 1',
          'default': '#4B7930',
          'baseColor': '#4B7930',
          'baseFont': '-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif'
        },
        'themeState': {
          'base-color': {
            'default': '#0094CE',
            'value': '#0094CE',
            'edited': false
          },
          'page-titlebar-backgroundColor': {
            'value': '#0094CE',
            'edited': false
          },
          'page-backgroundColor': {
            'value': '#fafafa',
            'edited': false
          },
          'page-sidebar-backgroundColor': {
            'value': '#ffffff',
            'edited': false
          },
          'group-textColor': {
            'value': '#1bbfff',
            'edited': false
          },
          'group-borderColor': {
            'value': '#ffffff',
            'edited': false
          },
          'group-backgroundColor': {
            'value': '#ffffff',
            'edited': false
          },
          'widget-textColor': {
            'value': '#111111',
            'edited': false
          },
          'widget-backgroundColor': {
            'value': '#0094ce',
            'edited': false
          },
          'widget-borderColor': {
            'value': '#ffffff',
            'edited': false
          },
          'base-font': {
            'value': '-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif'
          }
        },
        'angularTheme': {
          'primary': 'indigo',
          'accents': 'blue',
          'warn': 'red',
          'background': 'grey',
          'palette': 'light'
        }
      },
      'site': {
        'name': 'Node-RED Dashboard',
        'hideToolbar': 'false',
        'allowSwipe': 'false',
        'lockMenu': 'false',
        'allowTempTheme': 'true',
        'dateFormat': 'DD/MM/YYYY',
        'sizes': {
          'sx': 48,
          'sy': 48,
          'gx': 6,
          'gy': 6,
          'cx': 6,
          'cy': 6,
          'px': 0,
          'py': 0
        }
      }
    };
  }

  private mqttIn(x: number, y: number, tabId: string, mqttBrokerId: string, topic: string, wires: string[]) {
    return {
      'id': this.generateId(),
      'type': 'mqtt in',
      'z': tabId,
      'name': '',
      topic,
      'qos': '2',
      'datatype': 'json',
      'broker': mqttBrokerId,
      'nl': false,
      'rap': true,
      'rh': 0,
      'inputs': 0,
      x,
      y,
      wires: [wires]
    };
  }

  private topicFilter(x: number, y: number, id: string, tabId: string, name: string, regex: string, wires: string[]) {
    return     {
      id,
      'type': 'switch',
      'z': tabId,
      'name': name,
      'property': 'topic',
      'propertyType': 'msg',
      'rules': [
        {
          't': 'regex',
          'v': regex,
          'vt': 'str',
          'case': false
        }
      ],
      'checkall': 'true',
      'repair': false,
      'outputs': 1,
      x,
      y,
      wires: [wires]
    };
  }

  private join(x: number, y: number, id: string, tabId: string, build: string, count: number, wires: string[]) {
    return  {
      id,
      'type': 'join',
      'z': tabId,
      'name': '',
      'mode': 'custom',
      build,
      'property': 'payload',
      'propertyType': 'msg',
      'key': 'topic',
      'joiner': '\\n',
      'joinerType': 'str',
      'accumulate': true,
      'timeout': '',
      count,
      'reduceRight': false,
      'reduceExp': '',
      'reduceInit': '',
      'reduceInitType': '',
      'reduceFixup': '',
      x,
      y,
      wires: [wires]
    };
  }

  private uiTable(x: number, y: number, id: string, tabId: string, uiGroupId: string, name: string, width: number, height: number) {
    return     {
      id,
      'type': 'ui_table',
      'z': tabId,
      'group': uiGroupId,
      name,
      'order': 1,
      width,
      height,
      'columns': [],
      'outputs': 0,
      'cts': false,
      x,
      y,
      'wires': []
    };
  }

  private generalEventPrepareData(x: number, y: number, id: string, tabId: string, wires: string[]) {
    return {
      id,
      'type': 'function',
      'z': tabId,
      'name': 'prepareData',
      'func': 'return {\n    topic: msg.topic,\n    payload: [\n        {\'Parameter\': \'Letzte Abfrage durch Sunny Home Manager\', \'Wert\': msg.payload[\'sae/Event/SempDevice2EM\']?.time ?? \'-\'},\n        {\'Parameter\': \'Letzter Befehl vom Sunny Home Manager\', \'Wert\': msg.payload[\'sae/Event/SempEM2Device\']?.time ?? \'-\'}\n    ]\n}\n',
      'outputs': 1,
      'noerr': 0,
      'initialize': '',
      'finalize': '',
      'libs': [],
      x,
      y,
      wires: [wires]
    };
  }

  private appliancePrepareData(x: number, y: number, id: string, tabId: string, wires: string[]) {
    return {
      id,
      'type': 'function',
      'z': tabId,
      'name': 'prepareData',
      'func': 'if(msg.payload.applianceInfo.type === \'EVCharger\') {\n    if(msg.payload.newState === \'VEHICLE_NOT_CONNECTED\') {\n        return {\n            topic: msg.topic,\n            payload: [\n                {\'Parameter\': \'name\', \'Wert\': msg.payload.applianceInfo.name},\n                {\'Parameter\': \'vendor\', \'Wert\': msg.payload.applianceInfo.vendor},\n                {\'Parameter\': \'typ\', \'Wert\': msg.payload.applianceInfo.type},\n                {\'Parameter\': \'state\', \'Wert\': msg.payload.newState},\n                {\'Parameter\': \'minPowerConsumption\', \'Wert\': msg.payload.applianceInfo.minPowerConsumption},\n                {\'Parameter\': \'maxPowerConsumption\', \'Wert\': msg.payload.applianceInfo.maxPowerConsumption},\n                {\'Parameter\': \'interruptionsAllowed\', \'Wert\': msg.payload.applianceInfo.interruptionsAllowed}\n            ]\n        }\n    }\n    return {\n        topic: msg.topic,\n        payload: [\n            {\'Parameter\': \'name\', \'Wert\': msg.payload.applianceInfo.name},\n            {\'Parameter\': \'vendor\', \'Wert\': msg.payload.applianceInfo.vendor},\n            {\'Parameter\': \'typ\', \'Wert\': msg.payload.applianceInfo.type},\n            {\'Parameter\': \'state\', \'Wert\': msg.payload.newState},\n            {\'Parameter\': \'socCurrent\', \'Wert\': msg.payload.socValues?.current},\n            {\'Parameter\': \'socInitial\', \'Wert\': msg.payload.socValues?.initial},\n            {\'Parameter\': \'socInitialTime\', \'Wert\': msg.payload.socInitialTime},\n            {\'Parameter\': \'socRetrieved\', \'Wert\': msg.payload.socValues?.retrieved},\n            {\'Parameter\': \'socRetrievedTime\', \'Wert\': msg.payload.socRetrievedTime},\n            {\'Parameter\': \'chargePower\', \'Wert\': msg.payload.chargePower},\n            {\'Parameter\': \'chargeLoss\', \'Wert\': msg.payload.chargeLoss},\n            {\'Parameter\': \'batteryCapacity\', \'Wert\': msg.payload.socValues?.batteryCapacity},\n            {\'Parameter\': \'minPowerConsumption\', \'Wert\': msg.payload.applianceInfo.minPowerConsumption},\n            {\'Parameter\': \'maxPowerConsumption\', \'Wert\': msg.payload.applianceInfo.maxPowerConsumption},\n            {\'Parameter\': \'interruptionsAllowed\', \'Wert\': msg.payload.applianceInfo.interruptionsAllowed}\n        ]\n    }\n}\nreturn {\n    topic: msg.topic,\n    payload: [\n        {\'Parameter\': \'name\', \'Wert\': msg.payload.applianceInfo.name},\n        {\'Parameter\': \'vendor\', \'Wert\': msg.payload.applianceInfo.vendor},\n        {\'Parameter\': \'typ\', \'Wert\': msg.payload.applianceInfo.type},\n        {\'Parameter\': \'maxPowerConsumption\', \'Wert\': msg.payload.applianceInfo.maxPowerConsumption},\n        {\'Parameter\': \'interruptionsAllowed\', \'Wert\': msg.payload.applianceInfo.interruptionsAllowed}\n    ]\n}',
      'outputs': 1,
      'noerr': 0,
      'initialize': '',
      'finalize': '',
      'libs': [],
      x,
      y,
      wires: [wires]
    };
  }

  private controlChange(x: number, y: number, id: string, tabId: string, wires: string[]) {
    return     {
      id,
      'type': 'change',
      'z': tabId,
      'name': 'on',
      'rules': [
        {
          't': 'set',
          'p': 'payload',
          'pt': 'msg',
          'to': 'payload.on',
          'tot': 'msg'
        }
      ],
      'action': '',
      'property': '',
      'from': '',
      'to': '',
      'reg': false,
      x,
      y,
      wires: [wires]
    };
  }

  private controlInDashboard(x: number, y: number, id: string, tabId: string, uiGroupId: string) {
    return     {
      id,
      'type': 'ui_switch',
      'z': tabId,
      'name': 'Control',
      'label': '',
      'tooltip': '',
      'group': uiGroupId,
      'order': 2,
      'width': 2,
      'height': 5,
      'passthru': true,
      'decouple': 'false',
      'topic': 'topic',
      'topicType': 'msg',
      'style': '',
      'onvalue': 'true',
      'onvalueType': 'bool',
      'onicon': '',
      'oncolor': '',
      'offvalue': 'false',
      'offvalueType': 'bool',
      'officon': '',
      'offcolor': '',
      'animate': false,
      'className': '',
      x,
      y,
      'wires': [
        []
      ]
    };
  }

  private meterChartPrepareData(x: number, y: number, id: string, tabId: string, meterDisplayedInDashboardId: string) {
    return  {
      id,
      'type': 'function',
      'z': tabId,
      'name': 'prepareData',
      'func': 'const power = msg.payload.power;\nconst powerThreshold = msg.payload.powerThreshold;\nvar msg1 = {topic: \'Leistung\', payload: !!power ? power : 0};\nvar msg2 = {topic: \'Leistungsschaltgrenze\', payload: powerThreshold};\nif(powerThreshold) {\n    return [msg1, msg2];    \n}\nreturn [msg1];',
      'outputs': 2,
      'noerr': 0,
      'initialize': '',
      'finalize': '',
      'libs': [],
      x,
      y,
      'wires': [
        [
          meterDisplayedInDashboardId
        ],
        [
          meterDisplayedInDashboardId
        ]
      ]
    };
  }

  private meterChartInDashboard(x: number, y: number, id: string, tabId: string, uiGroupId: string) {
    return {
      id,
      'type': 'ui_chart',
      'z': tabId,
      'name': 'Meter',
      'group': uiGroupId,
      'order': 4,
      'width': 9,
      'height': '6',
      'label': 'Leistung',
      'chartType': 'line',
      'legend': 'true',
      'xformat': 'HH:mm:ss',
      'interpolate': 'linear',
      'nodata': '',
      'dot': false,
      'ymin': '0',
      'ymax': '',
      'removeOlder': '10',
      'removeOlderPoints': '',
      'removeOlderUnit': '60',
      'cutout': 0,
      'useOneColor': false,
      'useUTC': false,
      'colors': [
        '#1100ff',
        '#ff0000',
        '#ff7f0e',
        '#ff0000',
        '#98df8a',
        '#d62728',
        '#ff9896',
        '#9467bd',
        '#c5b0d5'
      ],
      'outputs': 1,
      'useDifferentColor': false,
      'className': '',
      x,
      y,
      'wires': [
        []
      ]
    };
  }

  private meterFiguresPrepareData(x: number, y: number, id: string, tabId: string, wires: string[]) {
    return {
      id,
      'type': 'function',
      'z': tabId,
      'name': 'prepareData',
      'func': 'const energyString = new Intl.NumberFormat(undefined,\n    { minimumFractionDigits: 2, maximumFractionDigits: 2 }\n).format(msg.payload.energy);\n\nreturn {\n    topc: msg.topic,\n    payload: `${msg.payload.power} W<BR>${energyString} kWh`\n}\n\n',
      'outputs': 1,
      'noerr': 0,
      'initialize': '',
      'finalize': '',
      'libs': [],
      x,
      y,
      wires: [wires]
    };
  }

  private meterFiguresInDashboard(x: number, y: number, id: string, tabId: string, uiGroupId: string) {
    return {
      id,
      'type': 'ui_text',
      'z': tabId,
      'group': uiGroupId,
      'order': 3,
      'width': '2',
      'height': 5,
      'name': 'Meter',
      'label': '',
      'format': '{{msg.payload}}',
      'layout': 'row-spread',
      'className': '',
      x,
      y,
      'wires': []
    };
  }

  private timeframeIntervalsChartPrepareData(x: number, y: number, id: string, tabId: string, wires: string[]) {
    return  {
      id,
      'type': 'function',
      'z': tabId,
      'name': 'generateChart',
      'func': 'var datefns = global.get(\'datefns\');\nconst dateFormat = \'yyyy-MM-dd HH:mm:ss\';\n\nconst startDateTime = new Date().setUTCHours(0, 0, 0, 0);\nconst endDateTime = datefns.addDays(new Date(startDateTime), 2);\n\nconst optionalEntries = msg.payload.entries.filter(entry => entry.min !== null && !!entry.max && entry.min !== entry.max)\nconst optionalEnabledEntries = optionalEntries.filter(entry => entry.enabled)\nconst optionalDisabledEntries = optionalEntries.filter(entry => !entry.enabled)\n\nconst requiredEntries = msg.payload.entries.filter(entry =>\n  (entry.type === \'RuntimeRequest\' && entry.min === null && entry.max > 0) || entry.type === \'SocRequest\');\nconst requiredEnabledEntries = requiredEntries.filter(entry => entry.enabled)\nconst requiredDisabledEntries = requiredEntries.filter(entry => !entry.enabled)\n\nconst buildOptionalDataItem = (entry) => {\n    return {\n        timeRange: [\n            entry.start,\n            entry.end\n        ],\n        val: entry.max - entry.min\n    }\n};\n\nconst buildRequiredDataItem = (entry) => {\n    return {\n        timeRange: [\n            entry.start,\n            entry.end\n        ],\n        val: entry.max\n    }\n};\n\nreturn {\n    topic: msg.topic,\n    payload: {\n        dataItems: [\n            {\n                group: "optional",\n                data: [\n                    {\n                        label: "disabled",\n                        data: optionalDisabledEntries.map(entry => buildOptionalDataItem(entry))\n                    },\n                    {\n                        label: "enabled",\n                        data: optionalEnabledEntries.map(entry => buildOptionalDataItem(entry))\n                    }\n                ]\n            },\n            {\n                group: "required",\n                data: [\n                    {\n                        label: "disabled",\n                        data: requiredDisabledEntries.map(entry => buildRequiredDataItem(entry))\n                    },\n                    {\n                        label: "enabled",\n                        data: requiredEnabledEntries.map(entry => buildRequiredDataItem(entry))\n                    }\n                ]\n            }\n        ],\n        settings: {\n            xAxis: {\n                startDateTime: datefns.format(startDateTime, dateFormat),\n                endDateTime: datefns.format(endDateTime, dateFormat),\n            },\n            chart: {\n                height: 20,\n                topMargin: 0\n            }\n        }\n    }\n}\n',
      'outputs': 1,
      'noerr': 0,
      'initialize': '',
      'finalize': '',
      'libs': [],
      x,
      y,
      wires: [wires]
    };
  }

  private timeframeIntervalsChartInDashboard(x: number, y: number, id: string, tabId: string, uiGroupId: string) {
    return {
      id,
      'type': 'ui_timelines_chart',
      'z': tabId,
      'group': uiGroupId,
      'name': 'Timeframes',
      'order': 5,
      'label': 'Timeframes',
      'width': '27',
      'height': '5',
      'lineColors': [
        {
          'statusColor': '#ff0000',
          'statusValue': ''
        }
      ],
      'xTickFormat': 'HH:mm',
      'startDateTime': '',
      'endDateTime': '',
      'maxLineHeight': 60,
      'xAxisLabelsFontSize': 16,
      'xAxisLabelslColor': '#000000',
      'yAxisLabelsFontSize': 16,
      'yAxisLabelslColor': '#000000',
      'resetZoomLabelFontSize': 24,
      'resetZoomLabelColor': '#000000',
      'enableAnimations': false,
      'enableDateMarker': false,
      'forwardInputMessages': false,
      x,
      y,
      'wires': [
        []
      ]
    };
  }

  // source: https://github.com/node-red/node-red/blob/master/packages/node_modules/%40node-red/util/lib/util.js
  private generateId() {
    const bytes = [];
    for (let i = 0; i < 8; i++) {
      bytes.push(Math.round(0xff * Math.random()).toString(16).padStart(2, '0'));
    }
    return bytes.join('');
  }
}
