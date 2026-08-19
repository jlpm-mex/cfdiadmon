import { useGetPendingDocs, useProcessDocs } from "../services/hooks/useDocuments";
import { Check, X, RefreshCw } from 'lucide-react';   

interface PendingDoc {
  pdfName: string;
  xmlName: string;
  message: string;
}

interface PendingDocsTableProps {
  pendingDocs?: PendingDoc[];
}

const PendingDocsTable = ({ pendingDocs = [] }: PendingDocsTableProps) => {
  return (
    <table className="w-full text-left text-slate-300 border-collapse">
      <thead>
        <tr className="border-b border-white/10 font-mono text-[11px] uppercase tracking-widest text-slate-400">
          <th className="pb-3 px-4">File Name</th>
          <th className="pb-3 px-4">PDF</th>
          <th className="pb-3 px-4">XML</th>
          <th className="pb-3 px-4">Status</th>
        </tr>
      </thead>
      <tbody>
        {pendingDocs.map((v, k) => {
          return (
            <tr key={k} className="border-b border-white/5 hover:bg-white/[0.02] transition-colors">
              <td className="py-3 px-4">{v.pdfName.substring(0,v.pdfName.length-4)}</td>
              <td className="py-3 px-4">{v.pdfName  ? <Check className="text-green-500"/> : <X className="text-red-600"/>}</td>
              <td className="py-3 px-4">{v.xmlName ? <Check className="text-green-500"/> : <X className="text-red-600"/>}</td>
              <td className="py-3 px-4 text-sm text-slate-300">{v.message}</td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
};


const PanelProcesar = () => {                                                                                                                                                                                                             
      const {                                                                                                                                                                                                                                 
        data: pendingData,                                                                                                                                                                                                                    
        isLoading,                                                                                                                                                                                                                            
        isSuccess: pendingDocsSuccess,                                                                                                                                                                                                        
        refetch                                                                                                                                                                                                                               
      } = useGetPendingDocs();                                                                                                                                                                                                                
                                                                                                                                                                                                                                              
      const {                                                                                                                                                                                                                                 
        mutate: procesar,                                                                                                                                                                                                                     
        isPending,                                                                                                                                                                                                                            
        isSuccess: processDocsSuccess,                                                                                                                                                                                                        
        data: processResult,                                                                                                                                                                                                                  
        reset: resetProcess // Para limpiar el resultado y volver a la vista inicial                                                                                                                                                          
      } = useProcessDocs();                                                                                                                                                                                                                   
                                                                                                                                                                                                                                              
      const unprocessedDocs = pendingData?.data?.unprocessedList ?? [];                                                                                                                                                                       
      const hasPendingDocs = unprocessedDocs.length > 0;                                                                                                                                                                                      
      const isDisabled = isLoading || isPending || !hasPendingDocs;                                                                                                                                                                           
                                                                                                                                                                                                                                              
      const handleProcesar = () => {                                                                                                                                                                                                          
        procesar();                                                                                                                                                                                                                           
      };                                                                                                                                                                                                                                      
                                                                                                                                                                                                                                              
      const handleReset = () => {                                                                                                                                                                                                             
        resetProcess(); // Limpia la mutación                                                                                                                                                                                                 
        refetch();      // Refresca la lista de pendientes en disco                                                                                                                                                                           
      };                                                                                                                                                                                                                                      
                                                                                                                                                                                                                                              
      return (                                                                                                                                                                                                                                
        <section className="space-y-6">                                                                                                                                                                                                       
          <div className="flex items-center justify-between">                                                                                                                                                                                 
            <h3 className="text-2xl font-bold">Documentos pendientes de procesar</h3>                                                                                                                                                                                                                            
          </div>                                                                                                                                                                                                                              
                                                                                                                                                                                                                                              
          {/* MOMENTO 2: RESULTADO DE LA EJECUCIÓN */}                                                                                                                                                                                        
          {processDocsSuccess && processResult && (                                                                                                                                                                                     
            <div className="space-y-4">                                                                                                                                                                                                       
              {/* Tarjetas de Resumen */}                                                                                                                                                                                                     
              <div className="grid grid-cols-3 gap-4">                                                                                                                                                                                        
                <div className="bg-slate-800/60 border border-slate-700 p-4 rounded-xl">                                                                                                                                                      
                  <span className="text-xs uppercase text-slate-400 font-mono">Total Archivos</span>                                                                                                                                          
                  <p className="text-2xl font-bold">{processResult.totalFiles}</p>                                                                                                                                                       
                </div>                                                                                                                                                                                                                        
                <div className="bg-emerald-950/40 border border-emerald-800/50 p-4 rounded-xl text-emerald-400">                                                                                                                              
                  <span className="text-xs uppercase text-emerald-500 font-mono">Procesados Éxito</span>                                                                                                                                      
                  <p className="text-2xl font-bold">{processResult.totalProcessedFiles}</p>                                                                                                                                                   
                </div>                                                                                                                                                                                                                        
                <div className="bg-rose-950/40 border border-rose-800/50 p-4 rounded-xl text-rose-400">                                                                                                                                       
                  <span className="text-xs uppercase text-rose-500 font-mono">Con Errores</span>                                                                                                                                              
                  <p className="text-2xl font-bold">{processResult.unprocessedList.length}</p>                                                                                                                                           
                </div>                                                                                                                                                                                                                        
              </div>                                                                                                                                                                                                                          
                                                                                                                                                                                                                                              
              {/* Tabla Consolidada de Resultados */}                                                                                                                                                                                         
              <div className="rounded-xl border border-white/10 bg-slate-900/60 p-5 overflow-x-auto">                                                                                                                                         
                <table className="w-full text-left text-slate-300 border-collapse">                                                                                                                                                           
                  <thead>                                                                                                                                                                                                                     
                    <tr className="border-b border-white/10 text-xs font-mono uppercase text-slate-400">                                                                                                                                      
                      <th className="pb-3 px-4">Archivo</th>                                                                                                                                                                                  
                      <th className="pb-3 px-4">Resultado</th>                                                                                                                                                                                
                      <th className="pb-3 px-4">Detalle / Mensaje</th>                                                                                                                                                                        
                    </tr>                                                                                                                                                                                                                     
                  </thead>                                                                                                                                                                                                                    
                  <tbody>                                                                                                                                                                                                                     
                    {/* 1. Lista de Archivos Procesados con Éxito */}                                                                                                                                                                         
                    {processResult.processedList.map((doc, idx) => (                                                                                                                                                                     
                      <tr key={`success-${idx}`} className="border-b border-white/5 hover:bg-emerald-500/[0.03]">                                                                                                                             
                        <td className="py-3 px-4 font-mono text-sm">{doc.xmlName}</td>                                                                                                                                       
                        <td className="py-3 px-4">                                                                                                                                                                                            
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">                                                    
                            <Check className="w-3.5 h-3.5" /> Procesado                                                                                                                                                                       
                          </span>                                                                                                                                                                                                             
                        </td>                                                                                                                                                                                                                 
                        <td className="py-3 px-4 text-sm text-slate-400">Guardado en BD y movido a procesados</td>                                                                                                                            
                      </tr>                                                                                                                                                                                                                   
                    ))}                                                                                                                                                                                                                       
                                                                                                                                                                                                                                              
                    {/* 2. Lista de Archivos que Fallaron */}                                                                                                                                                                                 
                    {processResult.unprocessedList.map((doc, idx) => (
                      <tr key={`fail-${idx}`} className="border-b border-white/5 hover:bg-rose-500/[0.03]">                                                                                                                                   
                        <td className="py-3 px-4 font-mono text-sm">{doc.xmlName}</td>                                                                                                                                       
                        <td className="py-3 px-4">                                                                                                                                                                                            
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20">                                                             
                            <X className="w-3.5 h-3.5" /> Error                                                                                                                                                                               
                          </span>                                                                                                                                                                                                             
                        </td>                                                                                                                                                                                                                 
                        <td className="py-3 px-4 text-sm text-rose-300">{doc.message}</td>                                                                                                                                                    
                      </tr>                                                                                                                                                                                                                   
                    ))}                                                                                                                                                                                                                       
                  </tbody>                                                                                                                                                                                                                    
                </table>                                                                                                                                                                                                                      
              </div>                                                                                                                                                                                                                          
            </div>                                                                                                                                                                                                                            
          )}       
          {/* Si ya se procesó, mostramos botón para refrescar / volver */}                                                                                                                                                                 
          {processDocsSuccess && (
            <div className="flex justify-center">                                                                                                                                                                                                          
              <button                                                                                                                                                                                                                         
                onClick={handleReset}                                                                                                                                                                                                         
                className="flex items-center gap-2 text-sm text-cyan-300 hover:text-cyan-500 transition-colors cursor-pointer"                                                                                                                                 
              >                                                                                                                                                                                                                               
                <RefreshCw className="w-4 h-4" /> Volver a lista de pendientes                                                                                                                                                                
              </button>
            </div>
          )}                                                                                                                                                                                                                               
                                                                                                                                                                                                                                              
          {/* MOMENTO 1: VISTA PREVIA INICIAL (Antes de procesar) */}                                                                                                                                                                         
          {!processDocsSuccess && (                                                                                                                                                                                                           
            <>                                                                                                                                                                                                                                
              <div className="rounded-[18px] border border-white/10 bg-gradient-to-b from-[#16263D] to-[#16263D]/60 p-7 min-h-64 overflow-x-auto">                                                                                            
                {pendingDocsSuccess && (                                                                                                                                                                                                      
                  <PendingDocsTable pendingDocs={unprocessedDocs} />                                                                                                                                                                          
                )}                                                                                                                                                                                                                            
              </div>                                                                                                                                                                                                                          
                                                                                                                                                                                                                                              
              <div className="m-3 text-center">                                                                                                                                                                                               
                <button                                                                                                                                                                                                                       
                  type="button"                                                                                                                                                                                                               
                  disabled={isDisabled}                                                                                                                                                                                                       
                  onClick={handleProcesar}                                                                                                                                                                                                    
                  className={`rounded-full border-2 px-9 py-3.5 text-sm font-bold uppercase tracking-wider transition-all w-75                                                                                                                
                    ${!isDisabled                                                                                                                                                                                                             
                        ? "cursor-pointer border-green-700 bg-green-500 text-slate-100 hover:-translate-y-0.5 active:scale-95 shadow-lg shadow-green-900/30"                                                                                  
                        : "cursor-not-allowed border-slate-700 bg-slate-800 text-slate-500 opacity-55"                                                                                                                                        
                    }`}                                                                                                                                                                                                                       
                >                                                                                                                                                                                                                             
                  {isPending ? "Procesando lote..." : `Procesar (${unprocessedDocs.length} pendientes)`}                                                                                                                                      
                </button>                                                                                                                                                                                                                     
              </div>                                                                                                                                                                                                                          
            </>                                                                                                                                                                                                                               
          )}                                                                                                                                                                                                                                  
        </section>                                                                                                                                                                                                                            
      );                                                                                                                                                                                                                                      
    };                

export default PanelProcesar