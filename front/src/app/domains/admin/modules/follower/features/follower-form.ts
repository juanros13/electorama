import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { GeographyService } from '@/app/core/geography/geography.service';
import { UserService } from '@/app/core/user/user.service';
import {
  closeLoading,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { getBirthdateFromCurp, getSexFromCurp } from '@/app/shared/utils/curp';
import { UppercaseDirective } from '@/app/shared/directives/uppercase';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { PromotersService } from '@/app/domains/admin/modules/promoters/data/promoters.service';
import { SectionByUserPromoter } from '@/app/domains/admin/modules/promoters/data/promoters.types';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { FollowerService } from '../data/follower.service';

interface Cat {
  id: number;
  nombre: string;
}
interface PromoterOption {
  promoterAuthId: string;
  names?: string;
  lastName?: string;
  surName?: string;
}

const CURP_PATTERN =
  '\\b[A-Z][AEIOUX][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]\\b';

@Component({
  selector: 'app-follower-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './follower-form.html',
  imports: [
    ReactiveFormsModule,
    UppercaseDirective,
    PageHeader,
    SectionCard,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
})
export default class FollowerForm {
  private fb = inject(FormBuilder);
  private followers = inject(FollowerService);
  private promoters = inject(PromotersService);
  private geography = inject(GeographyService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected readonly maxYear = new Date().getFullYear() + 10;
  protected readonly sexOptions = ['HOMBRE', 'MUJER'];

  protected sections = signal<SectionByUserPromoter[]>([]);
  protected promotersList = signal<PromoterOption[]>([]);
  protected maritalStatus = signal<Cat[]>([]);
  protected familyRole = signal<Cat[]>([]);
  protected municipalities = signal<Municipality[]>([]);
  protected saving = signal(false);

  private entityId = 0;

  protected form = this.fb.group({
    section: this.fb.control<SectionByUserPromoter | null>(null, Validators.required),
    promoter: this.fb.control<PromoterOption | null>(null, Validators.required),
    names: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    surName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    curp: ['', [Validators.required, Validators.pattern(CURP_PATTERN)]],
    ine: ['', Validators.required],
    validity: ['', [Validators.required, Validators.min(2018), Validators.max(this.maxYear)]],
    sex: ['', Validators.required],
    birthdate: ['', Validators.required],
    maritalStatus: this.fb.control<Cat | null>(null, Validators.required),
    familyRole: this.fb.control<Cat | null>(null, Validators.required),
    email: [''],
    cellPhoneNumber: ['', Validators.required],
    supportWhatsapp: ['SI', Validators.required],
    otherCellPhone: [''],
    street: ['', Validators.required],
    numberExt: ['', Validators.required],
    numberInter: [''],
    postalCode: [''],
    municipalityId: this.fb.control<number | null>(null, Validators.required),
    cologne: [''],
  });

  constructor() {
    this.loadCatalogs();
    // Auto-completar sexo y fecha de nacimiento desde la CURP.
    this.form.controls.curp.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((curp) => {
        const value = (curp ?? '').toUpperCase();
        if (value.length === 18) {
          const sex = getSexFromCurp(value);
          const birthdate = getBirthdateFromCurp(value);
          if (sex) {
            this.form.controls.sex.setValue(sex);
          }
          if (birthdate) {
            this.form.controls.birthdate.setValue(birthdate);
          }
        }
      });
  }

  compareById(a: { id: number } | null, b: { id: number } | null): boolean {
    return a?.id === b?.id;
  }

  onSectionChange(section: SectionByUserPromoter): void {
    this.form.patchValue({ promoter: null });
    this.promotersList.set([]);
    this.promoters
      .getAllPromotores(section.seccionGeoId)
      .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => this.promotersList.set(res ?? []));
  }

  cancel(): void {
    this.router.navigate(['/admin/follower']);
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();

    const follower = {
      street: v.street, externalNumber: v.numberExt, internalNumber: v.numberInter,
      zipCode: v.postalCode, neighborhood: v.cologne,
      sectionNumber: v.section!.seccionNumber,
      residenceEntityId: this.entityId,
      residenceMunicipalityId: v.municipalityId,
      promoterAuthId: v.promoter!.promoterAuthId,
      isAdult: true,
      names: v.names, lastName: v.lastName, surName: v.surName,
      curp: v.curp, ine: v.ine, validity: parseInt(v.validity ?? '0', 10),
      cellPhoneNumber: v.cellPhoneNumber,
      supportWhatsapp: v.supportWhatsapp,
      cellPhoneNumberOther: v.otherCellPhone,
      email: v.email,
      supportsMovement: 'SI',
      committeeParticipation: 'NO',
      pollinStationRepresentativeParticipation: 'NO',
      invitingOthersToParticipate: 'NO',
      numberOfPeopleInvited: 'uno',
      catSex: { id: v.sex === 'HOMBRE' ? 1 : 2, nombre: v.sex },
      birthdate: v.birthdate,
      birthEntityId: this.entityId,
      catMaritalStatus: { id: v.maritalStatus!.id, nombre: v.maritalStatus!.nombre },
      catFamilyRole: { id: v.familyRole!.id, nombre: v.familyRole!.nombre },
      status: 'ACTIVO',
      userCreateId: this.userService.current?.id,
      deleted: null,
      isInSync: false,
      latitude: '',
      longitude: '',
      followerSirenaId: null,
    };

    this.saving.set(true);
    showLoading('Registrando simpatizante…');
    this.followers.createFollower(follower).subscribe({
      next: () => {
        closeLoading();
        showSuccess('¡Registro exitoso!', 'El simpatizante se registró correctamente.');
        this.router.navigate(['/admin/follower']);
      },
      error: () => {
        this.saving.set(false);
        closeLoading();
        showError();
      },
    });
  }

  private loadCatalogs(): void {
    const userId = this.userService.current?.id;
    if (userId) {
      this.promoters
        .getSectionsByUser(userId)
        .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
        .subscribe((s) =>
          this.sections.set((s ?? []).filter((x) => x.deleted == null))
        );
    }

    forkJoin([
      this.followers.getMaritalStatus(),
      this.followers.getFamilyRole(),
      this.geography.getAllEntities(),
      this.geography.getMunicipalitiesByStateId('27'),
    ])
      .pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        if (!res) {
          return;
        }
        const [marital, family, entities, municipios] = res as any[];
        this.maritalStatus.set(marital?.catMaritalStatus ?? []);
        this.familyRole.set(family?.catFamilyRole ?? []);
        const list: any[] = entities?.entities ?? entities ?? [];
        const tabasco = list.find((e: any) =>
          (e.nomEnt ?? '').toUpperCase().includes('TABASCO')
        );
        this.entityId = tabasco?.id ?? list[0]?.id ?? 0;
        this.municipalities.set([...(municipios ?? [])].sort((a, b) => a.id - b.id));
      });
  }
}
