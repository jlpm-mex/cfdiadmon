import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { uploadDocs, getPendingDocs, processDocs, findDocumentsBySupplier } from "../api/documentService";

export function useUploadDocs(){
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async ({ pdfs, xmls, onProgreso }: 
      {pdfs: File[], xmls : File[], onProgreso?:(porcentaje: number) => void}) => {
      const formData = new FormData();
      pdfs.forEach((f) => formData.append("files", f));
      xmls.forEach((f) => formData.append("files", f));

      const {data, status} = await uploadDocs(formData, onProgreso);

      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-docs'] });
    }
  });
}

export const useGetPendingDocs = () => {
  return useQuery({
    queryKey: ['pending-docs'],
    queryFn: getPendingDocs,
    refetchOnWindowFocus: false, // Evita que recargue cada vez que cambias de pestaña
    staleTime: 1000 * 60 * 5,    // Considera la data "fresca" por 5 minutos
  });
}

export const useProcessDocs = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const {data, status} = await processDocs();
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-docs'] });
    }
  })
}

export const useGetDocsBySupplier = (rfc: string, year: number, options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: ['docs-by-supplier'],
    queryFn: () => findDocumentsBySupplier(rfc, year),
    enabled: options?.enabled ?? true,
    refetchOnWindowFocus: false, 
    staleTime: 1000 * 60 * 5,
  })
}

