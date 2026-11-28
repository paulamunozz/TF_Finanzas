import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { Component, ChangeDetectionStrategy, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/services/auth.service';
import { ReferenceCatalogService } from '../../core/services/reference-catalog.service';

interface Business {id?:number; ruc:string; razonSocial:string; direccion:string; telefono:string; giroId:number|null; estado:boolean; contrasena?:string}
@Component({selector:'app-business-admin',imports:[FieldHelp, FormsModule,MatButtonModule],changeDetection:ChangeDetectionStrategy.OnPush,
templateUrl:'./business-admin.html'})
export class BusinessAdmin {
  private http=inject(HttpClient);
  private auth=inject(AuthService);
  private router=inject(Router);
  protected rows=signal<Business[]|null>(null);
  protected readonly giros = inject(ReferenceCatalogService).sectors();
  protected error=signal('');
  protected busy=signal(false);
  protected draft: Business|null=null;
  protected pending=signal<Business|null>(null);
  private path='/api/admin/businesses';
  constructor(){ this.load(); }
  protected load(){
    this.error.set('');
    this.http.get<Business[]>(this.path).subscribe({next:r=>this.rows.set(r),error:()=>this.error.set('No se pudieron cargar los negocios.')});
  }
  protected edit(row?:Business){ this.draft=row?{...row,contrasena:''}:{razonSocial:'',ruc:'',direccion:'',telefono:'',estado:true,giroId:null,contrasena:''}; }
  protected save(){
    const d=this.draft;
    if(!d || d.giroId == null || this.busy() || this.giros.isLoading() || !this.giros.hasValue()) return;
    this.submit(d.id?this.http.put(this.path+'/'+d.id,d):this.http.post(this.path,d));
  }
  protected toggle(row:Business){this.submit(this.http.put(this.path+'/'+row.id+'/active',null,{params:{value:!row.estado}}));}
  private submit(request:import('rxjs').Observable<unknown>){
    if(this.busy()) return;
    this.busy.set(true);
    request.subscribe({next:()=>{this.draft=null;this.pending.set(null);this.busy.set(false);this.load();},
      error:e=>{this.busy.set(false);this.error.set(e.error?.message??'No se pudo guardar el cambio.');}});
  }
  protected logout(){this.auth.logout().subscribe({next:()=>this.router.navigate(['/auth/login']),error:()=>this.error.set('No se pudo cerrar la sesión.')});}
}
