import { useEffect, useState } from 'react'
import { api } from '../api'

const ESTADOS = [
  { valor: 'DISPONIBLE', texto: 'Disponible' },
  { valor: 'CAIDO', texto: 'Caída (HTTP 502)' },
  { valor: 'LENTO', texto: 'Lenta (timeout)' },
]

// Panel para demostrar la resiliencia cambiando el comportamiento de la ANT simulada.
export default function PanelSimulacion() {
  const [info, setInfo] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.estadoSimulacion().then(setInfo).catch((e) => setError(e.message))
  }, [])

  const cambiar = async (estado) => {
    try {
      setInfo(await api.cambiarSimulacion(estado))
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }

  if (error) return <p className="simulacion simulacion--error">Simulación ANT: {error}</p>
  if (!info) return null
  if (info.modo === 'real') return <p className="simulacion">ANT: conectado al servicio real.</p>

  return (
    <details className="simulacion">
      <summary>
        Simulación de la ANT: <strong>{ESTADOS.find((e) => e.valor === info.estado)?.texto}</strong>
      </summary>
      <p>
        La web de la ANT respondió 502 Bad Gateway, así que se usa un servicio simulado con el mismo contrato.
        Cambie su estado para probar el tiempo de espera ({info.tiempoEsperaSegundos} s) y el respaldo en caché.
      </p>
      <div className="simulacion__botones">
        {ESTADOS.map((e) => (
          <button
            key={e.valor}
            type="button"
            className={`boton boton--chico ${info.estado === e.valor ? 'boton--activo' : ''}`}
            onClick={() => cambiar(e.valor)}
          >
            {e.texto}
          </button>
        ))}
      </div>
    </details>
  )
}
