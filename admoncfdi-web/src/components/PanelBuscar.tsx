import { useState } from 'react'
import { useLoadSuppliers } from '../services/hooks/useSuppliers';
import BuscadorPorFecha from './BuscadorPorFecha';
import BuscadorPorProveedor from './BuscadorPorProveedor';
import FilesPopup from './FilesPopup';
import TblResults from './TblResults';
import type { CfdiRecibidoVO, ComprobanteVO } from '../services/api/documentService';


const TipoDeBuscador = {
    FechaFactura: "FechaFactura",
    FechaSistema: "FechaSistema",
    Proveedor: "Proveedor",
} as const;

type TipoDeBuscador = typeof TipoDeBuscador[keyof typeof TipoDeBuscador];

const PanelBuscar = () => {
    const [resultados, setResultados] = useState<{ comprobantes: CfdiRecibidoVO[] | null, pagos: ComprobanteVO[] | null }>({ comprobantes: null, pagos: null });
    const [tipoDeBuscador, setTipoDeBuscador] = useState<TipoDeBuscador>(TipoDeBuscador.FechaFactura);
    const { data: proveedores } = useLoadSuppliers();
    const [selectedComprobante, setSelectedComprobante] = useState<ComprobanteVO | null>(null);
    const [showPopup, setShowPopup] = useState<boolean>(false);
    const handleSelector = (tipo: TipoDeBuscador) => {
        setTipoDeBuscador(tipo);
    };

    const handleRowClick = (comprobante: ComprobanteVO) => {
        setSelectedComprobante(comprobante);
        setShowPopup(true);
    };

    const handleClosePopup = () => {
        setShowPopup(false);
        setSelectedComprobante(null);
    };

    return (
        <div className="rounded-[18px] border border-white/10 bg-linear-to-b from-[#16263D] to-[#16263D]/60 p-7">
            <section className='pb-5'>
                <p className='text-center relative pb-3 font-semibold text-[14.5px] transition-colors
              text-slate-100'>Buscar Por</p>
                <section className="flex justify-between">
                    <div>
                        <input
                            type="radio"
                            id="cfdi"
                            name="buscador"
                            value="cfdi"
                            checked={tipoDeBuscador === TipoDeBuscador.FechaFactura}
                            onChange={() => handleSelector(TipoDeBuscador.FechaFactura)}
                        />
                        <label className="px-3 font-mono text-[13px] tracking-widest text-slate-400" htmlFor="cfdi">Fecha CFDI</label>
                    </div>
                    <div>
                        <input
                            type="radio"
                            id="sistema"
                            name="buscador"
                            value="sistema"
                            checked={tipoDeBuscador === TipoDeBuscador.FechaSistema}
                            onChange={() => handleSelector(TipoDeBuscador.FechaSistema)}
                        />
                        <label className="px-3 font-mono text-[13px] tracking-widest text-slate-400" htmlFor="sistema">Fecha sistema</label>
                    </div>
                    <div>
                        <input
                            type="radio"
                            id="proveedor"
                            name="buscador"
                            value="proveedor"
                            checked={tipoDeBuscador === TipoDeBuscador.Proveedor}
                            onChange={() => handleSelector(TipoDeBuscador.Proveedor)}
                        />
                        <label className="px-3 font-mono text-[13px] tracking-widest text-slate-400" htmlFor="proveedor">Proveedor</label>
                    </div>
                </section>
            </section>

            {tipoDeBuscador == TipoDeBuscador.Proveedor ?
                <BuscadorPorProveedor proveedores={proveedores || []} onResults={({ comprobantes, pagos }) => setResultados({ comprobantes, pagos })} /> :
                <BuscadorPorFecha />
            }

            <div className="mt-6 scrollbar-thin overflow-y-auto">
                {resultados.comprobantes === null && (
                    <div className="rounded-xl border border-dashed border-white/15 py-8 text-center font-mono text-sm text-slate-400">
                        Realiza una búsqueda para ver resultados aquí.
                    </div>
                )}

                {resultados.comprobantes !== null && resultados.comprobantes.length === 0 && (
                    <div className="rounded-xl border border-dashed border-white/15 py-8 text-center font-mono text-sm text-slate-400">
                        No se encontraron documentos con esos criterios.
                    </div>
                )}

                {resultados.comprobantes !== null && resultados.comprobantes.length > 0 && (
                    <TblResults comprobantes={resultados.comprobantes} handleClick={(comprobanteVO: ComprobanteVO) => handleRowClick(comprobanteVO)} />
                )}
                {resultados.pagos !== null && resultados.pagos.length > 0 && (
                    <TblResults comprobantes={resultados.pagos} handleClick={(comprobanteVO: ComprobanteVO) => handleRowClick(comprobanteVO)} />
                )}

                {showPopup && selectedComprobante && (
                    <FilesPopup comprobanteVO={selectedComprobante} onClose={handleClosePopup} />
                )}
            </div>
        </div>
    );

}

export default PanelBuscar