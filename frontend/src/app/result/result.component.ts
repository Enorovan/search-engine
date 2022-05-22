import { Component, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';

import { Book } from '../models/book.model';
import { Position } from '../models/position.model';
import { Score } from '../models/score.model';
import { BookService } from '../services/book.service';

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements OnInit, OnChanges {

  bookUrl: string = 'https://www.gutenberg.org/ebooks/';
  bookService: BookService;
  books: Book[] | null = null;
  positions: Position[][] | null = [];
  href: string = "";
  scores: Score[] | null = null;
  suggestions: Book[] | null = null;
  currentBookId: string = "";

  constructor(private router: Router, bookService: BookService) { this.bookService = bookService }

  
  upvote(book: Book) {
    this.bookService.postNumberOfClick(book).subscribe(data => {
      this.ngOnChanges();
    });
  }

  getSuggestions(id: string) {
    this.currentBookId = id;
    this.bookService.getSuggestion(id).subscribe(suggestions => {
      this.suggestions = suggestions;
    });
  }
  
  ngOnChanges() {
    this.href = this.router.url.replace("/search/","");
    

    switch(this.href) {
      case "french": {
        this.bookService.getBooks("fr").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "english": {
        this.bookService.getBooks("en").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "german": {
        this.bookService.getBooks("de").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "all": {
        this.bookService.getBooks("all").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      default: {
        this.href = this.router.url.replace("/search/result/",""); 
        if (this.href.includes('regex')) {
          this.href = this.router.url.replace("/search/result/regex/","");
          this.bookService.postBooksByTokenRegEx(this.href).subscribe(books => {
            this.books = books;
          });
        } else {
          this.bookService.getBooksByToken(this.href).subscribe(books => {
            this.books = books;
          });
          this.bookService.getScoring().subscribe(scores => {
            this.scores = scores;
          });
        } 
        break;
      }
    }
  }

  ngOnInit(): void {
    this.href = this.router.url.replace("/search/","");

    switch(this.href) {
      case "french": {
        this.bookService.getBooks("fr").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "english": {
        this.bookService.getBooks("en").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "german": {
        this.bookService.getBooks("de").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      case "all": {
        this.bookService.getBooks("all").subscribe(books => {
          this.books = books;
        });
        this.bookService.getScoring().subscribe(scores => {
          this.scores = scores;
        });
        break;
      }
      default: {
        this.href = this.router.url.replace("/search/result/","");
        if (this.href.includes('regex')) {
          this.href = this.router.url.replace("/search/result/regex/","");
          
          this.bookService.postBooksByTokenRegEx(this.href).subscribe(books => {
            this.books = books;
          });
        } else {
          this.bookService.getBooksByToken(this.href).subscribe(books => {
            this.books = books;
            this.books.forEach(book => {
              this.bookService.getPositionOfOccurence(book.id,this.href).subscribe(positions => {
                positions.forEach( position => {
                  position.id = book.id;
                });
                this.positions?.push(positions);
              });
            })
          });
          this.bookService.getScoring().subscribe(scores => {
            this.scores = scores;
          });
        }
        break;
      }
    }
  }
}
