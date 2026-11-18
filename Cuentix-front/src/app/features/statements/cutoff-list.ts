import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../shared/ui/page-header/page-header';

interface Entry {id:number; purchaseId:number; purchaseDate:string; number:number; capital:number; interest:number; late:number; adjustment:boolean}
interface Report {id:number; customer:string; currency:string; paymentDay:string; entries:Entry[]}
@Component({selector:'app-cutoff-list',imports:[PageHeader,CurrencyPipe,DatePipe,MatButtonModule,RouterLink],changeDetection:ChangeDetectionStrategy.OnPush,templateUrl:'./cutoff-list.html'})
export class CutoffList {
  private http=inject(HttpClient);
  protected month=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Lima',year:'numeric',month:'2-digit'}).format(new Date());
  protected reports=signal<Report[]|null>(null);
  protected error=signal('');
  constructor(){ this.load(this.month); }
  protected load(month:string){
    if (!month) return;
    this.month=month;
    this.reports.set(null);
    this.error.set('');
    this.http.get<Report[]>('/api/business/cutoff',{params:{month}}).subscribe({
      next:r=>{if(this.month===month)this.reports.set(r);},
      error:()=>{if(this.month===month)this.error.set('No se pudieron cargar los listados.');}
    });
  }
  protected total(entries:Entry[]){return entries.reduce((sum,e)=>sum+e.capital+e.interest+e.late,0);}
}
