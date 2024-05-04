package ofc.bot.commands.staff_list;

import java.util.List;

record InputData(
        String banner,
        String bannerMessageId,
        List<StaffMessageBody> staffs
) {}