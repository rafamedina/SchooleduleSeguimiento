package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherDashboardMapper {

  @Mapping(target = "id", source = "c.id")
  @Mapping(target = "nombre", source = "c.nombre")
  @Mapping(target = "ubicacion", source = "c.ubicacion")
  @Mapping(target = "imparticionesCount", source = "imparticionesCount")
  TeacherCenterDTO toCenterDto(Centro c, long imparticionesCount);

  @Mapping(target = "imparticionId", source = "i.id")
  @Mapping(target = "moduloCodigo", source = "i.modulo.codigo")
  @Mapping(target = "moduloNombre", source = "i.modulo.nombre")
  @Mapping(target = "grupoNombre", source = "i.grupo.nombre")
  @Mapping(target = "cursoAcademicoNombre", source = "i.grupo.cursoAcademico.nombre")
  @Mapping(target = "alumnosCount", source = "alumnosCount")
  TeacherSubjectDTO toSubjectDto(Imparticion i, long alumnosCount);

  @Mapping(target = "matriculaId", source = "m.id")
  @Mapping(target = "alumnoId", source = "m.alumno.id")
  @Mapping(
      target = "nombreCompleto",
      expression = "java(m.getAlumno().getNombre() + \" \" + m.getAlumno().getApellidos())")
  @Mapping(target = "email", source = "m.alumno.email")
  @Mapping(target = "esRepetidor", source = "m.esRepetidor")
  TeacherStudentRowDTO toStudentRow(Matricula m);

  @Mapping(target = "criterioEvaluacionId", source = "ce.id")
  @Mapping(target = "codigo", source = "ce.codigo")
  @Mapping(target = "descripcion", source = "ce.descripcion")
  @Mapping(target = "valor", expression = "java(existing != null ? existing.getValor() : null)")
  @Mapping(
      target = "comentario",
      expression = "java(existing != null ? existing.getComentario() : null)")
  @Mapping(
      target = "calificacionId",
      expression = "java(existing != null ? existing.getId() : null)")
  TeacherCriterioGradeDTO toCriterioGrade(CriterioEvaluacion ce, Calificacion existing);
}
