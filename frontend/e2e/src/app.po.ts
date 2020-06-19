import { browser, by, element } from 'protractor';
import * as path from 'path';

export class AppPage {
  navigateTo(): Promise<unknown> {
    return browser.get(browser.baseUrl) as Promise<unknown>;
  }

  getMessageText(): Promise<string> {
    return element(by.id('message')).getText() as Promise<string>;
  }

  setImageFile(relativePath: string): Promise<void> {
    const absolutePath = path.resolve(process.cwd(), relativePath);
    return element(by.id('image')).sendKeys(absolutePath) as Promise<void>;
  }

  setDescriptionInput(description: string) {
    return element(by.id('description')).sendKeys(description) as Promise<void>;
  }

  submitForm() {
    return element(by.id('upload')).click() as Promise<void>;
  }
}
