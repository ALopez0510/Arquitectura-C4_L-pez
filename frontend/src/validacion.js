// Validaciones en el navegador (el backend vuelve a validar todo).

export function validarCorreo(correo) {
  if (!correo.trim()) return 'Ingrese su correo electrónico.'
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]{2,}$/.test(correo.trim())) return 'El correo no tiene un formato válido (ejemplo: nombre@dominio.com).'
  return null
}

export function validarRuc(ruc) {
  const valor = ruc.trim()
  if (!valor) return 'Ingrese el RUC.'
  if (!/^\d+$/.test(valor)) return 'El RUC solo puede contener números.'
  if (valor.length !== 13) return `El RUC debe tener 13 dígitos (tiene ${valor.length}).`
  const provincia = Number(valor.slice(0, 2))
  if (!((provincia >= 1 && provincia <= 24) || provincia === 30)) return 'Los dos primeros dígitos no corresponden a una provincia.'
  return null
}

export function normalizarPlaca(placa) {
  return placa.replace(/[\s-]/g, '').toUpperCase()
}

export function validarPlaca(placa) {
  const valor = normalizarPlaca(placa)
  if (!valor) return 'Ingrese la placa del vehículo.'
  if (!/^[A-Z]{3}\d{3,4}$/.test(valor) && !/^[A-Z]{2}\d{3}[A-Z]$/.test(valor)) {
    return 'Use el formato ABC1234 (auto) o AB123C (moto).'
  }
  return null
}
