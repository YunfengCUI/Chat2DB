package ai.chat2db.server.web.api.controller.ai.out;


import lombok.Builder;
import org.jetbrains.annotations.NotNull;
@Builder
public class OutModelApi {
    private @NotNull String question;
    private @NotNull Boolean streaming;
    private String[] history;

}
