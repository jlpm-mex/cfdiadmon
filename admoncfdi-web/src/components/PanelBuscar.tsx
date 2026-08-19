import { useState } from 'react'
import { useLoadSuppliers } from '../services/hooks/useSuppliers';
import type {CfdiRecibidoVO, ComprobanteVO } from '../services/api/documentService';
import { FileCode, FileText  } from 'lucide-react';   
import BuscadorPorFecha from './BuscadorPorFecha';
import BuscadorPorProveedor from './BuscadorPorProveedor';


const TipoDeBuscador = {
    FechaFactura: "FechaFactura",
    FechaSistema: "FechaSistema",
    Proveedor: "Proveedor",
} as const;

type TipoDeBuscador = typeof TipoDeBuscador[keyof typeof TipoDeBuscador];

const TipoDeDocumento = {
    Pago: "Pago",
    Cfdi: "Cfdi"
} as const;

type TipoDeDocumento = typeof TipoDeDocumento[keyof typeof TipoDeDocumento];

const fileNameGen = (comprobanteVO : ComprobanteVO): {fileName: string, filePath: string}  => {
    const documentDate = new Date(comprobanteVO.fdFechaComprobante)
    const documentYear = documentDate.getFullYear();
    const formattedDate = new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'        
    })
    const filePath = documentYear > 2022 ? 
	    `${comprobanteVO.proveedorVO.fcRfc}/${documentYear}` :
		 	`${formattedDate.format(documentDate)}`;
    
	const fileName = `${comprobanteVO.tipoDeComprobante == TipoDeDocumento.Pago ? 'CDP' : 
        comprobanteVO.fcUsoCFDI}_${comprobanteVO.proveedorVO.fcRfc}_${(comprobanteVO.fcFolio != null && comprobanteVO.fcFolio.length != 0) ? comprobanteVO.fcFolio :
             comprobanteVO.fcFoliofiscal}`

    return {fileName, filePath}
}

const folioFiscalTrimmer = (folioFiscal : string) : string => {
    const folioFiscalTokens    = (folioFiscal).split("-");
	return folioFiscalTokens[folioFiscalTokens.length - 1];
}

const UsoCfdi = {
		G01: "Adquisición de mercancias",
		G02: "Devoluciones, descuentos o bonificaciones",
		G03: "Gastos en general",
		I01: "Construcciones",
		I02: "Mobilario y equipo de oficina por inversiones",
		I03: "Equipo de transporte",
		I04: "Equipo de computo y accesorios",
		I05: "Dados, troqueles, moldes, matrices y herramental",
		I06: "Comunicaciones telefónicas",
		I07: "Comunicaciones satelitales",
		I08: "Otra maquinaria y equipo",
		D01: "Honorarios médicos, dentales y gastos hospitalarios.",
		D02: "Gastos médicos por incapacidad o discapacidad",
		D03: "Gastos funerales.",
		D04: "Donativos.",
		D05: "Intereses reales efectivamente pagados por créditos hipotecarios (casa habitación).",
		D06: "Aportaciones voluntarias al SAR.",
		D07: "Primas por seguros de gastos médicos.",
		D08: "Gastos de transportación escolar obligatoria.",
		D09: "Depósitos en cuentas para el ahorro, primas que tengan como base planes de pensiones.",
		D10: "Pagos por servicios educativos (colegiaturas)",
		S01: "Sin efectos fiscales",
		CP01: "Pagos",
		CN01: "Nómina"
	} as const;

export type UsoCfdiKey = keyof typeof UsoCfdi; 

const PanelBuscar = () => {
    const [resultados, setResultados] = useState<{comprobantes: CfdiRecibidoVO[] | null, pagos: ComprobanteVO[] | null}>({comprobantes: null, pagos: null});
    const [tipoDeBuscador, setTipoDeBuscador] = useState<TipoDeBuscador>(TipoDeBuscador.FechaFactura);
    const { data: proveedores } = useLoadSuppliers();
    const [selectedComprobante, setSelectedComprobante] = useState<ComprobanteVO | null>(null);
    const [showPopup, setShowPopup] = useState<boolean>(false);
    const formatter = new Intl.NumberFormat('en-US', {
		style: 'currency',
		currency: 'USD',
		minimumFractionDigits: 2
	});


    const handleSelector = (tipo: TipoDeBuscador) => {
        setTipoDeBuscador(tipo);
    };


    const DisplayPopup = ({ comprobanteVO }: { comprobanteVO: ComprobanteVO}) => {
        const { fileName, filePath } = fileNameGen(comprobanteVO);
        return (
            <div
                className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
                onClick={handleClosePopup} // Cerrar al hacer click fuera
            >
                <div
                    className="relative bg-slate-800 rounded-lg p-3 w-[500px]"
                    onClick={(e) => e.stopPropagation()} // Evitar cerrar al hacer click dentro
                >
                    <button
                        onClick={handleClosePopup}
                        className="absolute top-2 right-2 text-slate-400 hover:text-white"
                    >
                        ✕
                    </button>
                    <div className='flex-col p-3'>
                        <h3 className="text-center 
                        relative 
                        font-semibold 
                        text-[18.5px] 
                        transition-colors
                        text-teal-500">Documentos:</h3>
                        <div className="border border-teal-500 w-[90%] m-4"></div>
                        <section className="m-1 grid grid-cols-4">
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100">{`Documento:`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100
                            col-span-3">{`${fileName}`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100">{`Emisor:`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100
                            col-span-3">{`${comprobanteVO.proveedorVO.fcnombre}`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100">{`F Fiscal:`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100
                            col-span-3">{`${comprobanteVO.fcFoliofiscal}`}</div>    
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100">{`Total:`}</div>
                            <div className="text-left 
                            text-[14px] 
                            transition-colors
                            text-slate-100
                            col-span-3">{`${formatter.format(comprobanteVO.fdTotal)}`}</div>
                        </section>
                        <div className="border border-teal-500 w-[90%] m-4"></div>
                        <h4 className="text-center  
                        font-semibold 
                        text-[14.5px] 
                        transition-colors
                        text-slate-100
                        m-4">Seleccione PDF o XML para abrir</h4>
                        <div className="flex justify-around">
                            <div className="flex-col 
                            text-center 
                            py-3
                            px-5 
                            cursor-pointer
                            rounded-2xl
                            border
                            border-teal-400
                            mx-2
                            hover:border-teal-700">
                                <a href={`/myfiles/${filePath}/${fileName}.xml`} target="blank">
                                    <FileText className="size-10 text-red-500" strokeWidth={1}/>
                                </a>
                                <span className="m-2 text-teal-500 text-sm">pdf</span>
                            </div>
                            <div className="flex-col 
                            text-center 
                            py-3 
                            px-5
                            cursor-pointer
                            rounded-2xl
                            border
                            border-teal-400
                            mx-2
                            hover:border-teal-700">
                                <a href={`/myfiles/${filePath}/${fileName}.xml`} target="blank">
                                    <FileCode className="size-10 text-white" strokeWidth={1}/>
                                    
                                </a>
                                <span className="m-2 text-teal-500 text-sm">xml</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

  const handleRowClick = (comprobante: ComprobanteVO) => {
    setSelectedComprobante(comprobante);
    setShowPopup(true);
  };

  const handleClosePopup = () => {
    setShowPopup(false);
    setSelectedComprobante(null);
  };

  const TblResults = ({comprobantes}:{comprobantes : ComprobanteVO[] | CfdiRecibidoVO[]}) => {
    const [isOpen, setIsOpen] = useState<boolean>(true);
    return(
        <div className="mx-auto mt-10 border border-gray-400 rounded-lg overflow-hidden">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-full flex justify-between p-4 bg-gray-400 font-medium"
            >
                <span>{comprobantes[0].tipoDeComprobante == TipoDeDocumento.Pago.substring(0,1) ? 
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
                                <tr key={d.fcFoliofiscal} className="hover:bg-white/[0.03] cursor-pointer" onClick={() => { handleRowClick(d) }}>
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

    return (
        <div className="rounded-[18px] border border-white/10 bg-gradient-to-b from-[#16263D] to-[#16263D]/60 p-7">
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
            
            { tipoDeBuscador == TipoDeBuscador.Proveedor ? 
                <BuscadorPorProveedor proveedores={proveedores || []} onResults = { ({comprobantes,pagos}) => setResultados({comprobantes, pagos})}/> :
                <BuscadorPorFecha/>
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
                    <TblResults comprobantes = {resultados.comprobantes} />
                )}
                {resultados.pagos !== null && resultados.pagos.length > 0 && (
                    <TblResults comprobantes = {resultados.pagos} />
                )}

                { showPopup && selectedComprobante && (
                    <DisplayPopup comprobanteVO={selectedComprobante}/>
                )}
            </div>
        </div>
    );

}

export default PanelBuscar