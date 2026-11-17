export default function Campo({ id, etiqueta, ayuda, error, ...props }) {
  return (
    <div className={`campo ${error ? 'campo--error' : ''}`}>
      <label htmlFor={id}>{etiqueta}</label>
      <input id={id} aria-invalid={Boolean(error)} aria-describedby={`${id}-ayuda`} {...props} />
      <small id={`${id}-ayuda`}>{error ?? ayuda}</small>
    </div>
  )
}
