import { AppPage } from './app.po';
import { browser, logging } from 'protractor';
import { environment } from '../../src/environments/environment'

describe('ImageBook Frontend', () => {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();
  });

  it('should display existing records on page load', () => {
    page.navigateTo();
    expect(page.getMessageText()).toBe('');
    expect(page.getSearchResults()
      .then(results => results.length))
      .toBe(environment.imagesPerPage)
      .then(() => expect(page.hasEmptyListText())
      .toBe(false));
    expect(page.getLoadingText()).toBe('Loading more images...');
  });

  it('should display single result for given description', () => {
    page.navigateTo();
    expect(page.setSearchTextInput('file description')
      .then(() => page.submitSearch())
      .then(() => page.getSearchResults()))
      .toEqual(['file description'])
      .then(() => expect(page.hasLoadingText())
      .toBe(false));
  });

  it('should upload image with success', () => {
    page.navigateTo();
    expect(page.setImageFile('../web/src/test/resources/image/blank.png')
      .then(() => page.setDescriptionInput('Engage'))
      .then(() => page.submitForm())
      .then(() => page.getMessageText()))
      .toContain('Image uploaded successfully');
  });

  it('should upload without success due to missing description', () => {
    page.navigateTo();
    expect(page.setImageFile('../web/src/test/resources/image/blank.png')
      .then(() => page.submitForm())
      .then(() => page.getMessageText()))
      .toContain('Failed to upload the image.');
  });
});
