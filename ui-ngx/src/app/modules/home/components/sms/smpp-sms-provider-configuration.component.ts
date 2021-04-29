///
/// Copyright © 2016-2021 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppState } from '@app/core/core.state';
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { isDefinedAndNotNull } from '@core/utils';
import {
  SmppSmsProviderConfiguration,
  SmsProviderConfiguration,
  SmsProviderType, smtpPortPattern
} from '@shared/models/settings.models';

@Component({
  selector: 'tb-smpp-sms-provider-configuration',
  templateUrl: './smpp-sms-provider-configuration.component.html',
  styleUrls: [],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SmppSmsProviderConfigurationComponent),
    multi: true
  }]
})
export class SmppSmsProviderConfigurationComponent implements ControlValueAccessor, OnInit {

  smppSmsProviderConfigurationFormGroup: FormGroup;

  private requiredValue: boolean;

  get required(): boolean {
    return this.requiredValue;
  }

  @Input()
  set required(value: boolean) {
    this.requiredValue = coerceBooleanProperty(value);
  }

  @Input()
  disabled: boolean;

  private propagateChange = (v: any) => {};

  constructor(private store: Store<AppState>,
              private fb: FormBuilder) {
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  ngOnInit() {
    this.smppSmsProviderConfigurationFormGroup = this.fb.group({
      smppHost: ['localhost', [Validators.required]],
      // smppPort: ['2775', [Validators.required, Validators.pattern(smtpPortPattern),
      //   Validators.maxLength(5)]],
      smppPort: ['2775', [Validators.required, Validators.maxLength(5)]],
      username: [null, [Validators.required]],
      password: [null, [Validators.required]]
    });
    this.smppSmsProviderConfigurationFormGroup.valueChanges.subscribe(() => {
      this.updateModel();
    });
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (this.disabled) {
      this.smppSmsProviderConfigurationFormGroup.disable({emitEvent: false});
    } else {
      this.smppSmsProviderConfigurationFormGroup.enable({emitEvent: false});
    }
  }

  writeValue(value: SmppSmsProviderConfiguration | null): void {
    if (isDefinedAndNotNull(value)) {
      this.smppSmsProviderConfigurationFormGroup.patchValue(value, {emitEvent: false});
    }
  }

  private updateModel() {
    let configuration: SmppSmsProviderConfiguration = null;
    if (this.smppSmsProviderConfigurationFormGroup.valid) {
      configuration = this.smppSmsProviderConfigurationFormGroup.value;
      (configuration as SmsProviderConfiguration).type = SmsProviderType.SMPP;
    }
    this.propagateChange(configuration);
  }
}
