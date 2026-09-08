import { FileCode, FileText, X } from "lucide-react";
import { TipoDeDocumento, type ComprobanteVO } from "../types";

const fileNameGen = (comprobanteVO: ComprobanteVO): { fileName: string; filePath: string } => {
    const documentDate = new Date(comprobanteVO.fdFechaComprobante);
    const documentYear = documentDate.getFullYear();
    const formattedDate = new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });

    const filePath = documentYear > 2022
        ? `${comprobanteVO.emisorVO.fcRfc}/${documentYear}`
        : `${formattedDate.format(documentDate)}`;

    const fileName = `${comprobanteVO.tipoDeComprobante === TipoDeDocumento.Pago ? 'CDP' : comprobanteVO.fcUsoCFDI}_${comprobanteVO.emisorVO.fcRfc}_${(comprobanteVO.fcFolio != null && comprobanteVO.fcFolio.length !== 0) ? comprobanteVO.fcFolio : comprobanteVO.fcFoliofiscal}`;

    return { fileName, filePath };
};

interface FilesPopupProps {
    comprobanteVO: ComprobanteVO;
    onClose: () => void;
}

const FilesPopup = ({ comprobanteVO, onClose }: FilesPopupProps) => {
    const { fileName, filePath } = fileNameGen(comprobanteVO);
    const formatter = new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
        minimumFractionDigits: 2
    });

    return (
        <div
            role="dialog"
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-xs"
            onClick={onClose}
        >
            <div
                className="relative w-full max-w-lg rounded-2xl border border-white/10 bg-linear-to-b from-[#16263D] to-[#121F33] p-6 shadow-2xl text-slate-100"
                onClick={(e) => e.stopPropagation()}
            >
                {/* Botón cerrar */}
                <button
                    type="button"
                    onClick={onClose}
                    aria-label="Cerrar modal"
                    className="absolute top-4 right-4 rounded-lg p-1 text-slate-400 transition-colors hover:bg-white/10 hover:text-white cursor-pointer"
                >
                    <X className="h-5 w-5" />
                </button>

                {/* Encabezado */}
                <div className="mb-4">
                    <div className="flex items-center gap-2 font-mono text-xs uppercase tracking-widest text-teal-400">
                        <span className="inline-block h-px w-3 bg-teal-400" />
                        Detalle del Comprobante
                    </div>
                    <h3 className="mt-1 text-lg font-bold text-slate-100">
                        Archivos disponibles
                    </h3>
                </div>

                {/* Detalle del documento */}
                <div className="space-y-2.5 rounded-xl border border-white/10 bg-white/[0.03] p-4 text-xs">
                    <div className="grid grid-cols-[90px_1fr] gap-2">
                        <span className="font-mono uppercase tracking-wider text-slate-400">Documento:</span>
                        <span className="font-mono text-slate-200 break-all">{fileName}</span>
                    </div>
                    <div className="grid grid-cols-[90px_1fr] gap-2">
                        <span className="font-mono uppercase tracking-wider text-slate-400">Emisor:</span>
                        <span className="text-slate-200">{comprobanteVO.emisorVO.fcnombre}</span>
                    </div>
                    <div className="grid grid-cols-[90px_1fr] gap-2">
                        <span className="font-mono uppercase tracking-wider text-slate-400">F. Fiscal:</span>
                        <span className="font-mono text-teal-400 break-all">{comprobanteVO.fcFoliofiscal}</span>
                    </div>
                    <div className="grid grid-cols-[90px_1fr] gap-2">
                        <span className="font-mono uppercase tracking-wider text-slate-400">Total:</span>
                        <span className="font-mono font-semibold text-slate-100">{formatter.format(comprobanteVO.fdTotal)}</span>
                    </div>
                </div>

                {/* Selector de archivos PDF / XML */}
                <div className="mt-5 text-center">
                    <p className="mb-3 font-mono text-xs uppercase tracking-wider text-slate-400">
                        Selecciona el archivo que deseas abrir
                    </p>
                    <div className="grid grid-cols-2 gap-4">
                        {/* Botón PDF */}
                        <a
                            href={`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/${comprobanteVO.fcFoliofiscal}/${comprobanteVO.tipoDeComprobante}/pdf`}
                            target="_blank"
                            rel="noreferrer noopener"
                            className="group flex flex-col items-center justify-center gap-2 rounded-xl border border-[#C4423E]/30 bg-[#C4423E]/5 p-4 transition-all hover:-translate-y-0.5 hover:border-[#C4423E] hover:bg-[#C4423E]/10"
                        >
                            <FileText className="h-9 w-9 text-[#C4423E] transition-transform group-hover:scale-110" strokeWidth={1.5} />
                            <div className="text-center">
                                <span className="font-mono text-xs font-bold uppercase tracking-wider text-[#E06A66]">
                                    Abrir PDF
                                </span>
                                <span className="block font-mono text-[10px] text-slate-400">.pdf</span>
                            </div>
                        </a>

                        {/* Botón XML */}
                        <a
                            href={`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/${comprobanteVO.fcFoliofiscal}/${comprobanteVO.tipoDeComprobante}/xml`}
                            target="_blank"
                            rel="noreferrer noopener"
                            className="group flex flex-col items-center justify-center gap-2 rounded-xl border border-[#2FA88A]/30 bg-[#2FA88A]/5 p-4 transition-all hover:-translate-y-0.5 hover:border-[#2FA88A] hover:bg-[#2FA88A]/10"
                        >
                            <FileCode className="h-9 w-9 text-[#2FA88A] transition-transform group-hover:scale-110" strokeWidth={1.5} />
                            <div className="text-center">
                                <span className="font-mono text-xs font-bold uppercase tracking-wider text-teal-400">
                                    Abrir XML
                                </span>
                                <span className="block font-mono text-[10px] text-slate-400">.xml</span>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FilesPopup;