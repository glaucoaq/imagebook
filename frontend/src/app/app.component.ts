import { HttpClient, HttpEventType, HttpParams } from '@angular/common/http';
import { Component, ViewChild, ElementRef } from '@angular/core';

import { environment } from '../environments/environment'
import { Image } from './image';
import { ImageResponse } from './image-response';

import { delay } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  constructor(private httpClient: HttpClient) { }

  @ViewChild('inputFile') inputFile: ElementRef;

  image: File;
  description: string;
  searchText: string;
  imageType: string;
  minSize: number;
  maxSize: number;

  message: string;
  response: ImageResponse;

  ngAfterViewInit() {
    this.onSearch(false);
  }

  onImageFileChange(files: FileList) {
    this.image = files[0];
  }

  onSubmit() {
    console.group("Submit");
    console.log("File: ", this.image.name);
    console.log("Description: ", this.description);
    console.groupEnd();
    const formPayload = new FormData();
    if (!!this.image) formPayload.append('image', this.image, this.image.name);
    if (!!this.description) formPayload.append('description', this.description);

    this.message = '';
    this.httpClient
      .post<Image>(environment.imagesEndpointUrl, formPayload)
      .subscribe({
        next: data => this.response = new ImageResponse([ data ], 0, true),
        error: ({ error }) => this.message = `Failed to upload the image. ${error?.message ?? ''}`,
        complete: () => {
          this.inputFile.nativeElement.value = this.image = this.description = null;
          this.message = 'Image uploaded successfully';
        }
      });
  }

  onSearch(append: boolean) {
    if (append && this.response.last) {
      return;
    }

    if (!append) {
      this.response = null;
    }

    const page = (append && this.response) ? this.response.number + 1 : 0;
    var params = new HttpParams().set("page", String(page));
    params = this.searchText ? params.set("description", this.searchText) : params;
    params = this.imageType ? params.set("image-type", this.imageType) : params;
    params = this.maxSize ? params.set("max-size", String(this.maxSize)) : params;
    params = this.minSize ? params.set("min-size", String(this.minSize)) : params;

    this.httpClient
      .get<ImageResponse>(environment.imagesEndpointUrl, { params })
      .pipe(delay(append ? 1500 : 500)) // to give us a chance to see the loading message
      .subscribe({
        next: data => this.response = append
          ? ImageResponse.combine(this.response, data)
          : data,
        error: ({ error }) => this.message = `Failed to retrieve images. ${error?.message ?? ''}`,
      });
  }
}
