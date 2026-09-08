export const TipoDeBuscador = {
    FechaFactura: "FechaFactura",
    FechaSistema: "FechaSistema",
    Proveedor: "Proveedor",
} as const;

export type TipoDeBuscador = typeof TipoDeBuscador[keyof typeof TipoDeBuscador];
