/** Generadores de credenciales para altas que crean cuenta de usuario. */

const ALNUM = 'abcdefghijklmnopqrstuvwxyz0123456789';
const STRONG =
  'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+[]{}|;:,.<>?';

function randomString(length: number, alphabet: string): string {
  let out = '';
  for (let i = 0; i < length; i++) {
    out += alphabet.charAt(Math.floor(Math.random() * alphabet.length));
  }
  return out;
}

export function generateEmail(): string {
  return `user${randomString(10, ALNUM)}@somoslamayoria.com`;
}

export function generatePassword(): string {
  return randomString(8, STRONG);
}

export function generatePhrase(): string {
  return randomString(8, STRONG);
}
