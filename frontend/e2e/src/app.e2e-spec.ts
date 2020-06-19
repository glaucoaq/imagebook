import { AppPage } from './app.po';
import { browser, logging } from 'protractor';

describe('ImageBook Frontend', () => {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();
  });

  it('should display empty upload form', () => {
    page.navigateTo();
    expect(page.getMessageText()).toBe('');
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
