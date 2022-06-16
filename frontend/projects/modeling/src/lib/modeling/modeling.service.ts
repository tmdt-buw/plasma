import { Injectable } from '@angular/core';
import { DeltaModification, Relation } from '../api/generated/dms';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ModelingService {
  relation = new BehaviorSubject<Relation>(null);
  recommendation = new BehaviorSubject<DeltaModification>(null);
}
