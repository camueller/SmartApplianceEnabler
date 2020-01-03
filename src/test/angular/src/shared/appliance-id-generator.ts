/**
 * Generate an appliance id of format: F-00000001-000000000001-00
 */
export function generateApplianceId(): string {
  const id = Math.floor(Math.random() * 999999999999) + 1;
  return `F-00000001-${id.toString().padStart(12, '0')}-00`;
}
