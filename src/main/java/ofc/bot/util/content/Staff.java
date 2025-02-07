package ofc.bot.util.content;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import ofc.bot.Main;

import java.util.List;
import java.util.stream.Stream;

public enum Staff {
    GENERAL(         "691178135596695593",  Scope.NONE),
    ALMIRANTES_FROTA("1048808588375773234", Scope.NONE),

    /* Mov Call */
    MOV_CALL_CO_LEADER(  "691167801783877653", Scope.MOV_CALL),
    MOV_CALL_VICE_LEADER("740360644645093437", Scope.MOV_CALL),
    MOV_CALL_SUPERIOR(   "691167797270806538", Scope.MOV_CALL),
    MOV_CALL_MAIN(       "691173151400263732", Scope.MOV_CALL),
    MOV_CALL_TRAINEE(    "691173142969712640", Scope.MOV_CALL),
    
    /* Support */
    AJUDANTES_CO_LEADER(  "648444762852163588", Scope.SUPPORT),
    AJUDANTES_VICE_LEADER("740360642032173156", Scope.SUPPORT),
    AJUDANTES_SUPERIOR(   "691167798474440775", Scope.SUPPORT),
    AJUDANTES_MAIN(       "592427681727905792", Scope.SUPPORT),
    AJUDANTES_TRAINEE(    "648408508219260928", Scope.SUPPORT);

    private final String id;
    private final Scope field;

    Staff(String id, Scope scope) {
        this.id = id;
        this.field = scope;
    }

    public String getId() {
        return this.id;
    }

    public Scope getField() {
        return this.field;
    }

    public Role role() {
        return Main.getApi().getRoleById(this.id);
    }

    public static boolean isStaff(Member member) {
        return isStaff(member.getRoles());
    }

    public static boolean isStaff(List<Role> roles) {
        return roles
                .stream()
                .anyMatch(r -> r.getId().equals(GENERAL.id));
    }

    public static List<Staff> getByScope(Scope scope) {
        return Stream.of(values())
                .filter(s -> s.field == scope)
                .toList();
    }

    public static List<String> getIdsByScope(Scope scope) {
        return getByScope(scope)
                .stream()
                .map(Staff::getId)
                .toList();
    }

    public enum Scope {
        SUPPORT,
        MOV_CALL,
        NONE
    }
}