import { QueryClient, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import  {getSuppliers} from '../api/SupplierService'

export const useLoadSuppliers = () => {
    return useQuery({
        queryKey:["getSuppliers"],
        queryFn: getSuppliers,
        refetchOnWindowFocus: false, // Evita que recargue cada vez que cambias de pestaña
        staleTime: 1000 * 60 * 5,    // Considera la data "fresca" por 5 minutos
    })
}