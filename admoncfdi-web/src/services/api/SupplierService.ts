import axios from "axios";

export interface ProveedorVO {
    fcRfc: string,
    fcnombre: string,
    fiId: number
}

export const getSuppliers = async ():Promise<ProveedorVO[]> => {
    const { data } = await axios.get<ProveedorVO[]>(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/proveedores`);
    return data;
};