export abstract class Util {

  /**
   * Performs download
   * @param filename of file
   * @param content file
   */
  static download(filename: string, content: string): void {
    const blob = new Blob([content], {type: 'application/xml'});
    const link = document.createElement('a');
    link.setAttribute('type', 'hidden');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    link.remove();
  }
}
