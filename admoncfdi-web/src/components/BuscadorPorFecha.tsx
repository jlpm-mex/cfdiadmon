
const BuscadorPorFecha = () =>{

    const handleBuscar = () => {
        
    }

    return(
        <section>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1.3fr_1fr_1fr_auto]">
                <div className="flex flex-col gap-1.5">
                    <label htmlFor="fecha-inicial" className="font-mono text-[11px] uppercase tracking-widest text-slate-400">
                        Fecha inicial
                    </label>
                    <input
                        id="fecha-inicial"
                        type="date"
                        value={""}
                        onChange={(e) => e.target.value}
                        className="rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2.5 text-sm text-slate-100 outline-none focus:border-teal-400 [&::-webkit-calendar-picker-indicator]:invert"
                    />
                </div>


                <div className="flex flex-col gap-1.5">
                    <label htmlFor="fecha-final" className="font-mono text-[11px] uppercase tracking-widest text-slate-400">
                        Fecha final
                    </label>
                    <input
                        id="fecha-final"
                        type="date"
                        value={""}
                        onChange={(e) => e.target.value}
                        className="rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2.5 text-sm text-slate-100 outline-none focus:border-teal-400 [&::-webkit-calendar-picker-indicator]:invert"
                    />
                </div>

                <button
                    type="button"
                    onClick={handleBuscar}
                    className="self-end rounded-lg border-2 border-teal-400 bg-teal-400 px-5 py-2.5 text-sm font-bold uppercase tracking-wide text-[#0F1B2D] transition-transform hover:brightness-110 active:scale-95"
                >
                    Buscar
                </button>
            </div>
        </section>
    )
}

export default BuscadorPorFecha