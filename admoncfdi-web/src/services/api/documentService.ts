import axios from "axios";
import type { 
    CfdiRecibidoVO, 
    ComprobanteVO, 
    FindCfdiProvResponseTO, 
    PendingCfdi, 
    ProcesarCfdiResponseTO, 
    TipoDeBuscador, 
    UsoCfdiKey 
} from "../../types";
import { TipoDeDocumento, UsoCfdi } from "../../types";
import { SearchStrategyFactory } from "./documentSearchStrategy";

export type { 
    CfdiRecibidoVO, 
    ComprobanteVO, 
    FindCfdiProvResponseTO, 
    PendingCfdi, 
    ProcesarCfdiResponseTO, 
    UsoCfdiKey 
};

export { TipoDeDocumento, UsoCfdi };

export const uploadDocs = async (
    formData: FormData,
    onProgreso?: (porcentaje: number) => void
): Promise<{ data: string[]; status: number }> => {
    const { data, status } = await axios.post<string[]>(
        `${import.meta.env.VITE_API_BASE_URL}/api/cfdis/files`,
        formData,
        {
            onUploadProgress: (e) => {
                const porcentaje = Math.round((e.loaded * 100) / (e.total ?? 1));
                onProgreso?.(porcentaje);
            },
        }
    );

    return { data, status };
};

export const getPendingDocs = async (): Promise<{ data: any; status: number }> => {
    const { data, status } = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/unprocessed-docs`);
    return { data, status };
};

export const processDocs = async (): Promise<{ data: ProcesarCfdiResponseTO; status: number }> => {
    const { data, status } = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/start-process`);
    return { data, status };
};

export const findDocumentsBySupplier = async (
    rfc: string,
    year: number,
    page: number = 0,
    size: number = 10
): Promise<{ data: FindCfdiProvResponseTO; status: number }> => {
    const { data, status } = await axios.get<FindCfdiProvResponseTO>(
        `${import.meta.env.VITE_API_BASE_URL}/api/cfdis/by-rfc`,
        {
            params: {
                rfc,
                year,
                page,
                size,
            },
        }
    );
    return { data, status };
};

const dateToString = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};

export const findDocumentsByDate = async (
    startDate: Date,
    endDate: Date,
    tipoDeBuscador: TipoDeBuscador,
    page: number = 0,
    size: number = 10
): Promise<{ data: FindCfdiProvResponseTO; status: number }> => {
    const strategy = SearchStrategyFactory.create(tipoDeBuscador);
    console.log(strategy.getEndpoint);
    const { data, status } = await axios.get<FindCfdiProvResponseTO>(strategy.getEndpoint(), {
        params: {
            startDate: dateToString(startDate),
            endDate: dateToString(endDate),
            page,
            size,
        },
    });
    console.log('data'+data);
    return { data, status };
};