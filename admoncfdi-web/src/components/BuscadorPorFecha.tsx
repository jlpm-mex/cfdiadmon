import { useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import type { CfdiRecibidoVO, ComprobanteVO, TipoDeBuscador, PageResult } from "../types";
import { findDocumentsByDate } from "../services/api/documentService";

interface BuscadorPorFechaProps {
    tipoDeBuscador: TipoDeBuscador;
    onResults: (results: {
        comprobantes: PageResult<CfdiRecibidoVO> | CfdiRecibidoVO[];
        pagos: PageResult<ComprobanteVO> | ComprobanteVO[];
        onPageChangeComprobantes?: (page: number) => void;
        onPageChangePagos?: (page: number) => void;
    }) => void;
}

const getTodayString = () => new Date().toISOString().split("T")[0];

const BuscadorPorFecha = ({ tipoDeBuscador, onResults }: BuscadorPorFechaProps) => {
    const [startDate, setStartDate] = useState<string>(getTodayString());
    const [endDate, setEndDate] = useState<string>(getTodayString());
    const [isSearching, setIsSearching] = useState<boolean>(false);

    const ejecutarBusqueda = async (pagina: number = 0) => {
        setIsSearching(true);
        try {
            const { data: response } = await findDocumentsByDate(
                new Date(`${startDate}T00:00:00`),
                new Date(`${endDate}T23:59:59`),
                tipoDeBuscador,
                pagina,
                10
            );
            const comprobantes = response?.comprobantes || { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
            const pagos = response?.pagos || { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

            onResults({
                comprobantes,
                pagos,
                onPageChangeComprobantes: (newPage: number) => ejecutarBusqueda(newPage),
                onPageChangePagos: (newPage: number) => ejecutarBusqueda(newPage),
            });
        } catch (error) {
            console.error("Error al buscar comprobantes:", error);
            toast.error("Ocurrió un error al consultar los documentos por fecha.");
        } finally {
            setIsSearching(false);
        }
    };

    const handleBuscar = () => {
        if (!startDate || !endDate) {
            toast.warning("Por favor selecciona ambas fechas.");
            return;
        }

        if (startDate > endDate) {
            toast.warning("La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        ejecutarBusqueda(0);
    };

    return (
        <section>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1.3fr_1.3fr_auto]">
                <div className="flex flex-col gap-1.5">
                    <label htmlFor="fecha-inicial" className="font-mono text-[11px] uppercase tracking-widest text-slate-400">
                        Fecha inicial
                    </label>
                    <input
                        id="fecha-inicial"
                        type="date"
                        value={startDate}
                        max={endDate || undefined}
                        onChange={(e) => setStartDate(e.target.value)}
                        className="rounded-lg border border-white/10 bg-white/4 px-3 py-2.5 text-sm text-slate-100 outline-none focus:border-teal-400 [&::-webkit-calendar-picker-indicator]:invert"
                    />
                </div>

                <div className="flex flex-col gap-1.5">
                    <label htmlFor="fecha-final" className="font-mono text-[11px] uppercase tracking-widest text-slate-400">
                        Fecha final
                    </label>
                    <input
                        id="fecha-final"
                        type="date"
                        value={endDate}
                        min={startDate || undefined}
                        onChange={(e) => setEndDate(e.target.value)}
                        className="rounded-lg border border-white/10 bg-white/4 px-3 py-2.5 text-sm text-slate-100 outline-none focus:border-teal-400 [&::-webkit-calendar-picker-indicator]:invert"
                    />
                </div>

                <button
                    type="button"
                    disabled={isSearching}
                    onClick={handleBuscar}
                    className={`self-end flex items-center justify-center gap-2 rounded-lg border-2 border-teal-400 bg-teal-400 px-5 py-2.5 text-sm font-bold uppercase tracking-wide text-[#0F1B2D] transition-all hover:brightness-110 active:scale-95 ${
                        isSearching ? "cursor-wait opacity-70" : "cursor-pointer"
                    }`}
                >
                    {isSearching && <Loader2 className="h-4 w-4 animate-spin" />}
                    {isSearching ? "Buscando..." : "Buscar"}
                </button>
            </div>
        </section>
    );
};

export default BuscadorPorFecha;