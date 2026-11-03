package com.erms.employee;
import com.erms.employee.EmployeeDtos.*;
import com.erms.tenant.TenantContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {
 private final JdbcTemplate jdbc;
 private final RowMapper<EmployeeResponse> mapper=this::mapEmployee;
 public EmployeeService(JdbcTemplate jdbc){this.jdbc=jdbc;}

 @Transactional
 public EmployeeResponse create(CreateEmployeeRequest r){
  UUID org=TenantContext.getRequiredOrganizationId(), id=UUID.randomUUID();
  try {
   jdbc.update("""
    INSERT INTO employees(id,organization_id,department_id,position_id,employee_number,full_name,email,phone,hire_date)
    VALUES(?,?,?,?,?,?,?,?,?)
    """,id,org,r.departmentId(),r.positionId(),r.employeeNumber().trim(),r.fullName().trim(),
    r.email().trim().toLowerCase(),r.phone(),r.hireDate());
  } catch(DuplicateKeyException e){
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee number or email already exists in this organization");
  } catch(DataIntegrityViolationException e){
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Department or position does not belong to this organization");
  }
  return findById(id);
 }

 @Transactional(readOnly=true)
 public List<EmployeeResponse> findAll(){
  UUID org=TenantContext.getRequiredOrganizationId();
  return jdbc.query("""
   SELECT id,department_id,position_id,employee_number,full_name,email,phone,employment_status,hire_date
   FROM employees WHERE organization_id=? ORDER BY full_name
   """,mapper,org);
 }

 @Transactional(readOnly=true)
 public EmployeeResponse findById(UUID id){
  UUID org=TenantContext.getRequiredOrganizationId();
  List<EmployeeResponse> rows=jdbc.query("""
   SELECT id,department_id,position_id,employee_number,full_name,email,phone,employment_status,hire_date
   FROM employees WHERE id=? AND organization_id=?
   """,mapper,id,org);
  if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found");
  return rows.getFirst();
 }

 @Transactional
 public EmployeeResponse update(UUID id,UpdateEmployeeRequest r){
  UUID org=TenantContext.getRequiredOrganizationId();
  try {
   int count=jdbc.update("""
    UPDATE employees SET department_id=?,position_id=?,full_name=?,email=?,phone=?,hire_date=?,
    employment_status=?,updated_at=NOW() WHERE id=? AND organization_id=?
    """,r.departmentId(),r.positionId(),r.fullName().trim(),r.email().trim().toLowerCase(),
    r.phone(),r.hireDate(),r.employmentStatus(),id,org);
   if(count==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found");
  } catch(DataIntegrityViolationException e){
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email is already used or department/position is outside this organization");
  }
  return findById(id);
 }

 @Transactional
 public void delete(UUID id){
  UUID org=TenantContext.getRequiredOrganizationId();
  if(jdbc.update("DELETE FROM employees WHERE id=? AND organization_id=?",id,org)==0)
   throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found");
 }

 private EmployeeResponse mapEmployee(ResultSet rs,int row)throws SQLException{
  return new EmployeeResponse(rs.getObject("id",UUID.class),rs.getObject("department_id",UUID.class),
   rs.getObject("position_id",UUID.class),rs.getString("employee_number"),rs.getString("full_name"),
   rs.getString("email"),rs.getString("phone"),rs.getString("employment_status"),
   rs.getDate("hire_date").toLocalDate());
 }
}
