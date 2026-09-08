import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";

interface PaginadorProps {
    currentPage: number; // 0-indexed (coincide con Spring Data JPA Pageable)
    totalPages: number;
    totalElements: number;
    pageSize: number;
    onPageChange: (newPage: number) => void;
    isFetching?: boolean;
}

const Paginador = ({
    currentPage,
    totalPages,
    totalElements,
    pageSize,
    onPageChange,
    isFetching = false,
}: PaginadorProps) => {
    if (totalPages <= 1) return null;

    const fromIndex = currentPage * pageSize + 1;
    const toIndex = Math.min((currentPage + 1) * pageSize, totalElements);

    // Generador inteligente de números de página (ej. 1, 2, 3 ... 10)
    const getPageNumbers = () => {
        const pages: (number | string)[] = [];
        const maxVisible = 5;

        if (totalPages <= maxVisible) {
            for (let i = 0; i < totalPages; i++) pages.push(i);
        } else {
            pages.push(0);

            const start = Math.max(1, currentPage - 1);
            const end = Math.min(totalPages - 2, currentPage + 1);

            if (start > 1) pages.push("...");

            for (let i = start; i <= end; i++) {
                pages.push(i);
            }

            if (end < totalPages - 2) pages.push("...");

            pages.push(totalPages - 1);
        }

        return pages;
    };

    const pages = getPageNumbers();

    return (
        <div className="flex flex-col items-center justify-between gap-3 border-t border-white/10 bg-white/[0.02] px-4 py-3 sm:flex-row">
            {/* Texto informativo */}
            <div className="font-mono text-xs text-slate-400">
                Mostrando <span className="font-semibold text-slate-200">{fromIndex}</span> a{" "}
                <span className="font-semibold text-slate-200">{toIndex}</span> de{" "}
                <span className="font-semibold text-teal-400">{totalElements}</span> resultados
            </div>

            {/* Controles de página */}
            <div className="flex items-center gap-1">
                {/* Primera página */}
                <button
                    type="button"
                    disabled={currentPage === 0 || isFetching}
                    onClick={() => onPageChange(0)}
                    title="Primera página"
                    className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 text-slate-400 transition-colors hover:border-white/20 hover:bg-white/5 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                >
                    <ChevronsLeft className="h-4 w-4" />
                </button>

                {/* Página anterior */}
                <button
                    type="button"
                    disabled={currentPage === 0 || isFetching}
                    onClick={() => onPageChange(currentPage - 1)}
                    title="Página anterior"
                    className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 text-slate-400 transition-colors hover:border-white/20 hover:bg-white/5 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                >
                    <ChevronLeft className="h-4 w-4" />
                </button>

                {/* Números de página */}
                <div className="flex items-center gap-1">
                    {pages.map((p, index) => {
                        if (typeof p === "string") {
                            return (
                                <span
                                    key={`ellipsis-${index}`}
                                    className="px-1.5 font-mono text-xs text-slate-500"
                                >
                                    ...
                                </span>
                            );
                        }

                        const isActive = p === currentPage;

                        return (
                            <button
                                key={`page-${p}`}
                                type="button"
                                disabled={isFetching}
                                onClick={() => onPageChange(p)}
                                className={`flex h-8 min-w-[32px] items-center justify-center rounded-lg px-2 font-mono text-xs font-semibold transition-all ${
                                    isActive
                                        ? "border border-teal-400 bg-teal-400/20 text-teal-300 shadow-xs"
                                        : "border border-white/10 text-slate-400 hover:border-white/20 hover:bg-white/5 hover:text-white"
                                } disabled:cursor-not-allowed disabled:opacity-50`}
                            >
                                {p + 1}
                            </button>
                        );
                    })}
                </div>

                {/* Página siguiente */}
                <button
                    type="button"
                    disabled={currentPage >= totalPages - 1 || isFetching}
                    onClick={() => onPageChange(currentPage + 1)}
                    title="Página siguiente"
                    className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 text-slate-400 transition-colors hover:border-white/20 hover:bg-white/5 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                >
                    <ChevronRight className="h-4 w-4" />
                </button>

                {/* Última página */}
                <button
                    type="button"
                    disabled={currentPage >= totalPages - 1 || isFetching}
                    onClick={() => onPageChange(totalPages - 1)}
                    title="Última página"
                    className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 text-slate-400 transition-colors hover:border-white/20 hover:bg-white/5 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                >
                    <ChevronsRight className="h-4 w-4" />
                </button>
            </div>
        </div>
    );
};

export default Paginador;
