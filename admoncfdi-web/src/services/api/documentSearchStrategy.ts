import { TipoDeBuscador } from "../../types";

export interface SearchStrategy {
  getEndpoint(): string;
}


export class CfdiSearchStrategy implements SearchStrategy {
  getEndpoint(): string {
    return `${import.meta.env.VITE_API_BASE_URL}/api/cfdis/by-comprobante-date`;
  }

}

export class SystemSearchStrategy implements SearchStrategy {
  getEndpoint(): string {
    return `${import.meta.env.VITE_API_BASE_URL}/api/cfdis/by-registration-date`;
  }

}

export class SearchStrategyFactory {
  static create(tipoDeBuscador : TipoDeBuscador): SearchStrategy {
    switch(tipoDeBuscador) {
      case TipoDeBuscador.FechaFactura: return new CfdiSearchStrategy();
      case TipoDeBuscador.FechaSistema: return new SystemSearchStrategy();
      default: throw new Error('Tipo de búsqueda no soportado');
    }
  }
}
