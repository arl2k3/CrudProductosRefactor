import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, FormControl, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { API_PRODUCTOS, ProductoService } from '../core/producto.service';
import { ErrorApi, Producto } from '../models/producto.model';

@Component({
  selector: 'app-catalogo',
  imports: [ReactiveFormsModule],
  templateUrl: './catalogo.component.html'
})
export class CatalogoComponent {
  private readonly productosApi = inject(ProductoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  readonly api = API_PRODUCTOS;
  readonly busqueda = new FormControl('', { nonNullable: true });
  readonly alta = this.fb.group({
    nombre: this.fb.nonNullable.control('', [textoObligatorio, Validators.maxLength(255)]),
    precio: this.fb.control<number | null>(null, [precioMayorQueCero]),
    stock: this.fb.nonNullable.control(0, [stockValido])
  });

  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(false);
  readonly errorCatalogo = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly paginar = signal(false);
  readonly page = signal(0);
  readonly size = signal(5);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly filtroNombre = signal('');
  readonly detalle = signal<Producto | null>(null);
  readonly detalleCargando = signal(false);
  readonly errorDetalle = signal<string | null>(null);
  readonly errorAlta = signal<string | null>(null);
  readonly altaIntentada = signal(false);
  readonly guardandoAlta = signal(false);
  readonly precios = signal<Record<string, string>>({});
  readonly erroresPrecio = signal<Record<string, string>>({});
  readonly guardandoPrecio = signal<string | null>(null);
  readonly eliminando = signal<string | null>(null);

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCatalogo.set(null);
    const paginado = this.paginar();
    this.productosApi.consultar({
      nombre: this.filtroNombre() || undefined,
      page: paginado ? this.page() : undefined,
      size: paginado ? this.size() : undefined
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (catalogo) => {
        this.productos.set(catalogo.productos);
        this.sincronizarPrecios(catalogo.productos);
        this.totalElementos.set(catalogo.totalElementos ?? catalogo.productos.length);
        this.totalPaginas.set(catalogo.totalPaginas ?? 1);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.productos.set([]);
        this.errorCatalogo.set(mensajeDeError(error));
        this.cargando.set(false);
      }
    });
  }

  registrar(): void {
    this.altaIntentada.set(true);
    this.errorAlta.set(null);
    this.aviso.set(null);
    if (this.alta.invalid) {
      this.alta.markAllAsTouched();
      return;
    }

    const precio = Number(this.alta.controls.precio.value);
    const stock = Number(this.alta.controls.stock.value);
    this.guardandoAlta.set(true);
    this.productosApi.registrar({
      nombre: this.alta.controls.nombre.value.trim(),
      precio,
      stock
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (producto) => {
        this.guardandoAlta.set(false);
        this.alta.reset({ nombre: '', precio: null, stock: 0 });
        this.altaIntentada.set(false);
        this.aviso.set(`${producto.nombre} quedó ${producto.estadoInventario === 'DISPONIBLE' ? 'disponible' : 'agotado'}.`);
        this.page.set(0);
        this.cargar();
      },
      error: (error: unknown) => {
        this.guardandoAlta.set(false);
        this.errorAlta.set(mensajeDeError(error));
      }
    });
  }

  buscar(): void {
    this.filtroNombre.set(this.busqueda.value.trim());
    this.page.set(0);
    this.aviso.set(null);
    this.cargar();
  }

  limpiarBusqueda(): void {
    this.busqueda.setValue('');
    this.buscar();
  }

  alternarPaginacion(evento: Event): void {
    this.paginar.set((evento.target as HTMLInputElement).checked);
    this.page.set(0);
    this.cargar();
  }

  cambiarTamano(evento: Event): void {
    this.size.set(Number((evento.target as HTMLSelectElement).value));
    this.page.set(0);
    this.cargar();
  }

  irPagina(nueva: number): void {
    if (nueva < 0 || (this.totalPaginas() > 0 && nueva >= this.totalPaginas())) {
      return;
    }
    this.page.set(nueva);
    this.cargar();
  }

  ver(id: string): void {
    this.detalleCargando.set(true);
    this.errorDetalle.set(null);
    this.productosApi.obtener(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (producto) => {
        this.detalle.set(producto);
        this.detalleCargando.set(false);
      },
      error: (error: unknown) => {
        this.detalle.set(null);
        this.detalleCargando.set(false);
        this.errorDetalle.set(mensajeDeError(error));
      }
    });
  }

  cerrarDetalle(): void {
    this.detalle.set(null);
    this.errorDetalle.set(null);
  }

  editarPrecio(id: string, evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    this.precios.update((actual) => ({ ...actual, [id]: valor }));
    this.erroresPrecio.update((actual) => {
      const siguiente = { ...actual };
      delete siguiente[id];
      return siguiente;
    });
  }

  guardarPrecio(producto: Producto): void {
    const precio = Number(this.precios()[producto.id]);
    if (!Number.isFinite(precio) || precio <= 0) {
      this.erroresPrecio.update((actual) => ({
        ...actual,
        [producto.id]: 'El precio debe ser mayor que cero.'
      }));
      return;
    }

    this.guardandoPrecio.set(producto.id);
    this.aviso.set(null);
    this.productosApi.actualizarPrecio(producto.id, precio)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizado) => {
          this.guardandoPrecio.set(null);
          this.productos.update((lista) => lista.map((item) => item.id === actualizado.id ? actualizado : item));
          this.precios.update((actual) => ({ ...actual, [actualizado.id]: actualizado.precio.toFixed(2) }));
          if (this.detalle()?.id === actualizado.id) {
            this.detalle.set(actualizado);
          }
          this.aviso.set(`Precio de ${actualizado.nombre} actualizado.`);
        },
        error: (error: unknown) => {
          this.guardandoPrecio.set(null);
          this.erroresPrecio.update((actual) => ({ ...actual, [producto.id]: mensajeDeError(error) }));
        }
      });
  }

  eliminar(producto: Producto): void {
    const confirmar = window.confirm(`¿Dar de baja "${producto.nombre}"? Dejará de aparecer en el catálogo.`);
    if (!confirmar) {
      return;
    }

    this.eliminando.set(producto.id);
    this.aviso.set(null);
    this.productosApi.eliminar(producto.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.eliminando.set(null);
        if (this.detalle()?.id === producto.id) {
          this.detalle.set(null);
        }
        this.aviso.set(`${producto.nombre} fue dado de baja.`);
        if (this.paginar() && this.productos().length === 1 && this.page() > 0) {
          this.page.update((actual) => actual - 1);
        }
        this.cargar();
      },
      error: (error: unknown) => {
        this.eliminando.set(null);
        this.errorCatalogo.set(mensajeDeError(error));
      }
    });
  }

  mostrarError(campo: 'nombre' | 'precio' | 'stock', codigo: string): boolean {
    return this.altaIntentada() && this.alta.controls[campo].hasError(codigo);
  }

  precioDe(producto: Producto): string {
    return this.precios()[producto.id] ?? producto.precio.toFixed(2);
  }

  formatoPrecio(valor: number): string {
    return new Intl.NumberFormat('es-CO', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(valor);
  }

  private sincronizarPrecios(productos: Producto[]): void {
    const siguientes: Record<string, string> = {};
    for (const producto of productos) {
      siguientes[producto.id] = producto.precio.toFixed(2);
    }
    this.precios.set(siguientes);
    this.erroresPrecio.set({});
  }
}

function textoObligatorio(control: AbstractControl): ValidationErrors | null {
  return String(control.value ?? '').trim().length > 0 ? null : { obligatorio: true };
}

function precioMayorQueCero(control: AbstractControl): ValidationErrors | null {
  if (control.value === null || String(control.value) === '') {
    return { obligatorio: true };
  }
  const numero = Number(control.value);
  if (!Number.isFinite(numero) || numero <= 0) {
    return { mayorQueCero: true };
  }
  return null;
}

function stockValido(control: AbstractControl): ValidationErrors | null {
  if (control.value === null || String(control.value) === '') {
    return { obligatorio: true };
  }
  const numero = Number(control.value);
  if (!Number.isInteger(numero) || numero < 0) {
    return { stock: true };
  }
  return null;
}

function mensajeDeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No hay conexión con la API en http://localhost:8080.';
    }
    const mensajes = (error.error as ErrorApi | null)?.mensajes;
    if (Array.isArray(mensajes) && mensajes.length > 0) {
      return mensajes.join(' ');
    }
  }
  return 'No se pudo completar la operación.';
}
