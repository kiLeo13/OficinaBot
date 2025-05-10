package ofc.bot.commands.slash.stafflist;

record StaffMessageBody(
        String title,
        String role,
        String message,
        String footer
) {}