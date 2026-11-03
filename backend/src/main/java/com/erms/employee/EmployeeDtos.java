package com.erms.employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public final class EmployeeDtos {
 private EmployeeDtos() {}
 public record CreateEmployeeRequest(@NotBlank @Size(max=40) String employeeNumber,
  @NotBlank @Size(max=160) String fullName, @NotBlank @Email @Size(max=254) String email,
  @Size(max=40) String phone, UUID departmentId, UUID positionId, @NotNull LocalDate hireDate) {}
 public record UpdateEmployeeRequest(@NotBlank @Size(max=160) String fullName,
  @NotBlank @Email @Size(max=254) String email, @Size(max=40) String phone,
  UUID departmentId, UUID positionId, @NotNull LocalDate hireDate,
  @NotBlank @Pattern(regexp="ACTIVE|ON_LEAVE|RESIGNED|TERMINATED") String employmentStatus) {}
 public record EmployeeResponse(UUID id, UUID departmentId, UUID positionId, String employeeNumber,
  String fullName, String email, String phone, String employmentStatus, LocalDate hireDate) {}
}
