export function getValidString(input: any): string | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? input : undefined;
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
