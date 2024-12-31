package ofc.bot.commands.stafflist;

record StaffMessageBody(
        String title,
        String role,
        String message,
        String footer
) {}