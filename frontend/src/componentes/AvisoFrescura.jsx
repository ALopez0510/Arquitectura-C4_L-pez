// Muestra de dónde viene el dato y avisa cuando no es reciente.

const formato = new Intl.DateTimeFormat('es-EC', { dateStyle: 'short', timeStyle: 'short' })

export default function AvisoFrescura({ metadatos, onActualizar, actualizando }) {
  if (!metadatos) return null
  const fecha = metadatos.obtenidoEn ? formato.format(new Date(metadatos.obtenidoEn)) : '—'
  const etiquetas = {
    SERVICIO_EXTERNO: 'Consultado ahora',
    CACHE: 'Desde caché (vigente)',
    CACHE_RESPALDO: 'Último dato guardado',
  }

  return (
    <div className="frescura">
      {metadatos.desactualizado && (
        <div className="aviso aviso--alerta" role="alert">
          <strong>Dato posiblemente desactualizado.</strong> {metadatos.aviso}
        </div>
      )}
      <div className="frescura__linea">
        <span className={`etiqueta etiqueta--${metadatos.origen.toLowerCase()}`}>{etiquetas[metadatos.origen]}</span>
        {metadatos.simulado && <span className="etiqueta etiqueta--simulado">Datos simulados</span>}
        <span className="frescura__texto">
          Fuente: {metadatos.fuente} · obtenido el {fecha}
        </span>
        {onActualizar && (
          <button type="button" className="boton boton--enlace" onClick={onActualizar} disabled={actualizando}>
            {actualizando ? 'Actualizando…' : 'Actualizar'}
          </button>
        )}
      </div>
    </div>
  )
}
