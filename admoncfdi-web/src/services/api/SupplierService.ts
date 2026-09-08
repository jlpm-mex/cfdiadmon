import axios from "axios";
import type { ProveedorVO } from "../../types";

export type { ProveedorVO };

export const getSuppliers = async (): Promise<ProveedorVO[]> => {
    const { data } = await axios.get<ProveedorVO[]>(`${import.meta.env.VITE_API_BASE_URL}/api/cfdis/proveedores`);
    return data;
};