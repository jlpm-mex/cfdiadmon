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

export const UsoCfdi = {
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

export const TipoDeDocumento = {
    Pago: "Pago",
    Cfdi: "Cfdi"
} as const;

type TipoDeDocumento = typeof TipoDeDocumento[keyof typeof TipoDeDocumento];