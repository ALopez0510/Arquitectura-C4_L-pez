import { useState } from 'react'
import { api } from './api'
import { normalizarPlaca, validarCorreo, validarPlaca, validarRuc } from './validacion'
import Campo from './componentes/Campo'
import MensajeError from './componentes/MensajeError'
import AvisoFrescura from './componentes/AvisoFrescura'
import PanelSimulacion from './componentes/PanelSimulacion'

const PASOS = ['Identificación', 'Vehículo', 'Puntos de licencia']
const dinero = new Intl.NumberFormat('es-EC', { style: 'currency', currency: 'USD' })

function Dato({ etiqueta, valor }) {
  return (
    <div className="dato">
      <dt>{etiqueta}</dt>
      <dd>{valor ?? '—'}</dd>
    </div>
  )
}

export default function App() {
  const [correo, setCorreo] = useState('')
  const [ruc, setRuc] = useState('')
  const [placa, setPlaca] = useState('')
  const [erroresCampos, setErroresCampos] = useState({})

  const [verificacion, setVerificacion] = useState(null) // { datos, metadatos }
  const [vehiculo, setVehiculo] = useState(null)
  const [puntos, setPuntos] = useState(null)

  const [cargando, setCargando] = useState(null) // 'contribuyente' | 'vehiculo' | 'puntos'
  const [error, setError] = useState(null)

  const paso = !verificacion ? 0 : !vehiculo ? 1 : 2
  const contribuyente = verificacion?.datos.contribuyente

  async function verificar(e) {
    e.preventDefault()
    const errores = { correo: validarCorreo(correo), ruc: validarRuc(ruc) }
    setErroresCampos(errores)
    if (errores.correo || errores.ruc) return
    setError(null)
    setCargando('contribuyente')
    try {
      setVerificacion(await api.verificarContribuyente(correo.trim(), ruc.trim()))
    } catch (err) {
      setError(err)
    } finally {
      setCargando(null)
    }
  }

  async function consultarVehiculo(e) {
    e?.preventDefault()
    const errorPlaca = validarPlaca(placa)
    setErroresCampos({ placa: errorPlaca })
    if (errorPlaca) return
    const placaNormal = normalizarPlaca(placa)
    setPlaca(placaNormal)
    setError(null)
    setCargando('vehiculo')
    try {
      const v = await api.consultarVehiculo(placaNormal)
      setVehiculo(v)
      setCargando('puntos')
      setPuntos(await api.consultarPuntos(contribuyente.cedula, placaNormal))
    } catch (err) {
      setError(err)
    } finally {
      setCargando(null)
    }
  }

  async function actualizarPuntos() {
    setError(null)
    setCargando('puntos')
    try {
      setPuntos(await api.consultarPuntos(contribuyente.cedula, vehiculo.datos.placa, true))
    } catch (err) {
      setError(err)
    } finally {
      setCargando(null)
    }
  }

  function reiniciar() {
    setVerificacion(null)
    setVehiculo(null)
    setPuntos(null)
    setPlaca('')
    setError(null)
    setErroresCampos({})
  }

  function cambiarVehiculo() {
    setVehiculo(null)
    setPuntos(null)
    setError(null)
  }

  return (
    <div className="pagina">
      <header className="cabecera">
        <h1>Consulta vehicular</h1>
        <p>Verifique un contribuyente en el SRI, los datos de su vehículo y los puntos de su licencia en la ANT.</p>
      </header>

      <ol className="pasos" aria-label="Progreso">
        {PASOS.map((nombre, i) => (
          <li key={nombre} className={i < paso ? 'hecho' : i === paso ? 'actual' : ''} aria-current={i === paso ? 'step' : undefined}>
            <span>{i + 1}</span> {nombre}
          </li>
        ))}
      </ol>

      <MensajeError error={error} />

      {!verificacion && (
        <form className="tarjeta" onSubmit={verificar} noValidate>
          <h2>1. Identificación</h2>
          <Campo
            id="correo"
            etiqueta="Correo electrónico"
            type="email"
            autoComplete="email"
            placeholder="nombre@dominio.com"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            error={erroresCampos.correo}
            ayuda="Lo usaremos solo para identificar la consulta."
          />
          <Campo
            id="ruc"
            etiqueta="RUC de persona natural"
            inputMode="numeric"
            maxLength={13}
            placeholder="1710034065001"
            value={ruc}
            onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))}
            error={erroresCampos.ruc}
            ayuda="13 dígitos: su cédula seguida de 001."
          />
          <button className="boton" type="submit" disabled={cargando === 'contribuyente'}>
            {cargando === 'contribuyente' ? 'Verificando en el SRI…' : 'Verificar contribuyente'}
          </button>
        </form>
      )}

      {contribuyente && (
        <section className="tarjeta">
          <div className="tarjeta__titulo">
            <h2>Contribuyente</h2>
            <button type="button" className="boton boton--enlace" onClick={reiniciar}>Nueva consulta</button>
          </div>
          <p className="destacado">{contribuyente.razonSocial}</p>
          <dl className="datos">
            <Dato etiqueta="RUC" valor={contribuyente.ruc} />
            <Dato etiqueta="Tipo" valor={contribuyente.tipoContribuyente} />
            <Dato etiqueta="Estado" valor={contribuyente.estado} />
            <Dato etiqueta="Régimen" valor={contribuyente.regimen} />
            <Dato etiqueta="Actividad principal" valor={contribuyente.actividadEconomicaPrincipal} />
            <Dato etiqueta="Inicio de actividades" valor={contribuyente.fechaInicioActividades} />
            <Dato etiqueta="Correo" valor={verificacion.datos.correo} />
          </dl>
          <AvisoFrescura metadatos={verificacion.metadatos} />
        </section>
      )}

      {verificacion && !vehiculo && (
        <form className="tarjeta" onSubmit={consultarVehiculo} noValidate>
          <h2>2. Vehículo</h2>
          <Campo
            id="placa"
            etiqueta="Placa del vehículo"
            placeholder="PBX1234"
            maxLength={8}
            value={placa}
            onChange={(e) => setPlaca(e.target.value.toUpperCase())}
            error={erroresCampos.placa}
            ayuda="Formato ABC1234 para autos o AB123C para motos."
          />
          <button className="boton" type="submit" disabled={Boolean(cargando)}>
            {cargando === 'vehiculo' ? 'Consultando el SRI…' : cargando === 'puntos' ? 'Consultando la ANT…' : 'Consultar vehículo y puntos'}
          </button>
        </form>
      )}

      {vehiculo && (
        <section className="tarjeta">
          <div className="tarjeta__titulo">
            <h2>Vehículo {vehiculo.datos.placa}</h2>
            <button type="button" className="boton boton--enlace" onClick={cambiarVehiculo}>Consultar otra placa</button>
          </div>
          <dl className="datos">
            <Dato etiqueta="Marca" valor={vehiculo.datos.marca} />
            <Dato etiqueta="Modelo" valor={vehiculo.datos.modelo} />
            <Dato etiqueta="Año" valor={vehiculo.datos.anio} />
            <Dato etiqueta="País de origen" valor={vehiculo.datos.paisOrigen} />
            <Dato etiqueta="Clase" valor={vehiculo.datos.clase} />
            <Dato etiqueta="Servicio" valor={vehiculo.datos.servicio} />
            <Dato etiqueta="CAMV / CPN" valor={vehiculo.datos.camvCpn} />
            <Dato etiqueta="Último año pagado" valor={vehiculo.datos.ultimoAnioPagado} />
          </dl>
          <AvisoFrescura metadatos={vehiculo.metadatos} />
        </section>
      )}

      {vehiculo && (
        <section className="tarjeta">
          <h2>3. Puntos de licencia</h2>
          {cargando === 'puntos' && !puntos && <p>Consultando la ANT…</p>}
          {puntos && (
            <>
              <div className="puntos">
                <div className={`puntos__valor ${puntos.datos.puntos <= 10 ? 'puntos__valor--bajo' : ''}`}>
                  {puntos.datos.puntos}
                  <span>/ {puntos.datos.puntosMaximos}</span>
                </div>
                <div>
                  <p className="destacado">{contribuyente.razonSocial}</p>
                  <p>Cédula {puntos.datos.identificacion}</p>
                  <p>Total pendiente de pago: <strong>{dinero.format(puntos.datos.totalPendiente)}</strong></p>
                </div>
              </div>
              {puntos.datos.citaciones.length === 0 ? (
                <p>No registra citaciones.</p>
              ) : (
                <div className="tabla-contenedor">
                  <table>
                    <thead>
                      <tr>
                        <th>Citación</th>
                        <th>Fecha</th>
                        <th>Infracción</th>
                        <th>Puntos</th>
                        <th>Valor</th>
                        <th>Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {puntos.datos.citaciones.map((c) => (
                        <tr key={c.numero}>
                          <td>{c.numero}</td>
                          <td>{c.fechaEmision}</td>
                          <td>{c.infraccion}</td>
                          <td>-{c.puntosRestados}</td>
                          <td>{dinero.format(c.valor)}</td>
                          <td><span className={`estado estado--${c.estado.toLowerCase()}`}>{c.estado}</span></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
              <AvisoFrescura metadatos={puntos.metadatos} onActualizar={actualizarPuntos} actualizando={cargando === 'puntos'} />
            </>
          )}
          {!puntos && cargando !== 'puntos' && (
            <button className="boton" type="button" onClick={actualizarPuntos}>Reintentar consulta de puntos</button>
          )}
        </section>
      )}

      <footer className="pie">
        <PanelSimulacion />
        <p>Fuentes: SRI (catastro y matriculación vehicular) y ANT. Proyecto académico ISWZ2202 · UDLA.</p>
      </footer>
    </div>
  )
}
