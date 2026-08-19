import axios from "axios";

export const uploadDocs = async (formData: FormData,
     onProgreso?:(porcentaje: number) => void ):Promise<{data :string[], status: number}> => {
    const { data, status } = await axios.post<string[]>(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/files`, formData, {
        onUploadProgress: (e) => {
            const porcentaje = Math.round((e.loaded * 100) / (e.total ?? 1));
            onProgreso?.(porcentaje);
        },
    });

    return {data, status};
}

export const getPendingDocs = async() :Promise<{data: any, status: number}> => {
    const {data, status} = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/unprocessed-docs`);
    return {data, status};
}

interface ProcesarCfdiResponseTO {
	totalFiles: number
	totalProcessedFiles: number;
	unprocessedListName: string[];
	processedList: PendingCfdi[];
	unprocessedList: PendingCfdi[];
}

interface PendingCfdi {
    xml:{
        path: string
    },
    xmlName: string,
    pdf:{
        path: string
    },
    pdfName: String,
    isProcessed: boolean,
    message: string,
    comprobanteVO: any | null
}

export const processDocs = async () : Promise<{data: ProcesarCfdiResponseTO, status:number}> => {
    const {data, status} = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/start-process`);
    return {data, status}
}

interface ProveedorVO {
	fiId: number,
	fcnombre: string,
	fcRfc: string
}

export interface ComprobanteVO {
	fiId: number,
	proveedorVO: ProveedorVO,
	fcRfcReceptor:string, 
	fcFoliofiscal:string, 
	fcSerie:string, 
	fcFolio:string, 
	fcUsoCFDI:string,
	fdFechaComprobante: Date,
	fdFecha: Date,
	fdTotal: number,
	tipoDeComprobante: string
}

interface FindCfdiProvResponseTO{
	comprobantes: CfdiRecibidoVO[], 
	pagos: ComprobanteVO[],
}

export interface CfdiRecibidoVO extends ComprobanteVO {
	fcFormaDePago: string,
	fcMetodoDePago: string
}

export const findDocumentsBySupplier = async(rfc:string, year: number):Promise<{data: FindCfdiProvResponseTO, status: number}> => {
    const {data, status} = await axios.get<FindCfdiProvResponseTO>(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/by-rfc`,
        {params:{
            rfc,
            year
        }}
     );
    return {data, status}
} 