package biz.gelicon.gits.tamtambot.utils;

import chat.tamtam.bot.builders.attachments.AttachmentsBuilder;
import chat.tamtam.botapi.model.UploadedInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AttachmentsCarrier {
    private AttachmentsBuilder images;
    private List<UploadedInfo> files;
}
