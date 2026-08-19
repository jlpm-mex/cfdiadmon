import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useUploadDocs } from "../services/hooks/useDocuments";

type TipoArchivo = "pdf" | "xml";

const EXTENSION: Record<TipoArchivo, string> = {
  pdf: ".pdf",
  xml: ".xml",
};

const ACCEPT: Record<TipoArchivo, string> = {
  pdf: ".pdf,application/pdf",
  xml: ".xml,text/xml,application/xml",
};

function claveDe(archivo: File): string {
  return `${archivo.name}__${archivo.size}`;
}

interface ZonaArchivosProps {
  tipo: TipoArchivo;
  numero: number;
  archivos: File[];
  onAgregar: (tipo: TipoArchivo, archivos: File[]) => void;
  onQuitar: (tipo: TipoArchivo, clave: string) => void;
}

function ZonaArchivos({ tipo, numero, archivos, onAgregar, onQuitar }: ZonaArchivosProps) {
  const [arrastrando, setArrastrando] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const colorAcento = tipo === "pdf" ? "text-[#C4423E]" : "text-[#2FA88A]";
  const colorBorde = tipo === "pdf" ? "border-[#C4423E]" : "border-[#2FA88A]";

  const filtrarValidos = useCallback(
    (lista: FileList | File[]) => Array.from(lista).filter((f) => f.name.toLowerCase().endsWith(EXTENSION[tipo])),
    [tipo]
  );

  const manejarArchivos = useCallback(
    (lista: FileList | File[]) => {
      const validos = filtrarValidos(lista);
      if (validos.length > 0) onAgregar(tipo, validos);
      if (inputRef.current) inputRef.current.value = "";
    },
    [filtrarValidos, onAgregar, tipo]
  );

  return (
    <div
      className={`relative flex flex-col rounded-2xl border-[1.5px] px-4 py-5 transition-all
        ${
          archivos.length > 0
            ? `${colorBorde} bg-white/3`
            : arrastrando
            ? "scale-[1.01] border-teal-400 bg-teal-400/10"
            : "border-dashed border-white/15 hover:border-white/30"
        }`}
      onDragOver={(e) => {
        e.preventDefault();
        setArrastrando(true);
      }}
      onDragLeave={() => setArrastrando(false)}
      onDrop={(e) => {
        e.preventDefault();
        setArrastrando(false);
        manejarArchivos(e.dataTransfer.files);
      }}
    >
      <label className="flex cursor-pointer flex-col items-center text-center">
        <input
          ref={inputRef}
          type="file"
          multiple
          accept={ACCEPT[tipo]}
          aria-label={`Subir archivos ${tipo.toUpperCase()}`}
          className="absolute inset-0 cursor-pointer opacity-0"
          onChange={(e) => e.target.files && manejarArchivos(e.target.files)}
        />

        <svg viewBox="0 0 24 24" fill="none" strokeWidth={1.4} className={`mb-3 h-9 w-9 ${colorAcento}`}>
          <path d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z" stroke="currentColor" />
          <path d="M15 2v5h5" stroke="currentColor" />
          <text
            x="12"
            y="17"
            fontFamily="'IBM Plex Mono', monospace"
            fontSize="6.5"
            fill="currentColor"
            textAnchor="middle"
            stroke="none"
          >
            {tipo.toUpperCase()}
          </text>
        </svg>

        <div className="mb-1 font-mono text-[11px] uppercase tracking-widest text-slate-400">
          Casilla {numero} · {tipo.toUpperCase()}
        </div>

        <div className="text-sm text-slate-100">
          Arrastra uno o varios archivos {tipo.toUpperCase()}
          <br />o haz clic para buscarlos
        </div>
      </label>

      {archivos.length > 0 && (
        <ul className="relative z-10 mt-4 flex flex-col gap-1.5">
          {archivos.map((archivo) => {
            const clave = claveDe(archivo);
            return (
              <li
                key={clave}
                className="flex items-center justify-between gap-2 rounded-lg bg-black/20 px-2.5 py-1.5"
              >
                <span className="truncate font-mono text-xs text-teal-400">{archivo.name}</span>
                <button
                  type="button"
                  className="shrink-0 font-mono text-[11px] text-slate-400 underline"
                  onClick={() => onQuitar(tipo, clave)}
                >
                  Quitar
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}

export default function ProcesadorDocumentos() {
  const [pdfs, setPdfs] = useState<File[]>([]);
  const [xmls, setXmls] = useState<File[]>([]);
  const [progreso, setProgreso] = useState(0);
  const [resultado, setResultado] = useState<string | null>(null);
  const { mutate, isPending, isSuccess, error, data } = useUploadDocs();

  const agregarArchivos = useCallback((tipo: TipoArchivo, nuevos: File[]) => {
    setResultado(null);
    const setter = tipo === "pdf" ? setPdfs : setXmls;
    setter((prev) => {
      const clavesExistentes = new Set(prev.map(claveDe));
      const sinDuplicados = nuevos.filter((f) => !clavesExistentes.has(claveDe(f)));
      return [...prev, ...sinDuplicados];
    });
  }, []);

  const quitarArchivo = useCallback((tipo: TipoArchivo, clave: string) => {
    setResultado(null);
    const setter = tipo === "pdf" ? setPdfs : setXmls;
    setter((prev) => prev.filter((f) => claveDe(f) !== clave));
  }, []);

  const cantidadesCoinciden = pdfs.length > 0 && pdfs.length === xmls.length;

  const { estadoTexto, estadoColor } = useMemo(() => {
    if (isPending) return { estadoTexto: `Procesando documentos… ${progreso}%`, estadoColor: "text-slate-400" };
    if (error) return  { estadoTexto: resultado, estadoColor: "text-[#C4423E]" };
    if (resultado) return { estadoTexto: resultado, estadoColor: "text-teal-400" };
    if (pdfs.length === 0 && xmls.length === 0) {
      return { estadoTexto: "Esperando archivos PDF y XML…", estadoColor: "text-slate-400" };
    }
    if (cantidadesCoinciden) {
      return {
        estadoTexto: `${pdfs.length} PDF y ${xmls.length} XML listos para procesar.`,
        estadoColor: "text-teal-400",
      };
    }
    return {
      estadoTexto: `Debe haber el mismo número de PDF y XML (actualmente: ${pdfs.length} PDF, ${xmls.length} XML).`,
      estadoColor: "text-[#C4423E]",
    };
  }, [isPending,error, progreso, pdfs.length, xmls.length, cantidadesCoinciden]);

  const handleProcesar = () => {
    if (!cantidadesCoinciden) return;
    setResultado("");
    setProgreso(0); 
    mutate({ pdfs, xmls, onProgreso: setProgreso },{
      onSuccess: (data) => {
        setPdfs([]);
        setXmls([]);
        setProgreso(0);
        setResultado(`Se han subido exitosamente ${data.length} documentos.`);
      },
      onError: (error) => {
        setProgreso(0);
        setResultado("Ha ocurrido un problema, los archivos no se han subido");
        console.error("Error inmediato de Axios:", error);
      }
    });
  };



  return (
    <div className="flex min-h-screen items-center justify-center bg-[#0F1B2D] px-4 py-8 text-slate-100">
      <div className="w-full max-w-2xl">
        <div className="mb-3 flex items-center gap-2 font-mono text-xs uppercase tracking-widest text-teal-400">
          <span className="inline-block h-px w-4 bg-teal-400" />
          Bandeja de documentos
        </div>

        <h1 className="mb-2 text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
          Sube tus PDF y tus XML para procesarlos
        </h1>
        <p className="mb-9 max-w-lg text-sm leading-relaxed text-slate-400">
          Puedes subir varios archivos a la vez. Debe existir el mismo número de archivos PDF y XML,
          ya que cada PDF debe corresponder a un XML.
        </p>

        <div className="rounded-[18px] border border-white/10 bg-linear-to-b from-[#16263D] to-[#16263D]/60 p-7">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <ZonaArchivos tipo="pdf" numero={1} archivos={pdfs} onAgregar={agregarArchivos} onQuitar={quitarArchivo} />
            <ZonaArchivos tipo="xml" numero={2} archivos={xmls} onAgregar={agregarArchivos} onQuitar={quitarArchivo} />
          </div>

          <div className={`mt-6 min-h-4.5 text-center font-mono text-[12.5px] ${estadoColor}`}>
            {estadoTexto}
          </div>

          <div className="mt-6 flex justify-center">
            <button
              type="button"
              disabled={!cantidadesCoinciden || isPending}
              onClick={handleProcesar}
              className={`rounded-full border-2 px-9 py-3.5 text-sm font-bold uppercase tracking-wider transition-transform
                ${
                  cantidadesCoinciden && !isPending
                    ? "cursor-pointer border-[#C4423E] bg-[#C4423E] text-slate-100 hover:-translate-y-0.5 active:scale-95"
                    : "cursor-not-allowed border-[#7A2B29] bg-[#7A2B29] text-slate-100 opacity-55"
                }
                focus-visible:outline-2 focus-visible:outline-teal-400 focus-visible:outline-offset-2`}
            >
              Enviar
            </button>
          </div>
        </div>

        <footer className="mt-5 text-center font-mono text-xs text-slate-400">
          Formatos aceptados: .pdf y .xml únicamente · 1 PDF = 1 XML
        </footer>
      </div>
    </div>
  );
}
