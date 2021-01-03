export function simpleMeterType(meterType: string) {
  return meterType && meterType.split('.')[4];
}

export function simpleControlType(controlType: string) {
  return controlType && controlType.split('.')[4];
}

export function simpleTimeframeType(timeframeType: string) {
  return timeframeType && timeframeType.split('.')[4];
}

export function simpleRequestType(requestType: string) {
  return requestType && requestType.split('.')[4];
}

export function getValidString(input: any): string | undefined {
  if (!input || input.length === 0) {
    return undefined;
  }
  return input.toString()
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

export function getValidInt(input: any): number | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? Number.parseInt(input, 10) : undefined;
}

export function getValidFloat(input: any): number | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? Number.parseFloat(input) : undefined;
}
