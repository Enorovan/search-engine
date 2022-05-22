import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Book } from '../models/book.model';
import { Position } from '../models/position.model';
import { Score } from '../models/score.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {

  constructor(private http: HttpClient) { }

  mainUrl: string = 'http://localhost:8080/api/bibliotheque/';
  specifiedLanguageBookUrl: string = 'searchLang?q=';
  allBookUrl: string = 'getAll';
  searchUrl: string = 'searchWord?q=';
  regexUrl: string = 'advancedSearch';
  upvoteUrl: string = 'click';
  positionUrl: string = '/index/';
  graphUrl: string = 'getGraph';
  scoreUrl: string = 'getAllScoring';
  suggestionUrl: string = 'suggestion/'

  getBooks(language: string) {
    if (language == "all") {
      return this.http.get<Book[]>(this.mainUrl+this.allBookUrl);
    } else {
      return this.http.get<Book[]>(this.mainUrl+this.specifiedLanguageBookUrl+language);
    }
  }
  getBooksByToken(word: string) {
    return this.http.get<Book[]>(this.mainUrl+this.searchUrl+word);
  }

  getPositionOfOccurence(id: string, request: string) {    
    return this.http.get<Position[]>(this.mainUrl+id+this.positionUrl+request);
  }

  getScoring() {
    //this.http.get<Score[]>(this.mainUrl+this.graphUrl);
    return this.http.get<Score[]>(this.mainUrl+this.scoreUrl);
  }

  getSuggestion(id: string) {
    return this.http.get<Book[]>(this.mainUrl+this.suggestionUrl+id);
  }

  postBooksByTokenRegEx(regEx: string) {
    return this.http.post<Book[]>(this.mainUrl+this.regexUrl,regEx);
  }

  postNumberOfClick(book: Book) {
    return this.http.post(this.mainUrl+this.upvoteUrl,book);
  }

}