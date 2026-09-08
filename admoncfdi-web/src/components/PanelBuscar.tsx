import { useState } from 'react';
import { useLoadSuppliers } from '../services/hooks/useSuppliers';
import BuscadorPorFecha from './BuscadorPorFecha';
import BuscadorPorProveedor from './BuscadorPorProveedor';
import FilesPopup from './FilesPopup';
import TblResults from './TblResults';
import { TipoDeBuscador, type CfdiRecibidoVO, type ComprobanteVO, type PageResult } from '../types';

const PanelBuscar = () => {
    const [resultados, setResultados] = useState<{
        comprobantes: PageResult<CfdiRecibidoVO> | CfdiRecibidoVO[] | null;
        pagos: PageResult<ComprobanteVO> | ComprobanteVO[] | null;
        onPageChangeComprobantes?: (page: number) => void;
        onPageChangePagos?: (page: number) => void;
    }>({
        comprobantes: null,
        pagos: null,
    });
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

    const countItems = (items: PageResult<any> | any[] | null) => {
        if (!items) return 0;
        return Array.isArray(items) ? items.length : items.totalElements;
    };

    const hasComprobantes = resultados.comprobantes !== null && countItems(resultados.comprobantes) > 0;
    const hasPagos = resultados.pagos !== null && countItems(resultados.pagos) > 0;
    const isSearchDone = resultados.comprobantes !== null || resultados.pagos !== null;
    const isEmpty = isSearchDone && !hasComprobantes && !hasPagos;

    return (
        <div className="rounded-[18px] border border-white/10 bg-linear-to-b from-[#16263D] to-[#16263D]/60 p-7">
            <section className="pb-5">
                <p className="pb-3 text-center font-mono text-xs uppercase tracking-widest text-teal-400">
                    Buscar Por
                </p>
                <div className="flex flex-wrap items-center justify-center gap-6 rounded-xl border border-white/5 bg-black/20 p-2.5">
                    <label className="flex cursor-pointer items-center gap-2 font-mono text-xs text-slate-300 transition-colors hover:text-white">
                        <input
                            type="radio"
                            name="buscador"
                            value="cfdi"
                            checked={tipoDeBuscador === TipoDeBuscador.FechaFactura}
                            onChange={() => handleSelector(TipoDeBuscador.FechaFactura)}
                            className="accent-teal-400 cursor-pointer"
                        />
                        <span>Fecha CFDI</span>
                    </label>

                    <label className="flex cursor-pointer items-center gap-2 font-mono text-xs text-slate-300 transition-colors hover:text-white">
                        <input
                            type="radio"
                            name="buscador"
                            value="sistema"
                            checked={tipoDeBuscador === TipoDeBuscador.FechaSistema}
                            onChange={() => handleSelector(TipoDeBuscador.FechaSistema)}
                            className="accent-teal-400 cursor-pointer"
                        />
                        <span>Fecha Sistema</span>
                    </label>

                    <label className="flex cursor-pointer items-center gap-2 font-mono text-xs text-slate-300 transition-colors hover:text-white">
                        <input
                            type="radio"
                            name="buscador"
                            value="proveedor"
                            checked={tipoDeBuscador === TipoDeBuscador.Proveedor}
                            onChange={() => handleSelector(TipoDeBuscador.Proveedor)}
                            className="accent-teal-400 cursor-pointer"
                        />
                        <span>Proveedor</span>
                    </label>
                </div>
            </section>

            {tipoDeBuscador === TipoDeBuscador.Proveedor ? (
                <BuscadorPorProveedor
                    proveedores={proveedores || []}
                    onResults={setResultados}
                />
            ) : (
                <BuscadorPorFecha
                    tipoDeBuscador={tipoDeBuscador}
                    onResults={setResultados}
                />
            )}

            <div className="mt-6">
                {!isSearchDone && (
                    <div className="rounded-xl border border-dashed border-white/15 py-8 text-center font-mono text-sm text-slate-400">
                        Realiza una búsqueda para ver resultados aquí.
                    </div>
                )}

                {isEmpty && (
                    <div className="rounded-xl border border-dashed border-white/15 py-8 text-center font-mono text-sm text-slate-400">
                        No se encontraron documentos con esos criterios.
                    </div>
                )}

                {hasComprobantes && (
                    <TblResults
                        comprobantes={resultados.comprobantes!}
                        handleClick={handleRowClick}
                        onPageChange={resultados.onPageChangeComprobantes}
                    />
                )}

                {hasPagos && (
                    <TblResults
                        comprobantes={resultados.pagos!}
                        handleClick={handleRowClick}
                        onPageChange={resultados.onPageChangePagos}
                    />
                )}

                {showPopup && selectedComprobante && (
                    <FilesPopup comprobanteVO={selectedComprobante} onClose={handleClosePopup} />
                )}
            </div>
        </div>
    );
};

export default PanelBuscar;