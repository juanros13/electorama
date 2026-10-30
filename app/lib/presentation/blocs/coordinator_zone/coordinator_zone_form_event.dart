part of 'coordinator_zone_form_bloc.dart';

class CordinatorZoneFormEvent  {
  const CordinatorZoneFormEvent();
}


class SectionChange extends CordinatorZoneFormEvent{
  final String value;
  const SectionChange(this.value);
}

class MunicipalityChange extends CordinatorZoneFormEvent{
  final String value;
  const MunicipalityChange(this.value);
}


class LocalityChange extends CordinatorZoneFormEvent{
  final String value;
  const LocalityChange(this.value);
}
