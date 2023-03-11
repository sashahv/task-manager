package com.olekhv.taskmanager.team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDTO {
    private String name;
    private String description;
    private TeamType type;
}
