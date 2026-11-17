// Cliente de los microservicios. Cada error de la API llega como
// { codigo, mensaje, estado, detalles: [{ campo, mensaje }] }.

const API_SRI = import.meta.env.VITE_API_SRI ?? ''
const API_ANT = import.meta.env.VITE_API_ANT ?? ''

export class ErrorApi extends Error {
  constructor({ codigo, mensaje, estado, detalles }) {
    super(mensaje)
    this.codigo = codigo
    this.estado = estado
    this.detalles = detalles ?? []
  }
}

async function solicitar(url, opciones = {}) {
  let respuesta
  try {
    respuesta = await fetch(url, {
      ...opciones,
      headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...(opciones.headers ?? {}) },
    })
  } catch {
    throw new ErrorApi({
      codigo: 'SIN_CONEXION',
      mensaje: 'No se pudo conectar con el servidor. Verifique que los microservicios estén en ejecución.',
      estado: 0,
    })
  }
  const cuerpo = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new ErrorApi(cuerpo ?? { codigo: 'ERROR_DESCONOCIDO', mensaje: `Error HTTP ${respuesta.status}`, estado: respuesta.status })
  }
  return cuerpo
}

export const api = {
  verificarContribuyente: (correo, ruc) =>
    solicitar(`${API_SRI}/api/v1/contribuyentes/verificacion`, {
      method: 'POST',
      body: JSON.stringify({ correo, ruc }),
    }),

  consultarVehiculo: (placa, refrescar = false) =>
    solicitar(`${API_SRI}/api/v1/vehiculos/${encodeURIComponent(placa)}?refrescar=${refrescar}`),

  consultarPuntos: (cedula, placa, refrescar = false) =>
    solicitar(
      `${API_ANT}/api/v1/licencias/${encodeURIComponent(cedula)}/puntos?placa=${encodeURIComponent(placa ?? '')}&refrescar=${refrescar}`,
    ),

  estadoSimulacion: () => solicitar(`${API_ANT}/api/v1/simulacion/ant`),

  cambiarSimulacion: (estado) =>
    solicitar(`${API_ANT}/api/v1/simulacion/ant`, { method: 'PUT', body: JSON.stringify({ estado }) }),
}
