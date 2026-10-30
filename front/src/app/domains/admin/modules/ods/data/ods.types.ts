// Tipos del módulo ODS (Modelo para la transformación de Tabasco): drill-down
// ODS → metas → indicadores → instrumentos.

export interface Objective {
  id: number;
  objective: number;
  description: string;
}

export interface Goal {
  id: number;
  goalNumber: number;
  description: string;
}

export interface Indicator {
  id: number;
  indicatorNumber: number;
  description: string;
}

export interface Instrument {
  id: number;
  indicatorNumber: number;
  description: string;
}
