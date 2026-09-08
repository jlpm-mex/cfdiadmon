import { useState } from 'react';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';
import Dropdown from './Dropdown';
import { useGetDocsBySupplier } from '../services/hooks/useDocuments';
import type { EmisorVO, CfdiRecibidoVO, ComprobanteVO, PageResult } from '../types';

const generarAniosDisponibles = (startingYear = 2019): number[] => {
    const currentYear = new Date().getFullYear();
    const years: number[] = [];
    for (let i = startingYear; i <= currentYear; i++) {
        years.push(i);
    }
    return years;
};

interface BuscadorPorProveedorProps {
    proveedores: EmisorVO[];
    onResults: (results: {
        comprobantes: PageResult<CfdiRecibidoVO> | CfdiRecibidoVO[] | null;
        pagos: PageResult<ComprobanteVO> | ComprobanteVO[] | null;
        onPageChangeComprobantes?: (page: number) => void;
        onPageChangePagos?: (page: number) => void;
    }) => void;
}

const BuscadorPorProveedor = ({ proveedores, onResults }: BuscadorPorProveedorProps) => {
    const [proveedorSeleccionado, setProveedorSeleccionado] = useState<string>("");
    const [anioSeleccionado, setAnioSeleccionado] = useState<number>(0);
    const [page, setPage] = useState<number>(0);

    const years = generarAniosDisponibles();
    const opcionesProveedores = proveedores?.map(p => ({
        label: p.fcnombre,
        value: p.fcRfc
    })) || [];

    const { isFetching, refetch } = useGetDocsBySupplier(
        proveedorSeleccionado,
        anioSeleccionado,
        page,
        10,
        { enabled: false }
    );

    const ejecutarBusqueda = async (pagina: number = 0) => {
        setPage(pagina);
        try {
            const { data: response } = await refetch();
            const comprobantes = response?.data?.comprobantes || { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
            const pagos = response?.data?.pagos || { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

            onResults({
                comprobantes,
                pagos,
                onPageChangeComprobantes: (newPage: number) => ejecutarBusqueda(newPage),
                onPageChangePagos: (newPage: number) => ejecutarBusqueda(newPage),
            });
        } catch (error) {
            console.error("Error al buscar comprobantes:", error);
            toast.error("Ocurrió un error al consultar los documentos del proveedor.");
        }
    };

    const handleBuscar = () => {
        if (!proveedorSeleccionado) {
            toast.warning("Para realizar una búsqueda selecciona un proveedor.");
            return;
        }

        if (anioSeleccionado === 0) {
            toast.warning("Para realizar una búsqueda selecciona un año.");
            return;
        }

        ejecutarBusqueda(0);
    };

    return (
        <section>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_1.9fr_1fr_auto]">
                <div className="flex flex-col gap-1.5">
                    <Dropdown
                        label="Año"
                        data={years}
                        value={anioSeleccionado}
                        onSelect={(val) => setAnioSeleccionado(+val)}
                    />
                </div>

                <div className="flex flex-col gap-1.5">
                    <Dropdown
                        label="Proveedor"
                        data={opcionesProveedores}
                        value={proveedorSeleccionado}
                        onSelect={(val) => setProveedorSeleccionado(val.toString())}
                    />
                </div>

                <button
                    type="button"
                    disabled={isFetching}
                    onClick={handleBuscar}
                    className={`self-end flex items-center justify-center gap-2 rounded-lg border-2 border-teal-400 bg-teal-400 px-5 py-2.5 text-sm font-bold uppercase tracking-wide text-[#0F1B2D] transition-all hover:brightness-110 active:scale-95 ${
                        isFetching ? "cursor-wait opacity-70" : "cursor-pointer"
                    }`}
                >
                    {isFetching && <Loader2 className="h-4 w-4 animate-spin" />}
                    {isFetching ? "Buscando..." : "Buscar"}
                </button>
            </div>
        </section>
    );
};

export default BuscadorPorProveedor;