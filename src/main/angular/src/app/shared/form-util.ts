export function getValidString(input: any): string | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? input : undefined;
}
