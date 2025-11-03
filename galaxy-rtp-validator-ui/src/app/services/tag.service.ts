import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { XmlTag, TagStatistics, TagSelectionRequest, XmlGenerationResponse, XmlGenerationResult } from '../models/tag.model';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private apiUrl = 'http://localhost:8080/api/tags';

  constructor(private http: HttpClient) { }

  getAllTags(): Observable<XmlTag[]> {
    return this.http.get<XmlTag[]>(this.apiUrl);
  }

  searchTags(query: string): Observable<XmlTag[]> {
    return this.http.get<XmlTag[]>(`${this.apiUrl}/search`, {
      params: { query }
    });
  }

  getStatistics(): Observable<TagStatistics> {
    return this.http.get<TagStatistics>(`${this.apiUrl}/statistics`);
  }

  updateTagSelection(request: TagSelectionRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/selection`, request);
  }

  generateXml(request: TagSelectionRequest): Observable<XmlGenerationResponse> {
    return this.http.post<XmlGenerationResponse>(`${this.apiUrl}/generate-xml`, request);
  }

  generateXmlCombinations(request: TagSelectionRequest): Observable<XmlGenerationResult> {
    return this.http.post<XmlGenerationResult>(`${this.apiUrl}/generate-xml-combinations`, request);
  }

  generateExcelReport(request: TagSelectionRequest): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/generate-excel-report`, request, {
      responseType: 'blob'
    });
  }
}

