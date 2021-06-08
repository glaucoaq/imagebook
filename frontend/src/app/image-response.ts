import { Image } from './image';

export class ImageResponse {

  constructor(
    public content: Image[],
    public number: number,
    public last: boolean) {}

  static combine(prev: ImageResponse, next: ImageResponse): ImageResponse {
    next.content = prev.content.concat(next.content);
    return next;
  }
}
