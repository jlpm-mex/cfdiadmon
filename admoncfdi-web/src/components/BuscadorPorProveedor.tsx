import { useState } from 'react'
import Dropdown from './Dropdown';
import { useGetDocsBySupplier } from '../services/hooks/useDocuments';
import type { ProveedorVO } from '../services/api/SupplierService';
import type { CfdiRecibidoVO, ComprobanteVO } from '../services/api/documentService';
import { toast } from 'sonner';

const generarAniosDisponibles = (startingYear = 2019): number[] => {
    const currentYear = new Date().getFullYear();
    const years: number[] = [];
    for (let i = startingYear; i <= currentYear; i++) {
        years.push(i);
    }
    return years;
};

const BuscadorPorProveedor = ({ proveedores, onResults }: { proveedores: ProveedorVO[], onResults: ({ comprobantes, pagos }: { comprobantes: CfdiRecibidoVO[] | null, pagos: ComprobanteVO[] | null }) => void }) => {
    const [proveedorSeleccionado, setProveedorSeleccionado] = useState<string>("");
    const [anioSeleccionado, setAnioSeleccionado] = useState<number>(0);

    const years = generarAniosDisponibles();
    const opcionesProveedores = proveedores?.map(p => ({
        label: p.fcnombre,
        value: p.fcRfc
    }));
    const { data, isLoading, refetch } = useGetDocsBySupplier(proveedorSeleccionado, anioSeleccionado, { enabled: false });


    const handleBuscar = () => {
        if (!proveedorSeleccionado) {
            toast.warning("Para realizar una busqueda selecciona un proveedor.");
            return false;
        }

        if (anioSeleccionado == 0) {
            toast.warning("Para realizar una b;usqueda selecciona un año.");
            return false;
        }


        refetch();
        const comprobantes = data?.data.comprobantes || null;
        const pagos = data?.data.pagos || null;
        onResults({ comprobantes, pagos });
    }

    return (
        <section>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_1.9fr_1fr_auto]">
                <div className="flex flex-col gap-1.5">
                    <Dropdown label="año" data={years} value={anioSeleccionado} onSelect={(val) => setAnioSeleccionado(+val)} />
                </div>

                <div className="flex flex-col gap-1.5">
                    <Dropdown label="Proveedor" data={opcionesProveedores} value={proveedorSeleccionado} onSelect={(val) => setProveedorSeleccionado(val.toString())} />
                </div>

                <button
                    type="button"
                    onClick={handleBuscar}
                    className="self-end 
                    rounded-lg 
                    border-2 
                    border-teal-400 
                    bg-teal-400 
                    px-5 
                    py-2.5 
                    text-sm 
                    font-bold 
                    uppercase 
                    tracking-wide 
                    text-[#0F1B2D] 
                    transition-transform 
                    hover:brightness-110 
                    active:scale-95"
                >
                    Buscar
                </button>
            </div>
        </section>
    )
}

export default BuscadorPorProveedor