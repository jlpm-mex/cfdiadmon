import { useState } from 'react'
import PanelProcesar from '../components/PanelProcesar';
import PanelBuscar from '../components/PanelBuscar';

type Pestana = "procesar" | "buscar";

const CfdiPage = () => {

    const [pestana, setPestana] = useState<Pestana>("procesar");

    return (
        <div className="flex min-h-screen items-center justify-center bg-[#0F1B2D] px-4 py-8 text-slate-100">
            <div className="w-full max-w-3xl">
                <div className="mb-3 flex items-center gap-2 font-mono text-xs uppercase tracking-widest text-teal-400">
                    <span className="inline-block h-px w-4 bg-teal-400" />
                    Bandeja de documentos
                </div>

                <h1 className="mb-2 text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
                    Gestiona tus documentos PDF y XML
                </h1>

                <div role="tablist" className="mb-5 flex gap-5 border-b border-white/10">
                    <button
                        type="button"
                        role="tab"
                        aria-selected={pestana === "procesar"}
                        onClick={() => setPestana("procesar")}
                        className={`relative pb-3 font-semibold text-[14.5px] transition-colors
              ${pestana === "procesar" ? "text-slate-100" : "text-slate-400 hover:text-slate-200"}`}
                    >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.6} className="mr-1.5 inline-block h-3.75 w-3.75 -translate-y-px">
                            <path d="M12 3v12" />
                            <path d="m7 10 5 5 5-5" />
                            <path d="M5 21h14" />
                        </svg>
                        Procesar
                        {pestana === "procesar" && (
                            <span className="absolute inset-x-0 -bottom-px h-0.5 rounded bg-[#C4423E]" />
                        )}
                    </button>

                    <button
                        type="button"
                        role="tab"
                        aria-selected={pestana === "buscar"}
                        onClick={() => setPestana("buscar")}
                        className={`relative pb-3 font-semibold text-[14.5px] transition-colors
              ${pestana === "buscar" ? "text-slate-100" : "text-slate-400 hover:text-slate-200"}`}
                    >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.6} className="mr-1.5 inline-block h-3.75 w-3.75 -translate-y-px">
                            <circle cx="11" cy="11" r="7" />
                            <path d="m21 21-4.3-4.3" />
                        </svg>
                        Buscar CFDI
                        {pestana === "buscar" && (
                            <span className="absolute inset-x-0 -bottom-px h-0.5 rounded bg-teal-400" />
                        )}
                    </button>
                </div>

                {pestana === "procesar" ? <PanelProcesar /> : <PanelBuscar />}
            </div>
        </div>
    );
}

export default CfdiPage