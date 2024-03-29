/**
 * PLASMA DPS Service
 * Provides data processing capabilities to PLASMA
 *
 * The version of the OpenAPI document: 1.4.0
 * Contact: paulus@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional } from '@angular/core';
import {
  HttpClient,
  HttpContext,
  HttpEvent,
  HttpHeaders,
  HttpParameterCodec,
  HttpParams,
  HttpResponse
} from '@angular/common/http';
import { CustomHttpParameterCodec } from '../encoder';
import { Observable } from 'rxjs';

// @ts-ignore
import { CombinedModel } from '../model/combinedModel';
// @ts-ignore
import { SampleDTO } from '../model/sampleDTO';

// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS } from '../variables';
import { Configuration } from '../configuration';


@Injectable({
  providedIn: 'any'
})
export class DataProcessingControllerService {

  public defaultHeaders = new HttpHeaders();
  public configuration = new Configuration();
  protected basePath = 'http://plasma-gateway-service:8888';
  public encoder: HttpParameterCodec;

  constructor(protected httpClient: HttpClient, @Optional() @Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
    if (configuration) {
      this.configuration = configuration;
    }
    if (typeof this.configuration.basePath !== 'string') {
      if (typeof basePath !== 'string') {
        basePath = this.basePath;
      }
            this.configuration.basePath = basePath;
        }
        this.encoder = this.configuration.encoder || new CustomHttpParameterCodec();
    }

    /**
     * @param consumes string[] mime-types
     * @return true: consumes contains 'multipart/form-data', false: otherwise
     */
    private canConsumeForm(consumes: string[]): boolean {
      const form = 'multipart/form-data';
      for (const consume of consumes) {
        if (form === consume) {
          return true;
        }
      }
      return false;
    }

  /**
   * @param dataId
   * @param combinedModel
   * @param fileId
   * @param format
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public convertFile(dataId: string, combinedModel: CombinedModel, fileId?: string, format?: string, observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: '*/*', context?: HttpContext }): Observable<string>;

  public convertFile(dataId: string, combinedModel: CombinedModel, fileId?: string, format?: string, observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: '*/*', context?: HttpContext }): Observable<HttpResponse<string>>;

  public convertFile(dataId: string, combinedModel: CombinedModel, fileId?: string, format?: string, observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: '*/*', context?: HttpContext }): Observable<HttpEvent<string>>;

  public convertFile(dataId: string, combinedModel: CombinedModel, fileId?: string, format?: string, observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: '*/*', context?: HttpContext }): Observable<any> {
    if (dataId === null || dataId === undefined) {
      throw new Error('Required parameter dataId was null or undefined when calling convertFile.');
    }
    if (combinedModel === null || combinedModel === undefined) {
      throw new Error('Required parameter combinedModel was null or undefined when calling convertFile.');
    }

    let localVarQueryParameters = new HttpParams({encoder: this.encoder});
    if (dataId !== undefined && dataId !== null) {
      localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
        <any> dataId, 'dataId');
    }
    if (fileId !== undefined && fileId !== null) {
      localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
        <any> fileId, 'fileId');
    }
    if (format !== undefined && format !== null) {
      localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
        <any> format, 'format');
    }

        let localVarHeaders = this.defaultHeaders;

        let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
        if (localVarHttpHeaderAcceptSelected === undefined) {
            // to determine the Accept header
            const httpHeaderAccepts: string[] = [
              '*/*'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }


        // to determine the Content-Type header
        const consumes: string[] = [
            'application/json'
        ];
        const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
        if (httpContentTypeSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Content-Type', httpContentTypeSelected);
        }

        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        return this.httpClient.post<string>(`${this.configuration.basePath}/api/plasma-dps/convert`,
            combinedModel,
          {
            context: localVarHttpContext,
            params: localVarQueryParameters,
            responseType: <any> responseType_,
            withCredentials: this.configuration.withCredentials,
            headers: localVarHeaders,
            observe: observe,
            reportProgress: reportProgress
          }
        );
  }

  /**
   * Check if service available.
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public isAvailable(observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: 'text/plain', context?: HttpContext }): Observable<string>;

  public isAvailable(observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: 'text/plain', context?: HttpContext }): Observable<HttpResponse<string>>;

  public isAvailable(observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: 'text/plain', context?: HttpContext }): Observable<HttpEvent<string>>;

  public isAvailable(observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: 'text/plain', context?: HttpContext }): Observable<any> {

    let localVarHeaders = this.defaultHeaders;

    let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
    if (localVarHttpHeaderAcceptSelected === undefined) {
      // to determine the Accept header
      const httpHeaderAccepts: string[] = [
        'text/plain'
      ];
      localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }


        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        return this.httpClient.get<string>(`${this.configuration.basePath}/api/plasma-dps/available`,
          {
            context: localVarHttpContext,
            responseType: <any> responseType_,
            withCredentials: this.configuration.withCredentials,
            headers: localVarHeaders,
            observe: observe,
            reportProgress: reportProgress
          }
        );
  }

  /**
   * List all files of a dataId.
   * @param dataId
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public listFiles(dataId: string, observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<Array<string>>;

  public listFiles(dataId: string, observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<HttpResponse<Array<string>>>;

  public listFiles(dataId: string, observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<HttpEvent<Array<string>>>;

  public listFiles(dataId: string, observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<any> {
    if (dataId === null || dataId === undefined) {
      throw new Error('Required parameter dataId was null or undefined when calling listFiles.');
    }

    let localVarHeaders = this.defaultHeaders;

    let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
    if (localVarHttpHeaderAcceptSelected === undefined) {
      // to determine the Accept header
      const httpHeaderAccepts: string[] = [
        'application/json'
      ];
      localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }


        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        return this.httpClient.get<Array<string>>(`${this.configuration.basePath}/api/plasma-dps/files/${encodeURIComponent(String(dataId))}`,
          {
            context: localVarHttpContext,
            responseType: <any> responseType_,
            withCredentials: this.configuration.withCredentials,
            headers: localVarHeaders,
            observe: observe,
            reportProgress: reportProgress
          }
        );
  }

  /**
   * Initializes a new modeling.
   * @param file
   * @param dataId
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public uploadFile(file: Blob, dataId?: string, observe?: 'body', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<SampleDTO>;

  public uploadFile(file: Blob, dataId?: string, observe?: 'response', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<HttpResponse<SampleDTO>>;

  public uploadFile(file: Blob, dataId?: string, observe?: 'events', reportProgress?: boolean, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<HttpEvent<SampleDTO>>;

  public uploadFile(file: Blob, dataId?: string, observe: any = 'body', reportProgress: boolean = false, options?: { httpHeaderAccept?: 'application/json', context?: HttpContext }): Observable<any> {
    if (file === null || file === undefined) {
      throw new Error('Required parameter file was null or undefined when calling uploadFile.');
    }

    let localVarQueryParameters = new HttpParams({encoder: this.encoder});
    if (dataId !== undefined && dataId !== null) {
      localVarQueryParameters = this.addToHttpParams(localVarQueryParameters,
        <any> dataId, 'dataId');
    }

    let localVarHeaders = this.defaultHeaders;

    let localVarHttpHeaderAcceptSelected: string | undefined = options && options.httpHeaderAccept;
    if (localVarHttpHeaderAcceptSelected === undefined) {
      // to determine the Accept header
      const httpHeaderAccepts: string[] = [
        'application/json'
            ];
            localVarHttpHeaderAcceptSelected = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        }
        if (localVarHttpHeaderAcceptSelected !== undefined) {
            localVarHeaders = localVarHeaders.set('Accept', localVarHttpHeaderAcceptSelected);
        }

        let localVarHttpContext: HttpContext | undefined = options && options.context;
        if (localVarHttpContext === undefined) {
            localVarHttpContext = new HttpContext();
        }

        // to determine the Content-Type header
        const consumes: string[] = [
            'multipart/form-data'
        ];

        const canConsumeForm = this.canConsumeForm(consumes);

        let localVarFormParams: { append(param: string, value: any): any; };
        let localVarUseForm = false;
        let localVarConvertFormParamsToString = false;
        // use FormData to transmit files using content-type "multipart/form-data"
        // see https://stackoverflow.com/questions/4007969/application-x-www-form-urlencoded-or-multipart-form-data
        localVarUseForm = canConsumeForm;
        if (localVarUseForm) {
            localVarFormParams = new FormData();
        } else {
            localVarFormParams = new HttpParams({encoder: this.encoder});
        }

        if (file !== undefined) {
            localVarFormParams = localVarFormParams.append('file', <any>file) as any || localVarFormParams;
        }

        let responseType_: 'text' | 'json' | 'blob' = 'json';
        if (localVarHttpHeaderAcceptSelected) {
            if (localVarHttpHeaderAcceptSelected.startsWith('text')) {
                responseType_ = 'text';
            } else if (this.configuration.isJsonMime(localVarHttpHeaderAcceptSelected)) {
                responseType_ = 'json';
            } else {
                responseType_ = 'blob';
            }
        }

        return this.httpClient.post<SampleDTO>(`${this.configuration.basePath}/api/plasma-dps/files/upload`,
            localVarConvertFormParamsToString ? localVarFormParams.toString() : localVarFormParams,
            {
                context: localVarHttpContext,
              params: localVarQueryParameters,
              responseType: <any> responseType_,
              withCredentials: this.configuration.withCredentials,
              headers: localVarHeaders,
              observe: observe,
              reportProgress: reportProgress
            }
        );
  }

  // @ts-ignore
  private addToHttpParams(httpParams: HttpParams, value: any, key?: string): HttpParams {
    if (typeof value === 'object' && value instanceof Date === false) {
      httpParams = this.addToHttpParamsRecursive(httpParams, value);
    } else {
      httpParams = this.addToHttpParamsRecursive(httpParams, value, key);
    }
    return httpParams;
  }

  private addToHttpParamsRecursive(httpParams: HttpParams, value?: any, key?: string): HttpParams {
    if (value == null) {
      return httpParams;
    }

    if (typeof value === 'object') {
      if (Array.isArray(value)) {
        (value as any[]).forEach(elem => httpParams = this.addToHttpParamsRecursive(httpParams, elem, key));
      } else if (value instanceof Date) {
        if (key != null) {
          httpParams = httpParams.append(key, (value as Date).toISOString().substr(0, 10));
        } else {
          throw Error('key may not be null if value is Date');
        }
      } else {
        Object.keys(value).forEach(k => httpParams = this.addToHttpParamsRecursive(
          httpParams, value[k], key != null ? `${key}.${k}` : k));
      }
    } else if (key != null) {
      httpParams = httpParams.append(key, value);
    } else {
      throw Error('key may not be null if value is not object or array');
    }
    return httpParams;
  }

}
