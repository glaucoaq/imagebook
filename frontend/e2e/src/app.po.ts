import { browser, until, by, element } from 'protractor';
import * as path from 'path';

export class AppPage {
  navigateTo(): Promise<unknown> {
    return browser.get(browser.baseUrl) as Promise<unknown>;
  }

  getMessageText(): Promise<string> {
    return element(by.id('message')).getText() as Promise<string>;
  }

  getLoadingText(): Promise<string> {
    return element(by.className('loading')).getText() as Promise<string>;
  }

  hasLoadingText(): Promise<boolean> {
    return element(by.className('loading')).isPresent() as Promise<boolean>;
  }

  getEmptyListText(): Promise<string> {
    return element(by.className('empty')).getText() as Promise<string>;
  }

  hasEmptyListText(): Promise<boolean> {
    return element(by.className('empty')).isPresent() as Promise<boolean>;
  }

  getSearchResults(): Promise<string[]> {
    return browser.wait(until.elementsLocated(by.css('.image-card span')), 2000)
      .then(elements => Promise.all(elements.map(e => e.getText()))) as Promise<string[]>;
  }

  setImageFile(relativePath: string): Promise<void> {
    const absolutePath = path.resolve(process.cwd(), relativePath);
    return element(by.id('image')).sendKeys(absolutePath) as Promise<void>;
  }

  setDescriptionInput(description: string): Promise<void> {
    return element(by.id('description')).sendKeys(description) as Promise<void>;
  }

  submitForm(): Promise<void> {
    return element(by.id('upload')).click() as Promise<void>;
  }

  setSearchTextInput(description: string): Promise<void> {
    return element(by.id('searchText')).sendKeys(description) as Promise<void>;
  }

  submitSearch(): Promise<void> {
    return element(by.id('search')).click() as Promise<void>;
  }
}
