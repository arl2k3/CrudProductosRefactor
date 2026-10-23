import { Component } from '@angular/core';
import { CatalogoComponent } from './catalogo/catalogo.component';

@Component({
  selector: 'app-root',
  imports: [CatalogoComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {}
