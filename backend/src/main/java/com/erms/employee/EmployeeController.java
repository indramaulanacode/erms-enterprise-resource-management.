package com.erms.employee;
import com.erms.employee.EmployeeDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@PreAuthorize("hasAnyRole('ORG_ADMIN','HR_MANAGER')")
public class EmployeeController {
 private final EmployeeService service;
 public EmployeeController(EmployeeService service){this.service=service;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request){return service.create(request);}
 @GetMapping public List<EmployeeResponse> findAll(){return service.findAll();}
 @GetMapping("/{id}") public EmployeeResponse findById(@PathVariable UUID id){return service.findById(id);}
 @PutMapping("/{id}") public EmployeeResponse update(@PathVariable UUID id,@Valid @RequestBody UpdateEmployeeRequest request){return service.update(id,request);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){service.delete(id);}
}
