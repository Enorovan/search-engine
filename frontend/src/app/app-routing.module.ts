import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ResultComponent } from './result/result.component';
import { SearchComponent } from './search/search.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'search', component: SearchComponent },
  { path: 'search/french', component: ResultComponent },
  { path: 'search/english', component: ResultComponent },
  { path: 'search/german', component: ResultComponent },
  { path: 'search/all', component: ResultComponent },
  { path: 'search/result/:word', component: ResultComponent },
  { path: 'search/result/regex/:regex', component: ResultComponent },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
