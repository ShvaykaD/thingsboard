///
/// Copyright © 2016-2023 The Thingsboard Authors
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

export interface Unit {
  name: string;
  symbol: string;
  tags: string[];
}

export const units: Array<Unit> = [
  {
    name: 'unit.millimeter',
    symbol: 'mm',
    tags: ['level','height','distance','length','width','gap','depth','millimeter','millimeters','rainfall','precipitation',
      'displacement','position','movement','transition','mm']
  },
  {
    name: 'unit.centimeter',
    symbol: 'cm',
    tags: ['level','height','distance','length','width','gap','depth','centimeter','centimeters','rainfall','precipitation',
      'displacement','position','movement','transition','cm']
  },
  {
    name: 'unit.angstrom',
    symbol: 'Å',
    tags: ['level','height','distance','length','width','gap','depth','atomic scale','atomic distance','nanoscale',
      'angstrom','angstroms','Å']
  },
  {
    name: 'unit.nanometer',
    symbol: 'nm',
    tags: ['level','height','distance','length','width','gap','depth','nanoscale','atomic scale','molecular scale',
      'nanometer','nanometers','nm']
  },
  {
    name: 'unit.micrometer',
    symbol: 'µm',
    tags: ['level','height','distance','length','width','gap','depth','microns','micrometer','micrometers','µm']
  },
  {
    name: 'unit.meter',
    symbol: 'm',
    tags: ['level','height','distance','length','width','gap','depth','meter','meters','m']
  },
  {
    name: 'unit.kilometer',
    symbol: 'km',
    tags: ['distance','height','length','width','gap','depth','kilometer','kilometers','km']
  },
  {
    name: 'unit.inch',
    symbol: 'in',
    tags: ['level','height','distance','length','width','gap','depth','inch','inches','in']
  },
  {
    name: 'unit.foot',
    symbol: 'ft',
    tags: ['level','height','distance','length','width','gap','depth','foot','feet','ft']
  },
  {
    name: 'unit.yard',
    symbol: 'yd',
    tags: ['level','height','distance','length','width','gap','depth','yard','yards','yd']
  },
  {
    name: 'unit.mile',
    symbol: 'mi',
    tags: ['level','height','distance','length','width','gap','depth','mile','miles','mi']
  },
  {
    name: 'unit.nautical-mile',
    symbol: 'nm',
    tags: ['level','height','distance','length','width','gap','depth','nautical mile','nm']
  },
  {
    name: 'unit.astronomical-unit',
    symbol: 'AU',
    tags: ['distance','celestial bodies','solar system','AU']
  },
  {
    name: 'unit.reciprocal-metre',
    symbol: 'm⁻¹',
    tags: ['wavenumber','wave density','wave frequency','m⁻¹']
  },
  {
    name: 'unit.meter-per-meter',
    symbol: 'm/m',
    tags: ['ratio of length to length','meter per meter','m/m']
  },
  {
    name: 'unit.steradian',
    symbol: 'sr',
    tags: ['solid angle','spatial extent','steradian','sr']
  },
  {
    name: 'unit.thou',
    symbol: 'thou',
    tags: ['length','measurement','thou']
  },
  {
    name: 'unit.barleycorn',
    symbol: 'barleycorn',
    tags: ['length','shoe size','barleycorn']
  },
  {
    name: 'unit.hand',
    symbol: 'hand',
    tags: ['length','horse measurement','hand']
  },
  {
    name: 'unit.chain',
    symbol: 'ch',
    tags: ['length','land surveying','ch']
  },
  {
    name: 'unit.furlong',
    symbol: 'fur',
    tags: ['length','land surveying','fur']
  },
  {
    name: 'unit.league',
    symbol: 'league',
    tags: ['length','historical measurement','league']
  },
  {
    name: 'unit.fathom',
    symbol: 'fathom',
    tags: ['depth','nautical measurement','fathom']
  },
  {
    name: 'unit.cable',
    symbol: 'cable',
    tags: ['distance','nautical measurement','cable']
  },
  {
    name: 'unit.link',
    symbol: 'link',
    tags: ['length','land surveying','link']
  },
  {
    name: 'unit.rod',
    symbol: 'rod',
    tags: ['length','land surveying','rod']
  },
  {
    name: 'unit.nanogram',
    symbol: 'ng',
    tags: ['mass','weight','heaviness','load','nanogram','nanograms','ng']
  },
  {
    name: 'unit.microgram',
    symbol: 'μg',
    tags: ['mass','weight','heaviness','load','μg','microgram']
  },
  {
    name: 'unit.milligram',
    symbol: 'mg',
    tags: ['mass','weight','heaviness','load','milligram','miligrams','mg']
  },
  {
    name: 'unit.gram',
    symbol: 'g',
    tags: ['mass','weight','heaviness','load','gram','grams','g']
  },
  {
    name: 'unit.kilogram',
    symbol: 'kg',
    tags: ['mass','weight','heaviness','load','kilogram','kilograms','kg']
  },
  {
    name: 'unit.tonne',
    symbol: 't',
    tags: ['mass','weight','heaviness','load','tonne','tons','t']
  },
  {
    name: 'unit.ounce',
    symbol: 'oz',
    tags: ['mass','weight','heaviness','load','ounce','ounces','oz']
  },
  {
    name: 'unit.pound',
    symbol: 'lb',
    tags: ['mass','weight','heaviness','load','pound','pounds','lb']
  },
  {
    name: 'unit.stone',
    symbol: 'st',
    tags: ['mass','weight','heaviness','load','stone','stones','st']
  },
  {
    name: 'unit.hundredweight-count',
    symbol: 'cwt',
    tags: ['mass','weight','heaviness','load','hundredweight count','cwt']
  },
  {
    name: 'unit.short-tons',
    symbol: 'short tons',
    tags: ['mass','weight','heaviness','load','short ton','short tons']
  },
  {
    name: 'unit.dalton',
    symbol: 'Da',
    tags: ['atomic mass unit','AMU','unified atomic mass unit','dalton','Da']
  },
  {
    name: 'unit.grain',
    symbol: 'gr',
    tags: ['mass','measurement','grain','gr']
  },
  {
    name: 'unit.drachm',
    symbol: 'dr',
    tags: ['mass','measurement','drachm','dr']
  },
  {
    name: 'unit.quarter',
    symbol: 'qr',
    tags: ['mass','measurement','quarter','qr']
  },
  {
    name: 'unit.slug',
    symbol: 'slug',
    tags: ['mass','measurement','slug']
  },
  {
    name: 'unit.carat',
    symbol: 'ct',
    tags: ['gemstone','pearl','jewelry','carat','ct']
  },
  {
    name: 'unit.cubic-millimeter',
    symbol: 'mm³',
    tags: ['volume','capacity','extent','cubic millimeter','mm³']
  },
  {
    name: 'unit.cubic-centimeter',
    symbol: 'cm³',
    tags: ['volume','capacity','extent','cubic centimeter','cubic centimeters','cm³']
  },
  {
    name: 'unit.cubic-meter',
    symbol: 'm³',
    tags: ['volume','capacity','extent','cubic meter','cubic meters','m³']
  },
  {
    name: 'unit.cubic-kilometer',
    symbol: 'km³',
    tags: ['volume','capacity','extent','cubic kilometer','cubic kilometers','km³']
  },
  {
    name: 'unit.microliter',
    symbol: 'µL',
    tags: ['volume','liquid measurement','microliter','µL']
  },
  {
    name: 'unit.milliliter',
    symbol: 'mL',
    tags: ['volume','capacity','extent','milliliter','milliliters','mL']
  },
  {
    name: 'unit.liter',
    symbol: 'l',
    tags: ['volume','capacity','extent','liter','liters','l']
  },
  {
    name: 'unit.hectoliter',
    symbol: 'hl',
    tags: ['volume','capacity','extent','hectoliter','hectoliters','hl']
  },
  {
    name: 'unit.cubic-inch',
    symbol: 'in³',
    tags: ['volume','capacity','extent','cubic inch','cubic inches','in³']
  },
  {
    name: 'unit.cubic-foot',
    symbol: 'ft³',
    tags: ['volume','capacity','extent','cubic foot','cubic feet','ft³']
  },
  {
    name: 'unit.cubic-yard',
    symbol: 'yd³',
    tags: ['volume','capacity','extent','cubic yard','cubic yards','yd³']
  },
  {
    name: 'unit.fluid-ounce',
    symbol: 'fl-oz',
    tags: ['volume','capacity','extent','fluid ounce','fluid ounces','fl-oz']
  },
  {
    name: 'unit.pint',
    symbol: 'pt',
    tags: ['volume','capacity','extent','pint','pints','pt']
  },
  {
    name: 'unit.quart',
    symbol: 'qt',
    tags: ['volume','capacity','extent','quart','quarts','qt']
  },
  {
    name: 'unit.gallon',
    symbol: 'gal',
    tags: ['volume','capacity','extent','gallon','gallons','gal']
  },
  {
    name: 'unit.oil-barrels',
    symbol: 'bbl',
    tags: ['volume','capacity','extent','oil barrel','oil barrels','bbl']
  },
  {
    name: 'unit.cubic-meter-per-kilogram',
    symbol: 'm³/kg',
    tags: ['specific volume','volume per unit mass','cubic meter per kilogram','m³/kg']
  },
  {
    name: 'unit.gill',
    symbol: 'gi',
    tags: ['volume','liquid measurement','gi']
  },
  {
    name: 'unit.hogshead',
    symbol: 'hhd',
    tags: ['volume','liquid measurement','hhd']
  },
  {
    name: 'unit.teaspoon',
    symbol: 'tsp',
    tags: ['volume','cooking measurement','tsp']
  },
  {
    name: 'unit.tablespoon',
    symbol: 'tbsp',
    tags: ['volume','cooking measurement','tbsp']
  },
  {
    name: 'unit.cup',
    symbol: 'cup',
    tags: ['volume','cooking measurement','cup']
  },
  {
    name: 'unit.celsius',
    symbol: '°C',
    tags: ['temperature','heat','cold','warmth','degrees','celsius','shipment condition','°C']
  },
  {
    name: 'unit.kelvin',
    symbol: 'K',
    tags: ['temperature','heat','cold','warmth','degrees','kelvin','K','color quality','white balance','color temperature']
  },
  {
    name: 'unit.rankine',
    symbol: '°R',
    tags: ['temperature','heat','cold','warmth','Rankine','°R']
  },
  {
    name: 'unit.fahrenheit',
    symbol: '°F',
    tags: ['temperature','heat','cold','warmth','degrees','fahrenheit','°F']
  },
  {
    name: 'unit.meter-per-second',
    symbol: 'm/s',
    tags: ['speed','velocity','pace','meter per second','m/s','peak','peak to peak','root mean square (RMS)',
      'vibration','wind speed','weather']
  },
  {
    name: 'unit.kilometer-per-hour',
    symbol: 'km/h',
    tags: ['speed','velocity','pace','kilometer per hour','km/h']
  },
  {
    name: 'unit.foot-per-second',
    symbol: 'ft/s',
    tags: ['speed','velocity','pace','foot per second','ft/s']
  },
  {
    name: 'unit.mile-per-hour',
    symbol: 'mph',
    tags: ['speed','velocity','pace','mile per hour','mph']
  },
  {
    name: 'unit.knot',
    symbol: 'kt',
    tags: ['speed','velocity','pace','knot','knots','kt']
  },
  {
    name: 'unit.millimeters-per-minute',
    symbol: 'mm/min',
    tags: ['feed rate','cutting feed rate','millimeters per minute','mm/min']
  },
  {
    name: 'unit.kilometer-per-hour-squared',
    symbol: 'km/h²',
    tags: ['acceleration','rate of change of velocity','kilometer per hour squared','km/h²']
  },
  {
    name: 'unit.foot-per-second-squared',
    symbol: 'ft/s²',
    tags: ['acceleration','rate of change of velocity','foot per second squared','ft/s²']
  },
  {
    name: 'unit.pascal',
    symbol: 'Pa',
    tags: ['pressure','force','compression','tension','pascal','pascals','Pa','atmospheric pressure','air pressure',
      'weather','altitude','flight']
  },
  {
    name: 'unit.kilopascal',
    symbol: 'kPa',
    tags: ['pressure','force','compression','tension','kilopascal','kilopascals','kPa']
  },
  {
    name: 'unit.megapascal',
    symbol: 'MPa',
    tags: ['pressure','force','compression','tension','megapascal','megapascals','MPa']
  },
  {
    name: 'unit.gigapascal',
    symbol: 'GPa',
    tags: ['pressure','force','compression','tension','gigapascal','gigapascals','GPa']
  },
  {
    name: 'unit.millibar',
    symbol: 'mbar',
    tags: ['pressure','force','compression','tension','millibar','millibars','mbar']
  },
  {
    name: 'unit.bar',
    symbol: 'bar',
    tags: ['pressure','force','compression','tension','bar','bars']
  },
  {
    name: 'unit.kilobar',
    symbol: 'kbar',
    tags: ['pressure','force','compression','tension','kilobar','kilobars','kbar']
  },
  {
    name: 'unit.newton',
    symbol: 'N',
    tags: ['force','pressure','newton','newtons','N','push','pull','weight','gravity','N']
  },
  {
    name: 'unit.newton-meter',
    symbol: 'Nm',
    tags: ['torque','rotational force','newton meter','Nm']
  },
  {
    name: 'unit.foot-pounds',
    symbol: 'ft·lbf',
    tags: ['torque','rotational force','foot-pound','foot-pounds','ft·lbf']
  },
  {
    name: 'unit.inch-pounds',
    symbol: 'in·lbf',
    tags: ['torque','rotational force','inch-pounds','inch-pound','in·lbf']
  },
  {
    name: 'unit.newton-per-meter',
    symbol: 'N/m',
    tags: ['linear density','force per unit length','newton per meter','N/m']
  },
  {
    name: 'unit.atmospheres',
    symbol: 'atm',
    tags: ['pressure','force','compression','tension','atmosphere','atmospheres','atmospheric pressure','atm']
  },
  {
    name: 'unit.pounds-per-square-inch',
    symbol: 'psi',
    tags: ['pressure','force','compression','tension','pounds per square inch','psi']
  },
  {
    name: 'unit.torr',
    symbol: 'Torr',
    tags: ['pressure','force','compression','tension','vacuum pressure','torr']
  },
  {
    name: 'unit.inches-of-mercury',
    symbol: 'inHg',
    tags: ['pressure','force','compression','tension','vacuum pressure','inHg','atmospheric pressure','barometric pressure']
  },
  {
    name: 'unit.pascal-per-square-meter',
    symbol: 'Pa/m²',
    tags: ['pressure','stress','mechanical strength','pascal per square meter','Pa/m²']
  },
  {
    name: 'unit.pound-per-square-inch',
    symbol: 'psi/in²',
    tags: ['pressure','stress','mechanical strength','pound per square inch','psi/in²']
  },
  {
    name: 'unit.newton-per-square-meter',
    symbol: 'N/m²',
    tags: ['pressure','stress','mechanical strength','newton per square meter','N/m²']
  },
  {
    name: 'unit.kilogram-force-per-square-meter',
    symbol: 'kgf/m²',
    tags: ['pressure','stress','mechanical strength','kilogram-force per square meter','kgf/m²']
  },
  {
    name: 'unit.pascal-per-square-centimeter',
    symbol: 'Pa/cm²',
    tags: ['pressure','stress','mechanical strength','pascal per square centimeter','Pa/cm²']
  },
  {
    name: 'unit.ton-force-per-square-inch',
    symbol: 'tonf/in²',
    tags: ['pressure','stress','mechanical strength','ton-force per square inch','tonf/in²']
  },
  {
    name: 'unit.kilonewton-per-square-meter',
    symbol: 'kN/m²',
    tags: ['stress','pressure','mechanical strength','kilonewton per square meter','kN/m²']
  },
  {
    name: 'unit.newton-per-square-millimeter',
    symbol: 'N/mm²',
    tags: ['stress','pressure','mechanical strength','newton per square millimeter','N/mm²']
  },
  {
    name: 'unit.microjoule',
    symbol: 'μJ',
    tags: ['energy','microjoule','microjoules','μJ']
  },
  {
    name: 'unit.millijoule',
    symbol: 'mJ',
    tags: ['energy','millijoule','millijoules','mJ']
  },
  {
    name: 'unit.joule',
    symbol: 'J',
    tags: ['joule','joules','energy','work done','heat','electricity','mechanical work']
  },
  {
    name: 'unit.kilojoule',
    symbol: 'kJ',
    tags: ['energy','kilojoule','kilojoules','kJ']
  },
  {
    name: 'unit.megajoule',
    symbol: 'MJ',
    tags: ['energy','megajoule','megajoules','MJ']
  },
  {
    name: 'unit.gigajoule',
    symbol: 'GJ',
    tags: ['energy','gigajoule','gigajoules','GJ']
  },
  {
    name: 'unit.watt-hour',
    symbol: 'Wh',
    tags: ['energy','watt-hour','watt-hours','energy usage','power consumption','energy consumption','electricity usage']
  },
  {
    name: 'unit.kilowatt-hour',
    symbol: 'kWh',
    tags: ['energy','kilowatt-hour','kilowatt-hours','energy usage','power consumption','energy consumption','electricity usage']
  },
  {
    name: 'unit.electron-volts',
    symbol: 'eV',
    tags: ['energy','subatomic particles','radiation']
  },
  {
    name: 'unit.joules-per-coulomb',
    symbol: 'J/C',
    tags: ['electrical potential energy','voltage','joules per coulomb','J/C']
  },
  {
    name: 'unit.british-thermal-unit',
    symbol: 'BTU',
    tags: ['energy','heat','work done','british thermal unit','british thermal units','BTU']
  },
  {
    name: 'unit.foot-pound',
    symbol: 'ft·lb',
    tags: ['energy','foot-pound','foot-pounds','ft·lb','ft⋅lbf']
  },
  {
    name: 'unit.calorie',
    symbol: 'Cal',
    tags: ['energy','food energy','Calorie','Calories','Cal']
  },
  {
    name: 'unit.small-calorie',
    symbol: 'cal',
    tags: ['energy','small calorie','calories','cal']
  },
  {
    name: 'unit.kilocalorie',
    symbol: 'kcal',
    tags: ['energy','small calorie','kilocalories','kcal']
  },
  {
    name: 'unit.joule-per-kelvin',
    symbol: 'J/K',
    tags: ['specific heat capacity','heat capacity per unit temperature','joule per kelvin','J/K']
  },
  {
    name: 'unit.joule-per-kilogram-kelvin',
    symbol: 'J/(kg·K)',
    tags: ['specific heat capacity','heat capacity per unit mass and temperature','joule per kilogram-kelvin','J/(kg·K)']
  },
  {
    name: 'unit.joule-per-kilogram',
    symbol: 'J/kg',
    tags: ['specific energy','specific energy capacity','joule per kilogram','J/kg']
  },
  {
    name: 'unit.watt-per-meter-kelvin',
    symbol: 'W/(m·K)',
    tags: ['thermal conductivity','watt per meter-kelvin','W/(m·K)']
  },
  {
    name: 'unit.joule-per-cubic-meter',
    symbol: 'J/m³',
    tags: ['energy density','joule per cubic meter','J/m³']
  },
  {
    name: 'unit.therm',
    symbol: 'thm',
    tags: ['energy','natural gas consumption','BTU','therm','thm']
  },
  {
    name: 'unit.electric-dipole-moment',
    symbol: 'C·m',
    tags: ['electric dipole','dipole moment','coulomb meter','C·m']
  },
  {
    name: 'unit.magnetic-dipole-moment',
    symbol: 'A·m²',
    tags: ['magnetic dipole','dipole moment','ampere square meter','A·m²']
  },
  {
    name: 'unit.debye',
    symbol: 'D',
    tags: ['polarization','electric dipole moment','debye','D']
  },
  {
    name: 'unit.coulomb-per-square-meter-per-volt',
    symbol: 'C·m²/V',
    tags: ['polarization','electric field','coulomb per square meter per volt','C·m²/V']
  },
  {
    name: 'unit.milliwatt',
    symbol: 'mW',
    tags: ['power','horsepower','performance','milliwatt','milliwatts','electricity','mW']
  },
  {
    name: 'unit.microwatt',
    symbol: 'μW',
    tags: ['power','horsepower','performance','microwatt','microwatts','electricity','μW']
  },
  {
    name: 'unit.watt',
    symbol: 'W',
    tags: ['power','horsepower','performance','watt','watts','electricity','W']
  },
  {
    name: 'unit.kilowatt',
    symbol: 'kW',
    tags: ['power','horsepower','performance','kilowatt','kilowatts','electricity','kW']
  },
  {
    name: 'unit.megawatt',
    symbol: 'MW',
    tags: ['power','horsepower','performance','megawatt','megawatts','electricity','MW']
  },
  {
    name: 'unit.gigawatt',
    symbol: 'GW',
    tags: ['power','horsepower','performance','gigawatt','gigawatts','electricity','GW']
  },
  {
    name: 'unit.metric-horsepower',
    symbol: 'PS',
    tags: ['power','performance','metric horsepower','PS']
  },
  {
    name: 'unit.milliwatt-per-square-centimeter',
    symbol: 'mW/cm²',
    tags: ['power density','radiation intensity','sunlight intensity','signal power','intensity',
      'milliwatts per square centimeter','UV Intensity','mW/cm²']
  },
  {
    name: 'unit.watt-per-square-centimeter',
    symbol: 'W/cm²',
    tags: ['power density','intensity of power','watts per square centimeter','W/cm²']
  },
  {
    name: 'unit.kilowatt-per-square-centimeter',
    symbol: 'kW/cm²',
    tags: ['power density','intensity of power','kilowatts per square centimeter','kW/cm²']
  },
  {
    name: 'unit.milliwatt-per-square-meter',
    symbol: 'mW/m²',
    tags: ['power density','intensity of power','milliwatts per square meter','mW/m²']
  },
  {
    name: 'unit.watt-per-square-meter',
    symbol: 'W/m²',
    tags: ['power density','intensity of power','watts per square meter','W/m²']
  },
  {
    name: 'unit.kilowatt-per-square-meter',
    symbol: 'kW/m²',
    tags: ['power density','intensity of power','kilowatts per square meter','kW/m²']
  },
  {
    name: 'unit.watt-per-square-inch',
    symbol: 'W/in²',
    tags: ['power density','intensity of power','watts per square inch','W/in²']
  },
  {
    name: 'unit.kilowatt-per-square-inch',
    symbol: 'kW/in²',
    tags: ['power density','intensity of power','kilowatts per square inch','kW/in²']
  },
  {
    name: 'unit.horsepower',
    symbol: 'hp',
    tags: ['power','horsepower','performance','electricity','horsepowers','hp']
  },
  {
    name: 'unit.btu-per-hour',
    symbol: 'BTU/h',
    tags: ['power','heat transfer','thermal energy','HVAC','BTU/h']
  },
  {
    name: 'unit.coulomb',
    symbol: 'C',
    tags: ['charge','electricity','electrostatics','Coulomb','C']
  },
  {
    name: 'unit.millicoulomb',
    symbol: 'mC',
    tags: ['charge','electricity','electrostatics','millicoulombs','mC']
  },
  {
    name: 'unit.microcoulomb',
    symbol: 'µC',
    tags: ['charge','electricity','electrostatics','microcoulomb','µC']
  },
  {
    name: 'unit.picocoulomb',
    symbol: 'pC',
    tags: ['charge','electricity','electrostatics','picocoulomb','pC']
  },
  {
    name: 'unit.coulomb-per-meter',
    symbol: 'C/m',
    tags: ['electric displacement field per length','coulomb per meter','C/m']
  },
  {
    name: 'unit.coulomb-per-cubic-meter',
    symbol: 'C/m³',
    tags: ['electric charge density','coulomb per cubic meter','C/m³']
  },
  {
    name: 'unit.coulomb-per-square-meter',
    symbol: 'C/m²',
    tags: ['electric surface charge density','coulomb per square meter','C/m²']
  },
  {
    name: 'unit.square-millimeter',
    symbol: 'mm²',
    tags: ['area','lot','zone','space','region','square millimeter','square millimeters','mm²','sq-mm']
  },
  {
    name: 'unit.square-centimeter',
    symbol: 'cm²',
    tags: ['area','lot','zone','space','region','square centimeter','square centimeters','cm²','sq-cm']
  },
  {
    name: 'unit.square-meter',
    symbol: 'm²',
    tags: ['area','lot','zone','space','region','square meter','square meters','m²','sq-m']
  },
  {
    name: 'unit.hectare',
    symbol: 'ha',
    tags: ['area','lot','zone','space','region','hectare','hectares','ha']
  },
  {
    name: 'unit.square-kilometer',
    symbol: 'km²',
    tags: ['area','lot','zone','space','region','square kilometer','square kilometers','km²','sq-km']
  },
  {
    name: 'unit.square-inch',
    symbol: 'in²',
    tags: ['area','lot','zone','space','region','square inch','square inches','in²','sq-in']
  },
  {
    name: 'unit.square-foot',
    symbol: 'ft²',
    tags: ['area','lot','zone','space','region','square foot','square feet','ft²','sq-ft']
  },
  {
    name: 'unit.square-yard',
    symbol: 'yd²',
    tags: ['area','lot','zone','space','region','square yard','square yards','yd²','sq-yd']
  },
  {
    name: 'unit.acre',
    symbol: 'a',
    tags: ['area','lot','zone','space','region','acre','acres','a']
  },
  {
    name: 'unit.square-mile',
    symbol: 'ml²',
    tags: ['area','lot','zone','space','region','square mile','square miles','ml²','sq-mi']
  },
  {
    name: 'unit.are',
    symbol: 'are',
    tags: ['area','land measurement','are']
  },
  {
    name: 'unit.barn',
    symbol: 'barn',
    tags: ['cross-sectional area','particle physics','nuclear physics','barn']
  },
  {
    name: 'unit.circular-inch',
    symbol: 'circin',
    tags: ['area','circular measurement','circular inch','circin']
  },
  {
    name: 'unit.milliampere-hour',
    symbol: 'mAh',
    tags: ['electric current','current flow','electric charge','current capacity','flow of electricity',
      'electrical flow','milliampere-hour','milliampere-hours','mAh']
  },
  {
    name: 'unit.ampere-hours',
    symbol: 'Ah',
    tags: ['electric current','current flow','electric charge','current capacity','flow of electricity',
      'electrical flow','ampere','ampere-hours','Ah']
  },
  {
    name: 'unit.kiloampere-hours',
    symbol: 'kAh',
    tags: ['electric current','current flow','electric charge','current capacity','flow of electricity','electrical flow',
      'kiloampere-hours','kiloampere-hour','kAh']
  },
  {
    name: 'unit.nanoampere',
    symbol: 'nA',
    tags: ['current','amperes','nanoampere','nA']
  },
  {
    name: 'unit.picoampere',
    symbol: 'pA',
    tags: ['current','amperes','picoampere','pA']
  },
  {
    name: 'unit.microampere',
    symbol: 'μA',
    tags: ['electric current','microampere','microamperes','μA']
  },
  {
    name: 'unit.milliampere',
    symbol: 'mA',
    tags: ['electric current','milliampere','milliamperes','mA']
  },
  {
    name: 'unit.ampere',
    symbol: 'A',
    tags: ['electric current','current flow','flow of electricity','electrical flow','ampere','amperes','amperage','A']
  },
  {
    name: 'unit.kiloamperes',
    symbol: 'kA',
    tags: ['electric current','current flow','kiloamperes','kA']
  },
  {
    name: 'unit.microampere-per-square-centimeter',
    symbol: 'µA/cm²',
    tags: ['Current density','microampere per square centimeter','µA/cm²']
  },
  {
    name: 'unit.ampere-per-square-meter',
    symbol: 'A/m²',
    tags: ['current density','current per unit area','ampere per square meter','A/m²']
  },
  {
    name: 'unit.ampere-per-meter',
    symbol: 'A/m',
    tags: ['magnetic field strength','magnetic field intensity','ampere per meter','A/m']
  },
  {
    name: 'unit.oersted',
    symbol: 'Oe',
    tags: ['magnetic field','oersted','Oe']
  },
  {
    name: 'unit.bohr-magneton',
    symbol: 'μB',
    tags: ['atomic physics','magnetic moment','bohr magneton','μB']
  },
  {
    name: 'unit.ampere-meter-squared',
    symbol: 'A·m²',
    tags: ['magnetic moment','dipole moment','ampere-meter squared','A·m²']
  },
  {
    name: 'unit.ampere-meter',
    symbol: 'A·m',
    tags: ['magnetic field','current loop','ampere-meter','A·m']
  },
  {
    name: 'unit.nanovolt',
    symbol: 'nV',
    tags: ['voltage','volts','nanovolt','nV']
  },
  {
    name: 'unit.picovolt',
    symbol: 'pV',
    tags: ['voltage','volts','picovolt','pV']
  },
  {
    name: 'unit.millivolts',
    symbol: 'mV',
    tags: ['electric potential','electric tension','voltage','millivolt','millivolts','mV']
  },
  {
    name: 'unit.microvolts',
    symbol: 'μV',
    tags: ['electric potential','electric tension','voltage','microvolt','microvolts','μV']
  },
  {
    name: 'unit.volt',
    symbol: 'V',
    tags: ['electric potential','electric tension','voltage','volt','volts','V','power source','battery','battery level']
  },
  {
    name: 'unit.kilovolts',
    symbol: 'kV',
    tags: ['electric potential','electric tension','voltage','kilovolt','kilovolts','kV']
  },
  {
    name: 'unit.dbmV',
    symbol: 'dBmV',
    tags: ['decibels millivolt','voltage level','signal','dBmV']
  },
  {
    name: 'unit.volt-meter',
    symbol: 'V·m',
    tags: ['electric flux','volt-meter','V·m']
  },
  {
    name: 'unit.kilovolt-meter',
    symbol: 'kV·m',
    tags: ['electric flux','kilovolt-meter','kV·m']
  },
  {
    name: 'unit.megavolt-meter',
    symbol: 'MV·m',
    tags: ['electric flux','megavolt-meter','MV·m']
  },
  {
    name: 'unit.microvolt-meter',
    symbol: 'µV·m',
    tags: ['electric flux','microvolt-meter','µV·m']
  },
  {
    name: 'unit.millivolt-meter',
    symbol: 'mV·m',
    tags: ['electric flux','millivolt-meter','mV·m']
  },
  {
    name: 'unit.nanovolt-meter',
    symbol: 'nV·m',
    tags: ['electric flux','nanovolt-meter','nV·m']
  },
  {
    name: 'unit.ohm',
    symbol: 'Ω',
    tags: ['electrical resistance','resistance','impedance','ohm']
  },
  {
    name: 'unit.microohm',
    symbol: 'μΩ',
    tags: ['electrical resistance','resistance','microohm','μΩ']
  },
  {
    name: 'unit.milliohm',
    symbol: 'mΩ',
    tags: ['electrical resistance','resistance','milliohm','mΩ']
  },
  {
    name: 'unit.kilohm',
    symbol: 'kΩ',
    tags: ['electrical resistance','resistance','kilohm','kΩ']
  },
  {
    name: 'unit.megohm',
    symbol: 'MΩ',
    tags: ['electrical resistance','resistance','megohm','MΩ']
  },
  {
    name: 'unit.gigohm',
    symbol: 'GΩ',
    tags: ['electrical resistance','resistance','gigohm','GΩ']
  },
  {
    name: 'unit.hertz',
    symbol: 'Hz',
    tags: ['frequency','cycles per second','hertz','Hz']
  },
  {
    name: 'unit.kilohertz',
    symbol: 'kHz',
    tags: ['frequency','cycles per second','kilohertz','kHz']
  },
  {
    name: 'unit.megahertz',
    symbol: 'MHz',
    tags: ['frequency','cycles per second','megahertz','MHz']
  },
  {
    name: 'unit.gigahertz',
    symbol: 'GHz',
    tags: ['frequency','cycles per second','gigahertz','GHz']
  },
  {
    name: 'unit.rpm',
    symbol: 'RPM',
    tags: ['speed','velocity','cycle','engine','Revolutions Per Minute','RPM','angular velocity','rotation speed']
  },
  {
    name: 'unit.candela-per-square-meter',
    symbol: 'cd/m²',
    tags: ['brightness','light level','Luminance','Candela per square meter','cd/m²']
  },
  {
    name: 'unit.candela',
    symbol: 'cd',
    tags: ['light intensity','candle power','luminous intensity','Candela','cd']
  },
  {
    name: 'unit.lumen',
    symbol: 'lm',
    tags: ['total light output','light power','luminous flux','Lumen','lm']
  },
  {
    name: 'unit.lux',
    symbol: 'lx',
    tags: ['illumination','light level on a surface','illuminance','Lux','lx']
  },
  {
    name: 'unit.foot-candle',
    symbol: 'fc',
    tags: ['illuminance','light level','foot-candle','fc']
  },
  {
    name: 'unit.lumen-per-square-meter',
    symbol: 'lm/m²',
    tags: ['illuminance','light level','lumen per square meter','lm/m²']
  },
  {
    name: 'unit.lux-second',
    symbol: 'lx·s',
    tags: ['light exposure','illumination time','light dosage','Lux second','lx·s']
  },
  {
    name: 'unit.lumen-second',
    symbol: 'lm·s',
    tags: ['total light energy','luminous energy','Lumen second','lm·s']
  },
  {
    name: 'unit.lumens-per-watt',
    symbol: 'lm/W',
    tags: ['lighting efficiency','light output per energy','luminous efficacy','Lumens per watt','lm/W']
  },
  {
    name: 'unit.absorbance',
    symbol: 'AU',
    tags: ['optical density','light absorption','absorbance','AU']
  },
  {
    name: 'unit.mole',
    symbol: 'mol',
    tags: ['amount of substance','substance quantity','mole','moles','mol']
  },
  {
    name: 'unit.nanomole',
    symbol: 'nmol',
    tags: ['amount of substance','substance quantity','concentration','nanomole','nmol']
  },
  {
    name: 'unit.micromole',
    symbol: 'μmol',
    tags: ['amount of substance','substance quantity','micromole','μmol']
  },
  {
    name: 'unit.millimole',
    symbol: 'mmol',
    tags: ['amount of substance','substance quantity','millimole','mmol']
  },
  {
    name: 'unit.kilomole',
    symbol: 'kmol',
    tags: ['amount of substance','substance quantity','kilomole','kmol']
  },
  {
    name: 'unit.mole-per-cubic-meter',
    symbol: 'mol/m³',
    tags: ['concentration','amount of substance','mole per cubic meter','mol/m³']
  },
  {
    name: 'unit.battery',
    symbol: '%',
    tags: ['power source','state of charge (SoC)','battery','battery level','level','humidity','moisture',
      'relative humidity','water content','soil moisture','irrigation','water in soil','soil water content','VWC',
      'Volumetric Water Content','Total Harmonic Distortion','THD','power quality','UV Transmittance','%']
  },
  {
    name: 'unit.rssi',
    symbol: 'rssi',
    tags: ['signal strength','signal level','received signal strength indicator','rssi','dBm']
  },
  {
    name: 'unit.ppm',
    symbol: 'ppm',
    tags: ['carbon dioxide','co²','carbon monoxide','co','aqi','air quality','total volatile organic compounds','tvoc','ppm']
  },
  {
    name: 'unit.ppb',
    symbol: 'ppb',
    tags: ['ozone','o³','nitrogen dioxide','no²','sulfur dioxide','so²','aqi','air quality','tvoc','ppb']
  },
  {
    name: 'unit.micrograms-per-cubic-meter',
    symbol: 'µg/m³',
    tags: ['coarse particulate matter','pm10','fine particulate matter','pm2.5','aqi','air quality',
      'total volatile organic compounds','tvoc','micrograms per cubic meter','µg/m³']
  },
  {
    name: 'unit.aqi',
    symbol: 'aqi',
    tags: ['AQI','air quality index']
  },
  {
    name: 'unit.gram-per-cubic-meter',
    symbol: 'g/m³',
    tags: ['humidity','moisture','absolute humidity','g/m³']
  },
  {
    name: 'unit.gram-per-kilogram',
    symbol: 'g/kg',
    tags: ['humidity','moisture','specific humidity','g/kg']
  },
  {
    name: 'unit.millimeters-per-second',
    symbol: 'mm/s',
    tags: ['velocity','speed','rate of motion','peak','peak to peak','root mean square (RMS)','vibration','mm/s']
  },
  {
    name: 'unit.neper',
    symbol: 'Np',
    tags: ['logarithmic unit','ratio','gain','loss','attenuation','neper','Np']
  },
  {
    name: 'unit.bel',
    symbol: 'B',
    tags: ['logarithmic unit','power ratio','intensity ratio','bel','B']
  },
  {
    name: 'unit.decibel',
    symbol: 'dB',
    tags: ['noise level','sound level','volume','acoustics','decibel','dB']
  },
  {
    name: 'unit.meters-per-second-squared',
    symbol: 'm/s²',
    tags: ['peak','peak to peak','root mean square (RMS)','vibration','meters per second squared','m/s²']
  },
  {
    name: 'unit.becquerel',
    symbol: 'Bq',
    tags: ['radioactivity','radiation','becquerel','Bq']
  },
  {
    name: 'unit.curie',
    symbol: 'Ci',
    tags: ['radioactivity','radiation','curie','Ci']
  },
  {
    name: 'unit.gray',
    symbol: 'Gy',
    tags: ['radiation dose','gray','Gy']
  },
  {
    name: 'unit.sievert',
    symbol: 'Sv',
    tags: ['radiation dose','sievert','radiation dose equivalent2','Sv']
  },
  {
    name: 'unit.roentgen',
    symbol: 'R',
    tags: ['radiation exposure','roentgen','R']
  },
  {
    name: 'unit.cps',
    symbol: 'cps',
    tags: ['radiation detection','counts per second','cps']
  },
  {
    name: 'unit.rad',
    symbol: 'Rad',
    tags: ['radiation dose','rad']
  },
  {
    name: 'unit.rem',
    symbol: 'Rem',
    tags: ['radiation dose equivalent','rem']
  },
  {
    name: 'unit.dps',
    symbol: 'dps',
    tags: ['radioactive decay','radioactivity','disintegrations per second','dps']
  },
  {
    name: 'unit.rutherford',
    symbol: 'Rd',
    tags: ['radioactive decay','radioactivity','rutherford','Rd']
  },
  {
    name: 'unit.coulombs-per-kilogram',
    symbol: 'C/kg',
    tags: ['radiation exposure','dose','coulombs per kilogram','electric charge-to-mass ratio','C/kg']
  },
  {
    name: 'unit.becquerels-per-cubic-meter',
    symbol: 'Bq/m³',
    tags: ['radioactivity','radiation','becquerels per cubic meter','Bq/m³']
  },
  {
    name: 'unit.curies-per-liter',
    symbol: 'Ci/L',
    tags: ['radioactivity','radiation','curies per liter','Ci/L']
  },
  {
    name: 'unit.becquerels-per-second',
    symbol: 'Bq/s',
    tags: ['radioactive decay rate','becquerels per second','Bq/s']
  },
  {
    name: 'unit.curies-per-second',
    symbol: 'Ci/s',
    tags: ['radioactive decay rate','curies per second','Ci/s']
  },
  {
    name: 'unit.gy-per-second',
    symbol: 'Gy/s',
    tags: ['absorbed dose rate','radiation dose rate','gray per second','Gy/s']
  },
  {
    name: 'unit.watt-per-steradian',
    symbol: 'W/sr',
    tags: ['radiant intensity','power per unit solid angle','watt per steradian','W/sr']
  },
  {
    name: 'unit.watt-per-square-metre-steradian',
    symbol: 'W/(m²·sr)',
    tags: ['radiance','radiant flux density','watt per square metre-steradian','W/(m²·sr)']
  },
  {
    name: 'unit.ph-level',
    symbol: 'pH',
    tags: ['acidity','alkalinity','neutral','acid','base','pH','soil pH','water quality','water pH']
  },
  {
    name: 'unit.turbidity',
    symbol: 'NTU',
    tags: ['water turbidity','water clarity','Nephelometric Turbidity Units','NTU']
  },
  {
    name: 'unit.mg-per-liter',
    symbol: 'mg/L',
    tags: ['dissolved oxygen','water quality','mg/L']
  },
  {
    name: 'unit.microsiemens-per-centimeter',
    symbol: 'µS/cm',
    tags: ['Electrical conductivity','water quality','soil quality','microsiemens per centimeter','µS/cm']
  },
  {
    name: 'unit.millisiemens-per-meter',
    symbol: 'mS/m',
    tags: ['Electrical conductivity','water quality','soil quality','millisiemens per meter','mS/m']
  },
  {
    name: 'unit.siemens-per-meter',
    symbol: 'S/m',
    tags: ['Electrical conductivity','water quality','soil quality','siemens per meter','S/m']
  },
  {
    name: 'unit.kilogram-per-cubic-meter',
    symbol: 'kg/m³',
    tags: ['density','mass per unit volume','kg/m³']
  },
  {
    name: 'unit.gram-per-cubic-centimeter',
    symbol: 'g/cm³',
    tags: ['density','mass per unit volume','g/cm³']
  },
  {
    name: 'unit.kilogram-per-square-meter',
    symbol: 'kg/m²',
    tags: ['density','surface density','areal density','mass per unit area','kg/m²']
  },
  {
    name: 'unit.milligram-per-milliliter',
    symbol: 'mg/mL',
    tags: ['concentration','mass per volume','mg/mL']
  },
  {
    name: 'unit.pound-per-cubic-foot',
    symbol: 'lb/ft³',
    tags: ['Density','mass per unit volume','lb/ft³']
  },
  {
    name: 'unit.ounces-per-cubic-inch',
    symbol: 'oz/in³',
    tags: ['density','mass per unit volume','oz/in³']
  },
  {
    name: 'unit.tons-per-cubic-yard',
    symbol: 'ton/yd³',
    tags: ['density','mass per unit volume','ton/yd³']
  },
  {
    name: 'unit.particle-density',
    symbol: 'particles/mL',
    tags: ['particle concentration','count','particles/mL']
  },
  {
    name: 'unit.kilometers-per-liter',
    symbol: 'km/L',
    tags: ['fuel efficiency','km/L']
  },
  {
    name: 'unit.miles-per-gallon',
    symbol: 'mpg',
    tags: ['fuel efficiency','mpg']
  },
  {
    name: 'unit.liters-per-100-km',
    symbol: 'L/100km',
    tags: ['fuel efficiency','L/100km']
  },
  {
    name: 'unit.gallons-per-mile',
    symbol: 'gal/mi',
    tags: ['fuel efficiency','gal/mi']
  },
  {
    name: 'unit.liters-per-hour',
    symbol: 'L/hr',
    tags: ['fuel consumption','L/hr']
  },
  {
    name: 'unit.gallons-per-hour',
    symbol: 'gal/hr',
    tags: ['fuel consumption','gal/hr']
  },
  {
    name: 'unit.beats-per-minute',
    symbol: 'bpm',
    tags: ['heart rate','pulse','bpm']
  },
  {
    name: 'unit.millimeters-of-mercury',
    symbol: 'mmHg',
    tags: ['blood pressure','systolic','diastolic','mmHg']
  },
  {
    name: 'unit.milligrams-per-deciliter',
    symbol: 'mg/dL',
    tags: ['glucose','blood sugar','glucose level','mg/dL']
  },
  {
    name: 'unit.g-force',
    symbol: 'G',
    tags: ['acceleration','gravity','force','g-load','G']
  },
  {
    name: 'unit.kilonewton',
    symbol: 'kN',
    tags: ['force','kN']
  },
  {
    name: 'unit.kilogram-force',
    symbol: 'kgf',
    tags: ['force','kgf']
  },
  {
    name: 'unit.pound-force',
    symbol: 'lbf',
    tags: ['force','lbf']
  },
  {
    name: 'unit.kilopound-force',
    symbol: 'klbf',
    tags: ['force','klbf']
  },
  {
    name: 'unit.dyne',
    symbol: 'dyn',
    tags: ['force','dyn']
  },
  {
    name: 'unit.poundal',
    symbol: 'pdl',
    tags: ['force','pdl']
  },
  {
    name: 'unit.kip',
    symbol: 'kip',
    tags: ['force','kip']
  },
  {
    name: 'unit.gal',
    symbol: 'Gal',
    tags: ['acceleration','gravity','g-force','Gal']
  },
  {
    name: 'unit.gravity',
    symbol: 'gravity',
    tags: ['acceleration','gravity','g-force']
  },
  {
    name: 'unit.hectopascal',
    symbol: 'hPa',
    tags: ['atmospheric pressure','air pressure','weather','altitude','flight','hPa']
  },
  {
    name: 'unit.atmosphere',
    symbol: 'atm',
    tags: ['atmospheric pressure','air pressure','weather','altitude','flight','atm']
  },
  {
    name: 'unit.millibars',
    symbol: 'mb',
    tags: ['atmospheric pressure','air pressure','weather','altitude','flight','mb']
  },
  {
    name: 'unit.inch-of-mercury',
    symbol: 'inHg',
    tags: ['atmospheric pressure','air pressure','weather','altitude','flight','inHg','richter']
  },
  {
    name: 'unit.richter-scale',
    symbol: 'richter',
    tags: ['earthquake','seismic activity','richter']
  },
  {
    name: 'unit.percentage',
    symbol: '%',
    tags: ['percentage']
  },
  {
    name: 'unit.second',
    symbol: 's',
    tags: ['time','duration','interval','angle','second','arcsecond','sec']
  },
  {
    name: 'unit.minute',
    symbol: 'min',
    tags: ['time','duration','interval','angle','minute','arcminute','min']
  },
  {
    name: 'unit.hour',
    symbol: 'h',
    tags: ['time','duration','interval','h']
  },
  {
    name: 'unit.day',
    symbol: 'd',
    tags: ['time','duration','interval','d']
  },
  {
    name: 'unit.week',
    symbol: 'wk',
    tags: ['time','duration','interval','wk']
  },
  {
    name: 'unit.month',
    symbol: 'mo',
    tags: ['time','duration','interval','mo']
  },
  {
    name: 'unit.year',
    symbol: 'yr',
    tags: ['time','duration','interval','yr']
  },
  {
    name: 'unit.cubic-foot-per-minute',
    symbol: 'ft³/min',
    tags: ['airflow','ventilation','HVAC','gas flow rate','CFM','flow rate','fluid flow','cubic foot per minute','ft³/min']
  },
  {
    name: 'unit.cubic-meters-per-hour',
    symbol: 'm³/hr',
    tags: ['airflow','ventilation','HVAC','gas flow rate','cubic meters per hour','m³/hr']
  },
  {
    name: 'unit.cubic-meters-per-second',
    symbol: 'm³/s',
    tags: ['airflow','ventilation','HVAC','gas flow rate','cubic meters per second','m³/s']
  },
  {
    name: 'unit.liter-per-second',
    symbol: 'L/s',
    tags: ['airflow','ventilation','HVAC','gas flow rate','liter per second','L/s']
  },
  {
    name: 'unit.liter-per-minute',
    symbol: 'L/min',
    tags: ['airflow','ventilation','HVAC','gas flow rate','liter per minute','L/min']
  },
  {
    name: 'unit.gallons-per-minute',
    symbol: 'GPM',
    tags: ['airflow','ventilation','HVAC','gas flow rate','gallons per minute','GPM']
  },
  {
    name: 'unit.cubic-foot-per-second',
    symbol: 'ft³/s',
    tags: ['flow rate','fluid flow','cubic foot per second','cubic feet per second','ft³/s']
  },
  {
    name: 'unit.milliliters-per-minute',
    symbol: 'mL/min',
    tags: ['Flow rate','fluid dynamics','milliliters per minute','mL/min']
  },
  {
    name: 'unit.bit',
    symbol: 'bit',
    tags: ['data','binary digit','information','bit']
  },
  {
    name: 'unit.byte',
    symbol: 'B',
    tags: ['data','byte','information','storage','memory','B']
  },
  {
    name: 'unit.kilobyte',
    symbol: 'KB',
    tags: ['data','kilobyte','KB']
  },
  {
    name: 'unit.megabyte',
    symbol: 'MB',
    tags: ['data','megabyte','MB']
  },
  {
    name: 'unit.gigabyte',
    symbol: 'GB',
    tags: ['data','gigabyte','GB']
  },
  {
    name: 'unit.terabyte',
    symbol: 'TB',
    tags: ['data','terabyte','TB']
  },
  {
    name: 'unit.petabyte',
    symbol: 'PB',
    tags: ['data','petabyte','PB']
  },
  {
    name: 'unit.exabyte',
    symbol: 'EB',
    tags: ['data','exabyte','EB']
  },
  {
    name: 'unit.zettabyte',
    symbol: 'ZB',
    tags: ['data','zettabyte','ZB']
  },
  {
    name: 'unit.yottabyte',
    symbol: 'YB',
    tags: ['data','yottabyte','YB']
  },
  {
    name: 'unit.bit-per-second',
    symbol: 'bps',
    tags: ['data transfer rate','bps']
  },
  {
    name: 'unit.kilobit-per-second',
    symbol: 'kbps',
    tags: ['data transfer rate','kbps']
  },
  {
    name: 'unit.megabit-per-second',
    symbol: 'Mbps',
    tags: ['data transfer rate','Mbps']
  },
  {
    name: 'unit.gigabit-per-second',
    symbol: 'Gbps',
    tags: ['data transfer rate','Gbps']
  },
  {
    name: 'unit.terabit-per-second',
    symbol: 'Tbps',
    tags: ['data transfer rate','Tbps']
  },
  {
    name: 'unit.byte-per-second',
    symbol: 'B/s',
    tags: ['data transfer rate','B/s']
  },
  {
    name: 'unit.kilobyte-per-second',
    symbol: 'KB/s',
    tags: ['data transfer rate','KB/s']
  },
  {
    name: 'unit.megabyte-per-second',
    symbol: 'MB/s',
    tags: ['data transfer rate','MB/s']
  },
  {
    name: 'unit.gigabyte-per-second',
    symbol: 'GB/s',
    tags: ['data transfer rate','GB/s']
  },
  {
    name: 'unit.degree',
    symbol: 'deg',
    tags: ['angle','degree','degrees','deg']
  },
  {
    name: 'unit.radian',
    symbol: 'rad',
    tags: ['angle','radian','radians','rad']
  },
  {
    name: 'unit.gradian',
    symbol: 'grad',
    tags: ['angle','gradian','grades','grad']
  },
  {
    name: 'unit.mil',
    symbol: 'mil',
    tags: ['angle','military angle','angular mil','mil']
  },
  {
    name: 'unit.revolution',
    symbol: 'rev',
    tags: ['angle','revolution','full circle','complete turn','rev']
  },
  {
    name: 'unit.siemens',
    symbol: 'S',
    tags: ['electrical conductance','conductance','siemens','S']
  },
  {
    name: 'unit.millisiemens',
    symbol: 'mS',
    tags: ['electrical conductance','conductance','millisiemens','mS']
  },
  {
    name: 'unit.microsiemens',
    symbol: 'μS',
    tags: ['electrical conductance','conductance','microsiemens','μS']
  },
  {
    name: 'unit.kilosiemens',
    symbol: 'kS',
    tags: ['electrical conductance','conductance','kilosiemens','kS']
  },
  {
    name: 'unit.megasiemens',
    symbol: 'MS',
    tags: ['electrical conductance','conductance','megasiemens','MS']
  },
  {
    name: 'unit.gigasiemens',
    symbol: 'GS',
    tags: ['electrical conductance','conductance','gigasiemens','GS']
  },
  {
    name: 'unit.farad',
    symbol: 'F',
    tags: ['electric capacitance','capacitance','farad','F']
  },
  {
    name: 'unit.millifarad',
    symbol: 'mF',
    tags: ['electric capacitance','capacitance','millifarad','mF']
  },
  {
    name: 'unit.microfarad',
    symbol: 'μF',
    tags: ['electric capacitance','capacitance','microfarad','μF']
  },
  {
    name: 'unit.nanofarad',
    symbol: 'nF',
    tags: ['electric capacitance','capacitance','nanofarad','nF']
  },
  {
    name: 'unit.picofarad',
    symbol: 'pF',
    tags: ['electric capacitance','capacitance','picofarad','pF']
  },
  {
    name: 'unit.kilofarad',
    symbol: 'kF',
    tags: ['electric capacitance','capacitance','kilofarad','kF']
  },
  {
    name: 'unit.megafarad',
    symbol: 'MF',
    tags: ['electric capacitance','capacitance','megafarad','MF']
  },
  {
    name: 'unit.gigafarad',
    symbol: 'GF',
    tags: ['electric capacitance','capacitance','gigafarad','GF']
  },
  {
    name: 'unit.terfarad',
    symbol: 'TF',
    tags: ['electric capacitance','capacitance','terafarad','TF']
  },
  {
    name: 'unit.farad-per-meter',
    symbol: 'F/m',
    tags: ['electric permittivity','farad per meter','F/m']
  },
  {
    name: 'unit.tesla',
    symbol: 'T',
    tags: ['magnetic field','magnetic field strength','tesla','T','magnetic flux density']
  },
  {
    name: 'unit.gauss',
    symbol: 'G',
    tags: ['magnetic field','magnetic field strength','gauss','G','magnetic flux density']
  },
  {
    name: 'unit.kilogauss',
    symbol: 'kG',
    tags: ['magnetic field','magnetic field strength','kilogauss','kG','magnetic flux density']
  },
  {
    name: 'unit.millitesla',
    symbol: 'mT',
    tags: ['magnetic field','magnetic field strength','millitesla','mT']
  },
  {
    name: 'unit.microtesla',
    symbol: 'μT',
    tags: ['magnetic field','magnetic field strength','microtesla','μT']
  },
  {
    name: 'unit.nanotesla',
    symbol: 'nT',
    tags: ['magnetic field','magnetic field strength','nanotesla','nT']
  },
  {
    name: 'unit.kilotesla',
    symbol: 'kT',
    tags: ['magnetic field','magnetic field strength','kilotesla','kT']
  },
  {
    name: 'unit.megatesla',
    symbol: 'MT',
    tags: ['magnetic field','magnetic field strength','megatesla','MT']
  },
  {
    name: 'unit.millitesla-square-meters',
    symbol: 'millitesla square meters',
    tags: ['magnetic field','millitesla square meters']
  },
  {
    name: 'unit.gamma',
    symbol: 'γ',
    tags: ['magnetic flux density','gamma','γ']
  },
  {
    name: 'unit.lambda',
    symbol: 'λ',
    tags: ['wavelength','lambda','λ']
  },
  {
    name: 'unit.square-meter-per-second',
    symbol: 'm²/s',
    tags: ['kinematic viscosity','m²/s']
  },
  {
    name: 'unit.square-centimeter-per-second',
    symbol: 'cm²/s',
    tags: ['kinematic viscosity','cm²/s']
  },
  {
    name: 'unit.stoke',
    symbol: 'St',
    tags: ['kinematic viscosity','stokes','St']
  },
  {
    name: 'unit.centistokes',
    symbol: 'cSt',
    tags: ['kinematic viscosity','centistokes','cSt']
  },
  {
    name: 'unit.square-foot-per-second',
    symbol: 'ft²/s',
    tags: ['kinematic viscosity','ft²/s']
  },
  {
    name: 'unit.square-inch-per-second',
    symbol: 'in²/s',
    tags: ['kinematic viscosity','in²/s']
  },
  {
    name: 'unit.pascal-second',
    symbol: 'Pa·s',
    tags: ['dynamic viscosity','viscosity','fluid mechanics','pascal-second','Pa·s']
  },
  {
    name: 'unit.centipoise',
    symbol: 'cP',
    tags: ['viscosity','dynamic viscosity','fluid viscosity','centipoise','cP']
  },
  {
    name: 'unit.poise',
    symbol: 'P',
    tags: ['viscosity','dynamic viscosity','fluid viscosity','poise','P']
  },
  {
    name: 'unit.reynolds',
    symbol: 'Re',
    tags: ['fluid flow regime','fluid mechanics','reynolds','Re']
  },
  {
    name: 'unit.pound-per-foot-hour',
    symbol: 'lb/(ft·h)',
    tags: ['pound per foot-hour','lb/(ft·h)']
  },
  {
    name: 'unit.newton-second-per-square-meter',
    symbol: 'N·s/m²',
    tags: ['newton second per square meter','N·s/m²']
  },
  {
    name: 'unit.dyne-second-per-square-centimeter',
    symbol: 'dyn·s/cm²',
    tags: ['dyne second per square centimeter','dyn·s/cm²']
  },
  {
    name: 'unit.kilogram-per-meter-second',
    symbol: 'kg/(m·s)',
    tags: ['kilogram per meter-second','kg/(m·s)']
  },
  {
    name: 'unit.tesla-square-meters',
    symbol: 'T/m²',
    tags: ['magnetic flux density','tesla square meters','T/m²']
  },
  {
    name: 'unit.maxwell',
    symbol: 'Mx',
    tags: ['magnetic flux','magnetic field','maxwell','Mx']
  },
  {
    name: 'unit.tesla-per-meter',
    symbol: 'T/m',
    tags: ['magnetic field','tesla per meter','T/m']
  },
  {
    name: 'unit.gauss-per-centimeter',
    symbol: 'G/cm',
    tags: ['magnetic field','gauss per centimeter','G/cm']
  },
  {
    name: 'unit.weber',
    symbol: 'Wb',
    tags: ['magnetic flux','weber','Wb']
  },
  {
    name: 'unit.microweber',
    symbol: 'µWb',
    tags: ['magnetic flux','microweber','µWb']
  },
  {
    name: 'unit.milliweber',
    symbol: 'mWb',
    tags: ['magnetic flux','milliweber','mWb']
  },
  {
    name: 'unit.gauss-square-centimeter',
    symbol: 'G·cm²',
    tags: ['magnetic flux','gauss-square centimeter','G·cm²']
  },
  {
    name: 'unit.kilogauss-square-centimeter',
    symbol: 'kG·cm²',
    tags: ['magnetic flux','kilogauss-square centimeter','kG·cm²']
  },
  {
    name: 'unit.henry',
    symbol: 'H',
    tags: ['inductance','magnetic induction','H']
  },
  {
    name: 'unit.millihenry',
    symbol: 'mH',
    tags: ['inductance','millihenry','mH']
  },
  {
    name: 'unit.microhenry',
    symbol: 'µH',
    tags: ['inductance','microhenry','µH']
  },
  {
    name: 'unit.nanohenry',
    symbol: 'nH',
    tags: ['inductance','nanohenry','nH']
  },
  {
    name: 'unit.henry-per-meter',
    symbol: 'H/m',
    tags: ['magnetic permeability','henry per meter','H/m']
  },
  {
    name: 'unit.tesla-meter-per-ampere',
    symbol: 'T·m/A',
    tags: ['magnetic field','Tesla Meter per Ampere','T·m/A','magnetic flux']
  },
  {
    name: 'unit.gauss-per-oersted',
    symbol: 'G/Oe',
    tags: ['magnetic field','Gauss per Oersted','G/Oe']
  },
  {
    name: 'unit.kilogram-per-mole',
    symbol: 'kg/mol',
    tags: ['molar mass','kilogram per mole','kg/mol']
  },
  {
    name: 'unit.gram-per-mole',
    symbol: 'g/mol',
    tags: ['molar mass','gram per mole','g/mol']
  },
  {
    name: 'unit.milligram-per-mole',
    symbol: 'mg/mol',
    tags: ['molar mass','milligram per mole','mg/mol']
  },
  {
    name: 'unit.joule-per-mole',
    symbol: 'J/mol',
    tags: ['molar energy','joule per mole','J/mol']
  },
  {
    name: 'unit.joule-per-mole-kelvin',
    symbol: 'J/(mol·K)',
    tags: ['molar heat capacity','joule per mole-kelvin','J/(mol·K)']
  },
  {
    name: 'unit.millivolts-per-meter',
    symbol: 'mV/m',
    tags: ['electric field strength','millivolts per meter','mV/m']
  },
  {
    name: 'unit.volts-per-meter',
    symbol: 'V/m',
    tags: ['electric field strength','volts per meter','V/m']
  },
  {
    name: 'unit.kilovolts-per-meter',
    symbol: 'kV/m',
    tags: ['electric field strength','kilovolts per meter','kV/m']
  },
  {
    name: 'unit.radian-per-second',
    symbol: 'rad/s',
    tags: ['angular velocity','rotation speed','rad/s']
  },
  {
    name: 'unit.radian-per-second-squared',
    symbol: 'rad/s²',
    tags: ['angular acceleration','rotation rate of change','rad/s²']
  },
  {
    name: 'unit.revolutions-per-minute-per-second',
    symbol: 'rpm/s',
    tags: ['angular acceleration','rotation rate of change','rpm/s']
  },
  {
    name: 'unit.revolutions-per-minute-per-second-squared',
    symbol: 'rpm/s²',
    tags: ['angular acceleration','rotation rate of change','rpm/s²']
  },
  {
    name: 'unit.deg-per-second',
    symbol: 'deg/s',
    tags: ['angular velocity','degrees per second','deg/s']
  },
  {
    name: 'unit.degrees-brix',
    symbol: '°Bx',
    tags: ['sugar content','fruit ripeness','Bx']
  },
  {
    name: 'unit.katal',
    symbol: 'kat',
    tags: ['catalytic activity','enzyme activity','kat']
  },
  {
    name: 'unit.katal-per-cubic-metre',
    symbol: 'kat/m³',
    tags: ['catalytic activity concentration','enzyme concentration','kat/m³']
  }
];

export const unitBySymbol = (symbol: string): Unit => units.find(u => u.symbol === symbol);

const searchUnitTags = (unit: Unit, searchText: string): boolean =>
  !!unit.tags.find(t => t.toUpperCase().includes(searchText.toUpperCase()));

export const searchUnits = (_units: Array<Unit>, searchText: string): Array<Unit> => _units.filter(
    u => u.symbol.toUpperCase().includes(searchText.toUpperCase()) ||
      u.name.toUpperCase().includes(searchText.toUpperCase()) ||
      searchUnitTags(u, searchText)
);
