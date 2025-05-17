package org.cyan.in.model;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@GroupSequence({CreateAliasRequest.class, CreateAliasRequest.NotBlankGroup.class, CreateAliasRequest.PatternGroup.class})
@Data
public class CreateAliasRequest {

    @NotBlank(message = "Alias name cannot be null or blank", groups = NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Alias name can only contain letters and numbers", groups = PatternGroup.class)
    private String name;

    @NotBlank(message = "Alias password cannot be null or blank")
    private String password;

    public interface NotBlankGroup {}
    public interface PatternGroup {}
}
