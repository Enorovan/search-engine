import { Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent {
  myControl = new FormControl();
  word = new FormControl('');

  isRegEx = false;

  constructor(private _router: Router, private _route: ActivatedRoute) {}

  setIsRegEx() {    
    this.isRegEx = !this.isRegEx;
  }

  findResult() {    
    if (this.isRegEx) {
      this._router.navigate(["/search/result/regex/"+this.word.value]);
    } else if (!this.isRegEx) {
      this._router.navigate(["/search/result/"+this.word.value]);
    }
  }
}