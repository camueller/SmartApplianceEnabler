export function stringyfyWithoutFalsyNumbers(object: any): string {
  const clone =  JSON.parse(JSON.stringify(object));
  Object.keys(clone).forEach((key) => {
    if (clone[key] === null) {
      delete clone[key];
    }
  });
  return JSON.stringify(clone);
}
