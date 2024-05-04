package ofc.bot.commands.staff_list;

record StaffMessageBody(
        String title,
        String role,
        String message,
        String footer
) {}