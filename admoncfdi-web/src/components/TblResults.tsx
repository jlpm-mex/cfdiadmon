import { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { TipoDeDocumento, UsoCfdi, type CfdiRecibidoVO, type ComprobanteVO, type UsoCfdiKey, type PageResult } from '../types';
import Paginador from './Paginador';

const folioFiscalTrimmer = (folioFiscal: string): string => {
    const folioFiscalTokens = (folioFiscal).split("-");
    return folioFiscalTokens[folioFiscalTokens.length - 1];
};

interface TblResultsProps {
    comprobantes: (ComprobanteVO | CfdiRecibidoVO)[] | PageResult<ComprobanteVO | CfdiRecibidoVO>;
    handleClick: (comprobante: ComprobanteVO) => void;
    onPageChange?: (newPage: number) => void;
    isFetching?: boolean;
}

const TblResults = ({ comprobantes, handleClick, onPageChange, isFetching }: TblResultsProps) => {
    const [isOpen, setIsOpen] = useState<boolean>(true);
    const formatter = new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
        minimumFractionDigits: 2
    });

    if (!comprobantes) return null;

    // Normalizamos los datos tanto si vienen como Array simple o como PageResult paginado
    const isPaginated = !Array.isArray(comprobantes) && 'content' in comprobantes;
    const items: (ComprobanteVO | CfdiRecibidoVO)[] = isPaginated ? comprobantes.content : comprobantes;
    const totalCount = isPaginated ? comprobantes.totalElements : comprobantes.length;

    if (items.length === 0 && totalCount === 0) return null;

    const primerItem = items[0];
    const esPago = primerItem ? (
        primerItem.tipoDeComprobante === TipoDeDocumento.Pago.substring(0, 1) || 
        primerItem.tipoDeComprobante === TipoDeDocumento.Pago
    ) : false;

    return (
        <div className="mt-5 overflow-hidden rounded-xl border border-white/10 bg-slate-900/40">
            <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                className="flex w-full cursor-pointer items-center justify-between border-b border-white/10 bg-white/[0.04] px-4 py-3 text-left transition-colors hover:bg-white/[0.07]"
            >
                <div className="flex items-center gap-3">
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-mono font-semibold tracking-wider ${
                        esPago 
                            ? 'bg-[#C4423E]/20 text-[#E06A66] border border-[#C4423E]/30' 
                            : 'bg-teal-500/20 text-teal-300 border border-teal-500/30'
                    }`}>
                        {esPago ? TipoDeDocumento.Pago.toUpperCase() : TipoDeDocumento.Cfdi.toUpperCase()}
                    </span>
                    <span className="font-mono text-xs uppercase tracking-wider text-slate-400">
                        {totalCount} {totalCount === 1 ? 'documento' : 'documentos'}
                    </span>
                </div>
                <ChevronDown
                    className={`h-4 w-4 text-slate-400 transition-transform duration-200 ${
                        isOpen ? 'rotate-180 text-teal-400' : ''
                    }`}
                />
            </button>

            <div className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${isOpen ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'}`}>
                <div className="overflow-hidden">
                    <div className="max-h-[300px] overflow-y-auto overflow-x-auto scrollbar-thin">
                        <table className="w-full border-collapse text-left text-sm text-slate-300">
                            <thead className="sticky top-0 z-10 bg-[#132237] shadow-xs">
                                <tr className="border-b border-white/10 font-mono text-[11px] uppercase tracking-widest text-slate-400">
                                    {["F. Docto", "Serie", "Folio", "Folio Fiscal", "Total", "Uso", "F. Pago", "Tipo"].map((h, i) => (
                                        <th
                                            key={h}
                                            className={`px-3.5 py-2.5 ${i === 4 ? 'text-right' : (i >= 6 ? 'text-center' : 'text-left')}`}
                                        >
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {items.map((d) => (
                                    <tr
                                        key={d.fcFoliofiscal}
                                        className="cursor-pointer border-b border-white/5 transition-colors hover:bg-white/[0.03]"
                                        onClick={() => handleClick(d)}
                                    >
                                        <td className="px-3.5 py-2.5 font-mono text-xs text-slate-200 text-nowrap">
                                            {d.fdFechaComprobante?.toString()}
                                        </td>
                                        <td className="px-3.5 py-2.5 text-slate-300">
                                            {d.fcSerie || "-"}
                                        </td>
                                        <td className="px-3.5 py-2.5 font-mono text-xs text-slate-300">
                                            {d.fcFolio || "-"}
                                        </td>
                                        <td className="px-3.5 py-2.5 text-slate-200">
                                            <div className="group relative inline-block">
                                                <span className="font-mono text-xs text-teal-400 underline decoration-teal-400/30 underline-offset-2 hover:decoration-teal-400">
                                                    ..{folioFiscalTrimmer(d.fcFoliofiscal)}
                                                </span>
                                                <div className="pointer-events-none absolute bottom-full left-1/2 z-30 mb-2 -translate-x-1/2 whitespace-nowrap rounded-md border border-white/10 bg-[#0F1B2D] px-2.5 py-1 text-xs font-mono text-slate-200 opacity-0 shadow-xl transition-opacity group-hover:opacity-100">
                                                    {d.fcFoliofiscal}
                                                    <div className="absolute top-full left-1/2 -mt-1 h-2 w-2 -translate-x-1/2 rotate-45 border-r border-b border-white/10 bg-[#0F1B2D]"></div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-3.5 py-2.5 text-right font-mono text-xs font-semibold text-slate-100 text-nowrap">
                                            {formatter.format(d.fdTotal)}
                                        </td>
                                        <td className="px-3.5 py-2.5 text-slate-300">
                                            <div className="group relative inline-block">
                                                <span className="font-mono text-xs text-slate-300 cursor-help">
                                                    {d.fcUsoCFDI}
                                                </span>
                                                <div className="pointer-events-none absolute bottom-full left-1/2 z-30 mb-2 -translate-x-1/2 whitespace-nowrap rounded-md border border-white/10 bg-[#0F1B2D] px-2.5 py-1 text-xs text-slate-200 opacity-0 shadow-xl transition-opacity group-hover:opacity-100">
                                                    {UsoCfdi[d.fcUsoCFDI as UsoCfdiKey] || d.fcUsoCFDI}
                                                    <div className="absolute top-full left-1/2 -mt-1 h-2 w-2 -translate-x-1/2 rotate-45 border-r border-b border-white/10 bg-[#0F1B2D]"></div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-3.5 py-2.5 text-center font-mono text-xs text-slate-400">
                                            {"fcFormaDePago" in d && d.fcFormaDePago ? d.fcFormaDePago : "-"}
                                        </td>
                                        <td className="px-3.5 py-2.5 text-center font-mono text-xs text-slate-300">
                                            {d.tipoDeComprobante}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Componente Paginador si los datos son paginados */}
                    {isPaginated && (
                        <Paginador
                            currentPage={comprobantes.page}
                            totalPages={comprobantes.totalPages}
                            totalElements={comprobantes.totalElements}
                            pageSize={comprobantes.size}
                            onPageChange={onPageChange || (() => {})}
                            isFetching={isFetching}
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default TblResults;