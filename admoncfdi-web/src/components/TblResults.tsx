import { useState } from 'react'
import { TipoDeDocumento, UsoCfdi, type CfdiRecibidoVO, type ComprobanteVO, type UsoCfdiKey } from '../services/api/documentService';

const folioFiscalTrimmer = (folioFiscal: string): string => {
    const folioFiscalTokens = (folioFiscal).split("-");
    return folioFiscalTokens[folioFiscalTokens.length - 1];
}

const TblResults = ({ comprobantes, handleClick }: { comprobantes: ComprobanteVO[] | CfdiRecibidoVO[], handleClick: (comprobate: ComprobanteVO) => void }) => {
    const [isOpen, setIsOpen] = useState<boolean>(true);
    const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2
    });

    return (
        <div className="mx-auto mt-10 border border-gray-400 rounded-lg overflow-hidden">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex justify-between p-4 bg-gray-400 font-medium"
            >
                <span>{comprobantes[0].tipoDeComprobante == TipoDeDocumento.Pago.substring(0, 1) ?
                    `${TipoDeDocumento.Pago.toUpperCase()} ` :
                    `${TipoDeDocumento.Cfdi.toUpperCase()} `}
                    {` --> ${comprobantes.length}`}</span>
                <span className={`transform transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}>▼</span>
            </button>

            <div className={`grid transition-[grid-template-rows] duration-300 ease-in-out ${isOpen ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'}`}>
                <div className="overflow-hidden">
                    <table className="w-full border-collapse text-sm">
                        <thead>
                            <tr>
                                {["F.Docto", "Serie", "Folio", "Folio Fiscal", "Total", "Uso", "M de pago", "T de comp"].map((h) => (
                                    <th
                                        key={h}
                                        className="border-b 
                                        border-white/10 
                                        px-2.5 
                                        py-2 
                                        text-left 
                                        font-mono 
                                        text-[11px] 
                                        uppercase 
                                        tracking-widest 
                                        text-slate-400"
                                    >
                                        {h}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {comprobantes.map((d) => (
                                <tr key={d.fcFoliofiscal} className="hover:bg-white/3 cursor-pointer" onClick={() => { handleClick(d) }}>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100">{d.fdFechaComprobante.toString()}</td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100">{d.fcSerie}</td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100">{d.fcFolio}</td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100">
                                        <div className="relative group inline-block">
                                            <span className="  
                                            cursor-help">{folioFiscalTrimmer(d.fcFoliofiscal)}</span>
                                            <div className="absolute 
                                            bottom-full 
                                            left-1/2 
                                            z-20 
                                            mb-2 -translate-x-1/2 
                                            whitespace-nowrap 
                                            rounded 
                                            bg-black 
                                            px-2 
                                            py-1 
                                            text-xs 
                                            text-white 
                                            opacity-0 
                                            transition-opacity 

                                            group-hover:opacity-100 
                                            pointer-events-none">
                                                {d.fcFoliofiscal}
                                                <div className="absolute 
                                                top-full 
                                                left-1/2 
                                                h-2 
                                                w-2 
                                                -translate-x-1/2 
                                                -translate-y-1 
                                                bg-black 
                                                rotate-45"></div>
                                            </div>

                                        </div>
                                    </td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100
                                    text-right
                                    ">{formatter.format(d.fdTotal)}</td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100">
                                        <div className="relative group inline-block">
                                            <span className="
                                            cursor-help">{d.fcUsoCFDI}</span>
                                            <div className="absolute 
                                            bottom-full 
                                            left-1/2 
                                            z-20 
                                            mb-2 
                                            -translate-x-1/2 
                                            whitespace-nowrap 
                                            rounded 
                                            bg-black 
                                            px-2 
                                            py-1 
                                            text-xs 
                                            text-white 
                                            opacity-0 
                                            transition-opacity 
                                            group-hover:opacity-100 
                                            pointer-events-none">
                                                {UsoCfdi[d.fcUsoCFDI as UsoCfdiKey]}
                                                <div className="absolute 
                                                top-full 
                                                left-1/2 
                                                h-2 
                                                w-2 
                                                -translate-x-1/2 
                                                -translate-y-1 
                                                bg-black 
                                                rotate-45">
                                                </div>
                                            </div>

                                        </div>
                                    </td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100
                                    text-center">{"fcFormaDePago" in d ? d.fcFormaDePago : "-"}</td>
                                    <td className="border-b 
                                    border-white/10 
                                    px-2.5 py-2.5 
                                    text-slate-100
                                    text-center">
                                        {d.tipoDeComprobante}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    )
}

export default TblResults