package ofc.bot.util.content;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.stream.Stream;

public enum Staff {
    GENERAL(                "691178135596695593",   Field.NONE),

    ONYRIX(                 "1048808588375773234",  Field.NONE),
    
    /* Mov Call */
    MOV_CALL_CO_LEADER(     "691167801783877653",   Field.MOV_CALL),
    MOV_CALL_VICE_LEADER(   "740360644645093437",   Field.MOV_CALL),
    MOV_CALL_SUPERIOR(      "691167797270806538",   Field.MOV_CALL),
    MOV_CALL_MAIN(          "691173151400263732",   Field.MOV_CALL),
    MOV_CALL_TRAINEE(       "691173142969712640",   Field.MOV_CALL),
    
    /* Support */
    AJUDANTES_CO_LEADER(    "648444762852163588",   Field.SUPPORT),
    AJUDANTES_VICE_LEADER(  "740360642032173156",   Field.SUPPORT),
    AJUDANTES_SUPERIOR(     "691167798474440775",   Field.SUPPORT),
    AJUDANTES_MAIN(         "592427681727905792",   Field.SUPPORT),
    AJUDANTES_TRAINEE(      "648408508219260928",   Field.SUPPORT);

    private final String id;
    private final Field field;

    Staff(String id, Field field) {
        this.id = id;
        this.field = field;
    }

    public String getId() {
        return this.id;
    }

    public Field getField() {
        return this.field;
    }

    public Role toRole(Guild guild) {
        return guild.getRoleById(this.id);
    }

    public static List<Staff> getByArea(Field field) {
        return Stream.of(values())
                .filter(s -> s.field == field)
                .toList();
    }

    public static List<String> getIdsByArea(Field field) {

        return getByArea(field)
                .stream()
                .map(Staff::getId)
                .toList();
    }

    public enum Field {
        SUPPORT,
        MOV_CALL,
        NONE;
    }
}