export default function MensajeError({ error }) {
  if (!error) return null
  return (
    <div className="aviso aviso--error" role="alert">
      <strong>{error.message}</strong>
      {error.detalles?.length > 0 && (
        <ul>
          {error.detalles.map((d, i) => (
            <li key={i}>{d.mensaje}</li>
          ))}
        </ul>
      )}
      {error.codigo && <span className="aviso__codigo">Código: {error.codigo}</span>}
    </div>
  )
}
