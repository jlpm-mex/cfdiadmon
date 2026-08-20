import { FileCode, FileText } from "lucide-react";
import { TipoDeDocumento, type ComprobanteVO } from "../services/api/documentService";

  const fileNameGen = (comprobanteVO : ComprobanteVO): {fileName: string, filePath: string}  => {
      const documentDate = new Date(comprobanteVO.fdFechaComprobante)
      const documentYear = documentDate.getFullYear();
      const formattedDate = new Intl.DateTimeFormat('en-US', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'        
      });

      const filePath = documentYear > 2022 ? 
        `${comprobanteVO.proveedorVO.fcRfc}/${documentYear}` :
            `${formattedDate.format(documentDate)}`;
      
    const fileName = `${comprobanteVO.tipoDeComprobante == TipoDeDocumento.Pago ? 'CDP' : 
          comprobanteVO.fcUsoCFDI}_${comprobanteVO.proveedorVO.fcRfc}_${(comprobanteVO.fcFolio != null && comprobanteVO.fcFolio.length != 0) ? comprobanteVO.fcFolio :
               comprobanteVO.fcFoliofiscal}`
  
      return {fileName, filePath}
  }

    const FilesPopup = ({ comprobanteVO, onClose }: { comprobanteVO: ComprobanteVO, onClose : () => void}) => {
        const { fileName, filePath } = fileNameGen(comprobanteVO);
        const formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 2
        });

        const handleClosePopup = () => {
            onClose();
        };
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

export default FilesPopup