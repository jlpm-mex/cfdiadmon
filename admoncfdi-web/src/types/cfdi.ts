import type { EmisorVO } from './supplier';

export interface PendingCfdi {
    xml: {
        path: string;
    };
    xmlName: string;
    pdf: {
        path: string;
    };
    pdfName: string;
    isProcessed: boolean;
    message: string;
    comprobanteVO: any | null;
}

export interface ProcesarCfdiResponseTO {
    totalFiles: number;
    totalProcessedFiles: number;
    unprocessedListName: string[];
    processedList: PendingCfdi[];
    unprocessedList: PendingCfdi[];
}

export interface PageResult<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface ComprobanteVO {
    fiId: number;
    emisorVO: EmisorVO;
    fcRfcReceptor: string;
    fcFoliofiscal: string;
    fcSerie: string;
    fcFolio: string;
    fcUsoCFDI: string;
    fdFechaComprobante: Date;
    fdFecha: Date;
    fdTotal: number;
    tipoDeComprobante: string;
}

export interface CfdiRecibidoVO extends ComprobanteVO {
    fcFormaDePago: string;
    fcMetodoDePago: string;
}

export interface FindCfdiProvResponseTO {
    comprobantes: PageResult<CfdiRecibidoVO>;
    pagos: PageResult<ComprobanteVO>;
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

export type TipoDeDocumento = typeof TipoDeDocumento[keyof typeof TipoDeDocumento];
