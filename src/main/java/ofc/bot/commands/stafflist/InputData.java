package ofc.bot.commands.stafflist;

import java.util.List;

record InputData(
        String banner,
        String bannerMessageId,
        List<StaffMessageBody> staffs
) {}