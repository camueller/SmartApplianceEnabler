import {ErrorMessage} from '../shared/ErrorMessage';

export const ErrorMessages = [
  new ErrorMessage('id', 'required', 'Die ID muss muss angegeben werden'),
  new ErrorMessage('id', 'pattern', 'Die ID muss folgendem Schema entsprechen, wobei nur die Ziffern geändert ' +
    'werden dürfen: F-00000001-000000000001-00'),
  new ErrorMessage('vendor', 'required', 'Der Hesteller muss angegeben werden'),
  new ErrorMessage('name', 'required', 'Der Name muss angegeben werden'),
  new ErrorMessage('serial', 'required', 'Die Seriennummer muss angegeben werden'),
  new ErrorMessage('maxPowerConsumption', 'required', 'Die maximale Leistungsaufnahme muss angegeben werden'),
  new ErrorMessage('maxPowerConsumption', 'pattern', 'Die maximale Leistungsaufnahme muss in Watt angegeben werden')
]
