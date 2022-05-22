export interface Book {
    id: string;
    title: string;
    author: string;
    urlImage: string;
    keywords: string[];
    languages: string[];
    token: [
        {
            token: string,
            occurrences: number,
            frequencies: number
        }
    ]
    nbClick: number
}