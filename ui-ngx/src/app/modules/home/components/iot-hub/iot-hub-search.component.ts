///
/// Copyright © 2016-2026 The Thingsboard Authors
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

import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { forkJoin, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PageLink } from '@shared/models/page/page-link';
import { Direction, SortOrder } from '@shared/models/page/sort-order';
import { MpItemVersionQuery, MpItemVersionView } from '@shared/models/iot-hub/iot-hub-version.models';
import { ItemType, itemTypeTranslations } from '@shared/models/iot-hub/iot-hub-item.models';
import { IotHubInstalledItem } from '@shared/models/iot-hub/iot-hub-installed-item.models';
import { IotHubApiService } from '@core/http/iot-hub-api.service';
import { IotHubActionsService } from './iot-hub-actions.service';

interface SearchResultGroup {
  type: ItemType;
  items: MpItemVersionView[];
  /** Rows of this type behind the answer, from the response's typeTotal. */
  total: number;
  /** total - items.length, floored at 0. Zero means the header shows no "+N more". */
  remaining: number;
}

interface SortOption {
  value: string;
  label: string;
  direction: Direction;
}

/** Sort property served by relevance ranking. With an empty field the backend
 *  substitutes it with install count, so it is a safe default for the panel. */
const RELEVANCE = 'relevance';

const TYPE_ORDER: ItemType[] = [
  ItemType.DEVICE, ItemType.SOLUTION_TEMPLATE, ItemType.WIDGET,
  ItemType.CALCULATED_FIELD, ItemType.ALARM_RULE, ItemType.RULE_CHAIN
];

@Component({
  selector: 'tb-iot-hub-search',
  standalone: false,
  templateUrl: './iot-hub-search.component.html',
  styleUrls: ['./iot-hub-search.component.scss']
})
export class TbIotHubSearchComponent implements OnInit, OnDestroy {

  readonly ItemType = ItemType;

  @Input() searchText = '';
  @Input() creatorId: string;
  @Input() showCreator = true;
  @Output() searchTextChange = new EventEmitter<string>();

  get searchPlaceholderKey(): string {
    return this.creatorId ? 'iot-hub.search-published-items' : 'iot-hub.search';
  }

  resultGroups: SearchResultGroup[] = [];
  totalElements = 0;
  isLoading = false;
  hasError = false;
  private retryTimer: any = null;

  /** Not a user control any more: a grouped answer is one screen. The server ignores it on a
   *  grouped request, and it is only what a stale, ungrouped backend would page by. */
  pageSize = 15;

  sortOptions: SortOption[] = [
    { value: RELEVANCE, label: 'iot-hub.sort-most-relevant', direction: Direction.DESC },
    { value: 'totalInstallCount', label: 'iot-hub.sort-most-installed', direction: Direction.DESC },
    { value: 'publishedTime', label: 'iot-hub.sort-newest', direction: Direction.DESC },
    { value: 'name', label: 'iot-hub.sort-name', direction: Direction.ASC }
  ];
  selectedSortIndex = 0;

  /** Every surface this component serves is cross-type, so grouping is unconditional today.
   *  Named rather than inlined so a future single-type host turns it off in one place, without
   *  touching fetchResults. */
  readonly grouped = true;

  installedWidgets: IotHubInstalledItem[] = [];
  installedSolutionTemplates: IotHubInstalledItem[] = [];
  installedDeviceCounts: Record<string, number> = {};
  installedCalcFieldCounts: Record<string, number> = {};
  installedAlarmRuleCounts: Record<string, number> = {};
  installedRuleChainCounts: Record<string, number> = {};

  private searchSubject = new Subject<string>();
  private searchSubscription: Subscription;

  constructor(
    private router: Router,
    private translate: TranslateService,
    private iotHubApiService: IotHubApiService,
    private iotHubActions: IotHubActionsService
  ) {}

  ngOnInit(): void {
    this.loadInstalledItems();
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.loadResults();
    });
    this.loadResults();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  onSearchInput(): void {
    this.searchTextChange.emit(this.searchText);
    this.searchSubject.next(this.searchText || '');
  }

  clearSearch(): void {
    this.searchText = '';
    this.searchTextChange.emit(this.searchText);
    this.loadResults();
  }

  onSearchEnter(): void {
    this.loadResults();
  }

  onSortChange(index: number): void {
    this.selectedSortIndex = index;
    this.loadResults();
  }

  // Type helpers
  isCompactType(type: ItemType): boolean {
    return type === ItemType.CALCULATED_FIELD
      || type === ItemType.ALARM_RULE
      || type === ItemType.RULE_CHAIN;
  }

  getTypeLabel(type: ItemType): string {
    const key = itemTypeTranslations.get(type);
    return key ? this.translate.instant(key + '-plural') : type;
  }

  getTypeRoute(type: ItemType): string {
    switch (type) {
      case ItemType.WIDGET: return 'widgets';
      case ItemType.SOLUTION_TEMPLATE: return 'solution-templates';
      case ItemType.CALCULATED_FIELD: return 'calculated-fields';
      case ItemType.ALARM_RULE: return 'alarm-rules';
      case ItemType.RULE_CHAIN: return 'rule-chains';
      case ItemType.DEVICE: return 'devices';
      default: return 'widgets';
    }
  }

  navigateToType(type: ItemType): void {
    const search = this.searchText?.trim() || undefined;
    // creatorId must survive the jump: on a creator profile this header means "more of THIS
    // creator's widgets". Without it the user lands on the Hub-wide widgets page and the
    // profile's scope silently disappears.
    void this.router.navigate(['/iot-hub', this.getTypeRoute(type)],
      { queryParams: { search, creatorId: this.creatorId || undefined } });
  }

  // Installed items
  getInstalledItem(item: MpItemVersionView): IotHubInstalledItem | undefined {
    switch (item.type) {
      case ItemType.WIDGET:
        return this.installedWidgets.find(i => i.itemId === item.itemId);
      case ItemType.SOLUTION_TEMPLATE:
        return this.installedSolutionTemplates.find(i => i.itemId === item.itemId);
      default:
        return undefined;
    }
  }

  getInstalledItemsCount(item: MpItemVersionView): number {
    switch (item.type) {
      case ItemType.DEVICE:
        return this.installedDeviceCounts[item.itemId] || 0;
      case ItemType.CALCULATED_FIELD:
        return this.installedCalcFieldCounts[item.itemId] || 0;
      case ItemType.ALARM_RULE:
        return this.installedAlarmRuleCounts[item.itemId] || 0;
      case ItemType.RULE_CHAIN:
        return this.installedRuleChainCounts[item.itemId] || 0;
      default:
        return 0;
    }
  }

  // Dialogs
  openItemDetail(item: MpItemVersionView): void {
    this.iotHubActions.openItemDetail(item, this.getInstalledItem(item), this.getInstalledItemsCount(item), undefined, this.showCreator).subscribe(result => {
      if (result === 'installed' || result === 'deleted' || result === 'updated') {
        this.reloadInstalledItems();
      }
    });
  }

  installItem(item: MpItemVersionView): void {
    this.iotHubActions.installItem(item).subscribe(result => {
      if (result === 'installed') {
        this.reloadInstalledItems();
      }
    });
  }

  updateItem(item: MpItemVersionView): void {
    const installedItem = this.getInstalledItem(item);
    this.iotHubActions.updateItem(installedItem, item.version, item.id as string).subscribe(result => {
      if (result === 'updated') {
        this.reloadInstalledItems();
      }
    });
  }

  deleteInstalledItem(item: MpItemVersionView): void {
    const installedItem = this.getInstalledItem(item);
    this.iotHubActions.deleteItem(installedItem).subscribe((deleted) => {
      if (deleted) {
        this.reloadInstalledItems();
      }
    });
  }

  navigateToCreator(creatorId: string): void {
    void this.router.navigate(['/iot-hub/creator', creatorId]);
  }

  retryLoadResults(): void {
    if (this.retryTimer != null) {
      clearTimeout(this.retryTimer);
    }
    this.isLoading = true;
    this.retryTimer = setTimeout(() => {
      this.retryTimer = null;
      this.loadResults();
    }, 350);
  }

  // Data loading
  private loadResults(): void {
    if (this.retryTimer != null) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
    this.isLoading = true;
    // hasError stays as-is until the request actually succeeds
    // (cleared in the `next` callback below).
    this.fetchResults(this.searchText || '').subscribe({
      next: result => {
        this.applyResults(result.data, result.totalElements);
        this.hasError = false;
      },
      error: () => {
        this.isLoading = false;
        this.hasError = true;
        this.resultGroups = [];
        this.totalElements = 0;
      }
    });
  }

  private fetchResults(text: string) {
    const trimmed = text.trim();
    const sort = this.sortOptions[this.selectedSortIndex];
    const sortOrder: SortOrder = { property: sort.value, direction: sort.direction };
    // A grouped answer is one screen, so page index and page size stop being the user's
    // controls. PageLink still needs a page size, but on a grouped request the server IGNORES
    // it: the answer's shape is the mode's, not the caller's - the top rows of every type, one
    // screen, no second page. So pass a plain page size rather than inventing a constant for
    // it; a number here would be a second source of truth about the response's shape and a
    // thing to forget when a content type is added.
    const pageLink = new PageLink(this.pageSize, 0, trimmed || null, sortOrder);
    const query = new MpItemVersionQuery(pageLink, {
      creatorId: this.creatorId || undefined,
      grouped: this.grouped || undefined
    });
    return this.iotHubApiService.getPublishedVersions(query, { ignoreLoading: true, ignoreErrors: true });
  }

  private applyResults(data: MpItemVersionView[], totalElements: number): void {
    this.totalElements = totalElements;
    this.resultGroups = this.groupResults(data);
    this.isLoading = false;
  }

  private groupResults(items: MpItemVersionView[]): SearchResultGroup[] {
    const groupMap = new Map<ItemType, MpItemVersionView[]>();
    for (const item of items) {
      let list = groupMap.get(item.type);
      if (!list) {
        list = [];
        groupMap.set(item.type, list);
      }
      list.push(item);
    }
    return TYPE_ORDER
      .filter(type => groupMap.has(type))
      .map(type => {
        const groupItems = groupMap.get(type);
        // Every row of a type carries the same typeTotal. The fallback keeps a non-grouped
        // response rendering correctly - which is what a stale backend would send.
        const total = groupItems[0].typeTotal ?? groupItems.length;
        return { type, items: groupItems, total, remaining: Math.max(0, total - groupItems.length) };
      });
  }

  private loadInstalledItems(): void {
    const config = { ignoreLoading: true };
    const pageLink = new PageLink(10000, 0);
    forkJoin({
      widgets: this.iotHubApiService.getInstalledItems(pageLink, ItemType.WIDGET, undefined, config),
      solutionTemplates: this.iotHubApiService.getInstalledItems(pageLink, ItemType.SOLUTION_TEMPLATE, undefined, config),
      deviceCounts: this.iotHubApiService.getInstalledItemCounts(ItemType.DEVICE, config),
      calcFieldCounts: this.iotHubApiService.getInstalledItemCounts(ItemType.CALCULATED_FIELD, config),
      alarmRuleCounts: this.iotHubApiService.getInstalledItemCounts(ItemType.ALARM_RULE, config),
      ruleChainCounts: this.iotHubApiService.getInstalledItemCounts(ItemType.RULE_CHAIN, config)
    }).subscribe(results => {
      this.installedWidgets = results.widgets.data;
      this.installedSolutionTemplates = results.solutionTemplates.data;
      this.installedDeviceCounts = results.deviceCounts;
      this.installedCalcFieldCounts = results.calcFieldCounts;
      this.installedAlarmRuleCounts = results.alarmRuleCounts;
      this.installedRuleChainCounts = results.ruleChainCounts;
    });
  }

  private reloadInstalledItems(): void {
    this.loadInstalledItems();
  }
}
