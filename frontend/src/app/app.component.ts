import { HttpClient, HttpEventType } from '@angular/common/http';
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  constructor(private httpClient: HttpClient) { }

  image: File;
  description: string ;
  message: string;
  imageUrl: string;

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

    this.httpClient
      .post<any>('http://localhost:8080/api/images', formPayload)
      .subscribe({
        next: data => this.imageUrl = data.imageUrl,
        error: ({ error }) => this.message = `Failed to upload the image. ${error?.message ?? ''}`,
        complete: () => this.message = 'Image uploaded successfully'
      });
  }
}
