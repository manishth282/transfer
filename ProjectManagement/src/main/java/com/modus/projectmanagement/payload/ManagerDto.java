package com.modus.projectmanagement.payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerDto {
    public Long managerId;
    public String managerName;
    public List<ProjectDto> projects;

}
