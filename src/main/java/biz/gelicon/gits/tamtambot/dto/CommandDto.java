package biz.gelicon.gits.tamtambot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandDto {
    @NotBlank(message = "Command text cannot be blank")
    private String text;
    @NotNull(message = "Command showConf cannot be null")
    private float showConf;
    @NotNull(message = "Command allConf cannot be null")
    private float allConf;
    @NotNull(message = "Command logoutConf cannot be null")
    private float logoutConf;
    @NotNull(message = "Command helpConf cannot be null")
    private float helpConf;
}
