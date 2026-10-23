export interface Producto {
  id: string;
  nombre: string;
  precio: number;
  stock: number;
  estadoInventario: 'DISPONIBLE' | 'AGOTADO';
}

export interface ProductoAlta {
  nombre: string;
  precio: number;
  stock: number;
}

export interface Catalogo {
  productos: Producto[];
  paginado: boolean;
  page?: number;
  size?: number;
  totalElementos?: number;
  totalPaginas?: number;
}

export interface ErrorApi {
  mensajes?: string[];
}
