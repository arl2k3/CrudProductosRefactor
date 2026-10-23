import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Catalogo, Producto, ProductoAlta } from '../models/producto.model';

export const API_PRODUCTOS = 'http://localhost:8080/api/productos';

export interface ConsultaCatalogo {
  nombre?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);

  consultar(consulta: ConsultaCatalogo): Observable<Catalogo> {
    let params = new HttpParams();
    const nombre = consulta.nombre?.trim();
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    if (consulta.page !== undefined) {
      params = params.set('page', consulta.page);
    }
    if (consulta.size !== undefined) {
      params = params.set('size', consulta.size);
    }
    return this.http.get<Catalogo>(API_PRODUCTOS, { params });
  }

  obtener(id: string): Observable<Producto> {
    return this.http.get<Producto>(`${API_PRODUCTOS}/${id}`);
  }

  registrar(producto: ProductoAlta): Observable<Producto> {
    return this.http.post<Producto>(API_PRODUCTOS, producto);
  }

  actualizarPrecio(id: string, precio: number): Observable<Producto> {
    return this.http.patch<Producto>(`${API_PRODUCTOS}/${id}/precio`, { precio });
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${API_PRODUCTOS}/${id}`);
  }
}
